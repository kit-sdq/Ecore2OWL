package edu.kit.ipd.are.ontologyaccess;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.ontology.AllValuesFromRestriction;
import org.apache.jena.ontology.CardinalityRestriction;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.EnumeratedClass;
import org.apache.jena.ontology.FunctionalProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.MaxCardinalityRestriction;
import org.apache.jena.ontology.MinCardinalityRestriction;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.OntTools;
import org.apache.jena.ontology.OntTools.Path;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.ValidityReport.Report;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OntologyAccess {
    private static Logger logger = LoggerFactory.getLogger(OntologyAccess.class);
    private static OntModelSpec modelSpec = OntModelSpec.OWL_DL_MEM;
    // alternatives: OWL_MEM, OWL_DL_MEM, OWL_DL_MEM_RULE_INF, OWL_DL_MEM_TRANS_INF, OWL_DL_MEM_RDFS_INF
    // other see https://jena.apache.org/documentation/javadoc/jena/org/apache/jena/ontology/OntModelSpec.html

    private OntModel ontModel;
    private InfModel infModel = null;
    private String defaultPrefix = "";

    private OntologyAccess() {
        super();
    }

    public static OntologyAccess ofFile(String ontoFile) {
        OntologyAccess ontAcc = new OntologyAccess();
        ontAcc.ontModel = ModelFactory.createOntologyModel(modelSpec);
        ontAcc.ontModel.read(ontoFile);
        return ontAcc;
    }

    public static OntologyAccess ofOntModel(OntModel ontModel) {
        OntologyAccess ontAcc = new OntologyAccess();
        ontAcc.ontModel = ontModel;
        return ontAcc;
    }

    public static OntologyAccess empty(String defaultNameSpaceUri) {
        OntologyAccess ontAcc = new OntologyAccess();
        ontAcc.ontModel = ModelFactory.createOntologyModel(modelSpec);
        ontAcc.ontModel.createOntology(defaultNameSpaceUri);
        ontAcc.ontModel.setNsPrefix("", defaultNameSpaceUri);
        ontAcc.ontModel.setNsPrefix("xsd", XSD.NS);
        return ontAcc;
    }

    private synchronized InfModel getInfModel() {
        if (infModel == null) {
            Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
            infModel = ModelFactory.createInfModel(reasoner, ontModel);
        }
        return infModel;
    }

    public OntModel getOntology() {
        return this.ontModel;
    }

    public boolean validateOntology() {
        InfModel infModel = ModelFactory.createRDFSModel(ontModel);
        ValidityReport validity = infModel.validate();
        if (validity.isValid()) {
            return true;
        }
        Iterator<Report> i = validity.getReports();
        while (i.hasNext()) {
            logger.warn("Conflict in ontology: {}", i.next());
        }
        return false;
    }

    public boolean save(String file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        try {
            OutputStream out = new FileOutputStream(file);
            ontModel.write(out, "RDF/XML");
            return true;
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        }

        return false;
    }

    public void setDefaultPrefix(String prefix) {
        this.defaultPrefix = prefix;
    }

    public void addNsPrefix(String prefix, String uri) {
        ontModel.setNsPrefix(prefix, uri);
    }

    private String createUri(String suffix) {
        return createUri(defaultPrefix, suffix);
    }

    private String createUri(String prefix, String suffix) {
        return ontModel.expandPrefix(prefix + ":" + suffix);
    }

    public MutableList<Individual> searchIndividual(Predicate<Individual> searchPredicate) {
        return createMutableListFromIterator(ontModel.listIndividuals()
                                                     .filterKeep(searchPredicate));
    }

    /**
     * Adds an individual to the ontology by adding it as individual of OWL:Thing.
     * 
     * @param shortUri
     *            ShortUri of the Individual
     * @return the created Individual
     */
    public Individual addNamedIndividual(String shortUri) {
        Resource clazz = OWL.Thing;
        String uri = createUri(shortUri);
        return ontModel.createIndividual(uri, clazz);
    }

    public Individual addNamedIndividual(OntClass cls, String shortUri) {
        String uri = createUri(shortUri);
        Individual individual = ontModel.getIndividual(uri);
        if (individual != null) {
            individual.addOntClass(cls);
            individual.removeOntClass(OWL.Thing);
            return individual;
        }
        return cls.createIndividual(uri);
    }

    public Individual addNamedIndividual(String className, String shortUri) {
        OntClass clazz = addClass(className);
        return addNamedIndividual(clazz, shortUri);
    }

    public Optional<Individual> getNamedIndividual(String individualName) {
        if (individualName == null) {
            return Optional.empty();
        }
        // check if name is actually an uri and return the found individual, if it is
        Optional<Individual> uriIndividual = getNamedIndividualByShortUri(individualName);
        if (uriIndividual.isPresent()) {
            return uriIndividual;
        }
        return ontModel.listIndividuals()
                       .filterKeep(individual -> {
                           Optional<String> optName = getName(individual.getURI());
                           if (optName.isPresent()) {
                               String name = optName.get();
                               return name.equals(individualName);
                           } else {
                               return false;
                           }
                       })
                       .nextOptional();
    }

    public Optional<Individual> getNamedIndividualByShortUri(String individualShortUri) {
        return Optional.ofNullable(ontModel.getIndividual(createUri(individualShortUri)));
    }

    public Optional<Individual> getNamedIndividualByUri(String individualUri) {
        return Optional.ofNullable(ontModel.getIndividual(individualUri));
    }

    public Optional<String> getName(String individualUri) {
        List<String> names = getDataPropertyValuesForIndividual("entityName_-_NamedElement", individualUri);
        return (names.isEmpty()) ? Optional.empty() : Optional.of(names.get(0));
    }

    public MutableList<OntProperty> getAllNamedProperties() {
        return createMutableListFromIterator(ontModel.listAllOntProperties());
    }

    public MutableList<Property> getNamedPropertiesWithLocalName(String localName) {
        MutableList<Property> propertyList = Lists.mutable.empty();
        ontModel.listOntProperties()
                .forEachRemaining(prop -> {
                    if (prop.getLocalName()
                            .equals(localName)) {
                        propertyList.add(prop);
                    }
                });
        return propertyList;

    }

    public boolean containsDataProperty(String name) {
        return getDataProperty(name).isPresent();
    }

    /**
     * Adds a DatatypeProperty and sets the range. Range is xsd:string
     * 
     * @param name
     *            Name of the property
     * @return the created DatatypeProperty
     */
    public DatatypeProperty addDataProperty(String name) {
        return ontModel.createDatatypeProperty(createUri(name));
    }

    /**
     * Adds a DatatypeProperty with the given range as type.
     * 
     * @param name
     *            Name of the property
     * @param domain
     *            Domain of the DatatypeProperty
     * @return the created DatatypeProperty
     */
    public DatatypeProperty addDataProperty(String name, Resource domain) {
        DatatypeProperty property = addDataProperty(name);
        property.addDomain(domain);
        return property;
    }

    /**
     * Adds a DatatypeProperty with the given range as type.
     * 
     * @param name
     *            Name of the property
     * @param domain
     *            Domain of the DatatypeProperty
     * @param range
     *            Range of the DatatypeProperty
     * @return the created DatatypeProperty
     */
    public DatatypeProperty addDataProperty(String name, Resource domain, Resource range) {
        DatatypeProperty property = addDataProperty(name, domain);
        property.addRange(range);
        return property;
    }

    public void addStringDataPropertyToIndividual(Individual individual, DatatypeProperty property, String value) {
        Literal literal = ontModel.createTypedLiteral(value);
        ontModel.addLiteral(individual, property, literal);
    }

    public void addDataPropertyToIndividual(Individual individual, DatatypeProperty property, Object value) {
        Literal literal = ontModel.createTypedLiteral(value);
        ontModel.addLiteral(individual, property, literal);
    }

    public void addDataPropertyToIndividual(Individual individual, DatatypeProperty property, Object value,
            RDFDatatype type) {
        Literal literal = ontModel.createTypedLiteral(value, type);
        ontModel.addLiteral(individual, property, literal);
    }

    public MutableList<DatatypeProperty> getDataPropertiesOfIndividual(String individualName) {
        Optional<Individual> individual = getNamedIndividual(individualName);
        if (!individual.isPresent()) {
            return Lists.mutable.empty();
        }
        return getPropertiesOfIndividual(individual.get()).select(property -> property.canAs(DatatypeProperty.class))
                                                          .collect(property -> property.as(DatatypeProperty.class));
    }

    public MutableList<DatatypeProperty> getDataPropertiesOfIndividualbyUri(String individualUri) {
        Optional<Individual> individual = getNamedIndividualByUri(individualUri);
        if (!individual.isPresent()) {
            return Lists.mutable.empty();
        }
        return getPropertiesOfIndividual(individual.get()).select(property -> property.canAs(DatatypeProperty.class))
                                                          .collect(property -> property.as(DatatypeProperty.class));
    }

    public MutableList<String> getDataPropertyValuesForIndividual(String datatypePropertyName, String individualUri) {
        Optional<DatatypeProperty> optProperty = getDataProperty(datatypePropertyName);
        if (!optProperty.isPresent()) {
            return Lists.mutable.empty();
        }
        DatatypeProperty property = optProperty.get();
        return getDataPropertyValuesForIndividual(property, individualUri);
    }

    public MutableList<String> getDataPropertyValuesForIndividual(Property property, String individualUri) {
        Resource resource = ontModel.getResource(individualUri);
        StmtIterator statementsIterator = resource.listProperties(property);
        MutableList<Statement> statements = createMutableListFromIterator(statementsIterator);
        return statements.collect(Statement::getString);
    }

    public Optional<DatatypeProperty> getDataProperty(String dataPropertyLocalName) {
        String uri = createUri(dataPropertyLocalName);
        return getDataPropertyByUri(uri);
    }

    public Optional<DatatypeProperty> getDataPropertyByUri(String dataPropertyUri) {
        return Optional.ofNullable(ontModel.getDatatypeProperty(dataPropertyUri));
    }

    private ObjectProperty addObjectProperty(String objectPropertyName, OntClass domain, OntClass range,
            boolean functional) {
        ObjectProperty property = ontModel.createObjectProperty(createUri(objectPropertyName), functional);
        property.addDomain(domain);
        property.addRange(range);
        return property;
    }

    public ObjectProperty addObjectProperty(String objectPropertyName, OntClass domain, OntClass range) {
        return addObjectProperty(objectPropertyName, domain, range, false);
    }

    public ObjectProperty addFunctionalObjectProperty(String objectPropertyName, OntClass domain, OntClass range) {
        return addObjectProperty(objectPropertyName, domain, range, true);
    }

    public boolean containsObjectProperty(String objectPropertyName) {
        return getObjectProperty(objectPropertyName).isPresent();
    }

    public Optional<ObjectProperty> getObjectProperty(String objectPropertyName) {
        String uri = createUri(objectPropertyName);
        return getObjectPropertyByUri(uri);
    }

    public Optional<ObjectProperty> getObjectPropertyByUri(String objectPropertyUri) {
        return Optional.ofNullable(ontModel.getObjectProperty(objectPropertyUri));
    }

    /**
     * Adds a restriction that all values have to come from a specific class
     * 
     * @param uri
     *            Uri of the restriction, or null if anonymous
     * @param property
     *            Property that should be restricted
     * @param cls
     *            The class to which all value are restricted to
     * @return the restriction
     */
    public AllValuesFromRestriction addAllValuesFrom(String uri, Property property, Resource cls) {
        return ontModel.createAllValuesFromRestriction(uri, property, cls);
    }

    /**
     * Adds a restriction that all values have to come from a specific class
     * 
     * @param property
     *            Property that should be restricted
     * @param cls
     *            The class to which all value are restricted to
     * @return the restriction
     */
    public AllValuesFromRestriction addAllValuesFrom(Property property, Resource cls) {
        return ontModel.createAllValuesFromRestriction(null, property, cls);
    }

    public EnumeratedClass addAnonymousEnumeratedClass() {
        return ontModel.createEnumeratedClass(null, null);
    }

    public EnumeratedClass addEnumeratedClass(String name) {
        String uri = createUri(name);
        return ontModel.createEnumeratedClass(uri, null);
    }

    public void addIndividualToEnumeratedClass(EnumeratedClass clazz, Individual individual) {
        clazz.addOneOf(individual);
    }

    /**
     * Adds a statement provided with a triple (subject, predicate, object)
     * 
     * @param subject
     *            the subject
     * @param property
     *            the predicate
     * @param object
     *            the object
     */
    public void addStatement(Resource subject, ObjectProperty property, RDFNode object) {
        ontModel.add(subject, property, object);
    }

    public Optional<ObjectProperty> addObjectPropertyOfIndividual(String subjectShortUri, String propertyName,
            String objectShortUri) {
        Optional<Individual> optSubject = getNamedIndividualByShortUri(subjectShortUri);
        if (!optSubject.isPresent()) {
            optSubject = getNamedIndividual(subjectShortUri);
            if (!optSubject.isPresent()) {
                String msg = "Could not find subject for ObjectProperty \"" + propertyName + "\" of Individual: "
                        + subjectShortUri;
                logger.debug(msg);
                return Optional.empty();
            }
        }
        Individual subject = optSubject.get();

        Optional<ObjectProperty> optProperty = getObjectProperty(propertyName);
        if (!optProperty.isPresent()) {
            return Optional.empty();
        }
        ObjectProperty property = optProperty.get();

        Optional<Individual> optObject = getNamedIndividual(objectShortUri);
        if (!optObject.isPresent()) {
            String uri = createUri(objectShortUri);
            optObject = Optional.of(ontModel.createIndividual(uri, OWL.Thing));
        }
        Individual object = optObject.get();

        addStatement(subject, property, object);
        return Optional.of(property);
    }

    /**
     * Adds an {@link ObjectProperty} to an individual.
     * 
     * @param subject
     *            Subject of the property
     * @param property
     *            the property
     * @param object
     *            Object of the property
     */
    public void addObjectPropertyOfIndividual(Resource subject, ObjectProperty property, RDFNode object) {
        addStatement(subject, property, object);
    }

    /**
     * Checks whether there is an existing ObjectProperty that fulfils the given parameters. A parameter that is
     * <code>null</code> matches everything.
     * 
     * @param subjectName
     *            name of Subject (or null)
     * @param propertyName
     *            name of Property (or null)
     * @param objectName
     *            name of Object (or null)
     * @return whether there is an existing ObjectProperty with the given subject and object
     */
    public boolean containsObjectPropertyForIndividuals(String subjectName, String propertyName, String objectName) {
        Optional<Individual> optSubject = getNamedIndividual(subjectName);
        Individual subject = optSubject.orElse(null);

        Optional<ObjectProperty> optProperty = getObjectProperty(propertyName);
        ObjectProperty property = optProperty.orElse(null);

        Optional<Individual> optObject = getNamedIndividual(objectName);
        Individual object = optObject.orElse(null);

        return containsObjectPropertyForIndividuals(subject, property, object);
    }

    /**
     * Checks whether there is an existing ObjectProperty that fulfils the given parameters. A parameter that is
     * <code>null</code> matches everything.
     * 
     * @param subject
     *            Subject (or null)
     * @param property
     *            Property (or null)
     * @param object
     *            Object (or null)
     * @return whether there is an existing ObjectProperty with the given subject and object
     */
    public boolean containsObjectPropertyForIndividuals(Resource subject, ObjectProperty property, RDFNode object) {
        if (subject == null || property == null || object == null) {
            return false;
        }

        StmtIterator statements = ontModel.listStatements(subject, property, object);
        return statements.hasNext();
    }

    /**
     * Returns a list of {@link ObjectProperty} for the individual that is provided with its name
     * 
     * @param individualName
     *            name of an individual
     * @return List of object properties of the individual. If the individual cannot be found, returns an empty list
     */
    public MutableList<ObjectProperty> getObjectPropertiesOfIndividual(String individualName) {
        Optional<Individual> optIndividual = getNamedIndividual(individualName);
        if (!optIndividual.isPresent()) {
            return Lists.mutable.empty();
        }
        Individual individual = optIndividual.get();
        return getObjectPropertiesOfIndividualWithUri(individual.getURI());

    }

    /**
     * Returns a list of {@link ObjectProperty} for individual given by the URI
     * 
     * @param individualUri
     *            URI of an individual
     * @return List of object properties of the individual. If the individual cannot be found, returns an empty list
     */
    public MutableList<ObjectProperty> getObjectPropertiesOfIndividualWithUri(String individualUri) {
        return getObjectPropertiesOfIndividual(ontModel.getResource(individualUri));
    }

    /**
     * Returns a list of {@link ObjectProperty} for the given individual
     * 
     * @param individual
     *            individual
     * @return List of object properties of the individual
     */
    public MutableList<ObjectProperty> getObjectPropertiesOfIndividual(Resource individual) {
        return getPropertiesOfIndividual(individual).select(property -> property.canAs(ObjectProperty.class))
                                                    .collect(property -> property.as(ObjectProperty.class));
    }

    /**
     * Returns the Properties of an individual
     * 
     * @param individual
     *            Individual
     * @return List of properties of the individual
     */
    public MutableList<Property> getPropertiesOfIndividual(Resource individual) {
        MutableList<Statement> statements = createMutableListFromIterator(individual.listProperties());
        return statements.collect(Statement::getPredicate);
    }

    public FunctionalProperty addFunctionalToProperty(OntProperty property) {
        return property.convertToFunctionalProperty();
    }

    public MinCardinalityRestriction addMinCardinalityToProperty(OntProperty property, int cardinality) {
        return ontModel.createMinCardinalityRestriction(null, property, cardinality);
    }

    public MaxCardinalityRestriction addMaxCardinalityToProperty(OntProperty property, int cardinality) {
        return ontModel.createMaxCardinalityRestriction(null, property, cardinality);
    }

    public CardinalityRestriction addCardinalityToProperty(OntProperty property, int cardinality) {
        return ontModel.createCardinalityRestriction(null, property, cardinality);
    }

    public void addRangeToProperty(OntProperty property, Resource range) {
        property.addRange(range);
    }

    public void addDomainToProperty(OntProperty property, Resource domain) {
        property.addDomain(domain);
    }

    /**
     * Returns an Optional holding the class that corresponds to the given name. If no such class exists, returns an
     * empty optional.
     * 
     * @param className
     *            Name of the class that should be returned
     * @return Optional holding the class that corresponds to the given name, or an empty optional if no such exists
     */
    public Optional<OntClass> getClass(String className) {
        return Optional.ofNullable(ontModel.getOntClass(createUri(className)));
    }

    /**
     * Adds a class with the given name to the ontology. If the class exists already, returns the existing class.
     * 
     * @param className
     *            Name of the class that should be created
     * @return created class
     */
    public OntClass addClass(String className) {
        return ontModel.createClass(createUri(className));
    }

    public void addSubClassing(OntClass subClass, Resource superClass) {
        subClass.addSuperClass(superClass);
    }

    public void addSubClassingExclusive(OntClass subClass, Resource superClass) {
        subClass.setSuperClass(superClass);
    }

    public OntClass addSubClassOf(String className, String superClassName) {
        Optional<OntClass> superClassOpt = getClass(superClassName);
        if (superClassOpt.isPresent()) {
            OntClass superClass = superClassOpt.get();
            return addSubClassOf(className, superClass);
        } else {
            return addClass(className);
        }
    }

    public OntClass addSubClassOf(String className, OntClass superClass) {
        OntClass clazz = addClass(className);
        superClass.addSubClass(clazz);
        return clazz;
    }

    public void addSubClassProperty(OntClass subClass, OntClass superClass) {
        superClass.addSubClass(subClass);
    }

    public boolean classIsSubClassOf(OntClass clazz, OntClass superClass) {
        return clazz.hasSuperClass(superClass) && superClass.hasSubClass(clazz);
    }

    public void removeSubClassing(OntClass clazz, OntClass superClass) {
        clazz.removeSuperClass(superClass);
        superClass.removeSubClass(clazz);
    }

    public boolean containsClass(String className) {
        return getClass(className).isPresent();
    }

    /**
     * Returns the Individuals that have a class that corresponds to the given class name
     * 
     * @param className
     *            Name of the class
     * @return List of Individuals for the given class (name)
     */
    public MutableList<Individual> getInstancesOfClass(String className) {
        Optional<OntClass> optClass = getClass(className);
        if (!optClass.isPresent()) {
            return Lists.mutable.empty();
        }
        OntClass clazz = optClass.get();
        return getInstancesOfClass(clazz);
    }

    // TODO
    public MutableList<Individual> getInferredInstancesOfClass(String className) {
        Optional<OntClass> optClass = getClass(className);
        if (!optClass.isPresent()) {
            return Lists.mutable.empty();
        }
        OntClass clazz = optClass.get();

        StmtIterator stmts = getInfModel().listStatements(null, RDF.type, clazz);
        return createMutableIndividualListFromStatementIterator(stmts);
    }

    private MutableList<Individual> getInstancesOfClass(OntClass clazz) {
        return createMutableListFromIterator(ontModel.listIndividuals(clazz));
    }

    /**
     * Adds an Individual to the given class
     * 
     * @param name
     *            name of the individual that should be added
     * @param clazz
     *            Class the individual should be added to
     * @return the created individual
     */
    public Individual addInstanceToClass(String name, OntClass clazz) {
        return ontModel.createIndividual(createUri(name), clazz);
    }

    /**
     * Returns the values of the given {@link ObjectProperty} for the provided {@link Individual}
     * 
     * @param individual
     *            Individual that should be checked
     * @param property
     *            Property that should be evaluated
     * @return List of values for the given Individual and ObjectProperty
     */
    public MutableList<Resource> getObjectPropertyValues(Individual individual, Property property) {
        MutableList<Statement> properties = createMutableListFromIterator(individual.listProperties(property));
        return properties.collect(Statement::getObject)
                         .collect(RDFNode::asResource);
    }

    /**
     * Adds a comment to a resource with the provided language set
     * 
     * @param resource
     *            Resource that should be annotated with the comment
     * @param comment
     *            comment that should be annotated
     * @param lang
     *            Language that should be set
     */
    public void addComment(OntResource resource, String comment, String lang) {
        resource.addComment(comment, lang);
    }

    public void addLabel(OntResource resource, String label) {
        addLabel(resource, label, null);
    }

    public void addLabel(OntResource resource, String label, String language) {
        resource.addLabel(label, language);
    }

    /**
     * Find all the statements matching a pattern. Lists all statements within the ontology with the given pattern of
     * subject-predicate-object
     * 
     * @param subject
     *            Subject
     * @param predicate
     *            Predicate
     * @param object
     *            Object
     * @return all the statements matching the pattern
     */
    public StmtIterator listStatements(Resource subject, Property predicate, RDFNode object) {
        return ontModel.listStatements(subject, predicate, object);
    }

    /**
     * Returns whether a node is an instance of the given class
     * 
     * @param node
     *            Node that should be checked
     * @param clazz
     *            Class that should be checked for
     * @return whether a node is an instance of the given class
     */
    public boolean nodeHasClass(RDFNode node, OntClass clazz) {
        if (node.isURIResource()) {
            String uri = node.asResource()
                             .getURI();
            Optional<Individual> optIndi = getNamedIndividualByUri(uri);
            if (optIndi.isPresent() && optIndi.get()
                                              .hasOntClass(clazz)) {
                return true;
            }
        }
        return false;
    }

    public Optional<Path> getShortestPathBetween(Individual start, Individual target, Predicate<Statement> filter) {
        return Optional.ofNullable(OntTools.findShortestPath(ontModel, start, target, filter));
    }

    /**
     * Returns the shortest path between two individuals while treating every edge as undirected. The path can only
     * travel to directions the provided filter allows.
     * 
     * @param start
     *            Starting individual
     * @param target
     *            Target individual
     * @param filter
     *            Filter for valid Statements (for the path)
     * @return Optional holding the shortest Path from start to target, if one exists
     */
    public Optional<Path> getUndirectedShortestPathBetween(Individual start, Individual target,
            Predicate<Statement> filter) {
        MutableList<Path> paths = Lists.mutable.empty();
        MutableSet<Statement> seen = UnifiedSet.newSet();
        Predicate<Statement> selector = stmt -> !seen.contains(stmt);
        selector = selector.and(filter);

        // initialise the paths
        ExtendedIterator<Statement> statements = getStatementsContainingResource(start);
        while (statements.hasNext()) {
            Statement statement = statements.next();
            if (selector.test(statement)) {
                paths.add(new Path().append(statement));
                seen.add(statement);
            }
        }

        Path solution = null;
        while (!paths.isEmpty()) {
            Path candidate = paths.remove(0);

            // check if solution found
            if (pathHasTerminus(candidate, target)) {
                solution = candidate;
                break;
            }
            // breadth-first expansion
            MutableList<Resource> termini = getTerminalResourcesFromPath(candidate);
            for (Resource terminus : termini) {
                statements = getStatementsContainingResource(terminus);
                while (statements.hasNext()) {
                    Statement statement = statements.next();
                    if (selector.test(statement)) {
                        paths.add(candidate.append(statement));
                        seen.add(statement);
                    }
                }
            }
        }
        return Optional.ofNullable(solution);
    }

    private ExtendedIterator<Statement> getStatementsContainingResource(Resource terminus) {
        ExtendedIterator<Statement> stmts = terminus.listProperties();
        return stmts.andThen(ontModel.listStatements((Resource) null, null, terminus));
    }

    private boolean pathHasTerminus(Path path, Resource target) {
        return path.hasTerminus(target) || (!path.isEmpty()) && target.equals(path.get(path.size() - 1)
                                                                                  .getSubject());
    }

    private MutableList<Resource> getTerminalResourcesFromPath(Path path) {
        MutableList<Resource> resources = Lists.mutable.empty();
        if (path.isEmpty()) {
            return resources;
        }
        Statement stmt = path.get(path.size() - 1);
        RDFNode terminal = stmt.getObject();
        if (terminal != null && terminal.isResource()) {
            resources.add((Resource) terminal);
        }
        Resource subj = stmt.getSubject();
        if (subj != null && subj.isResource()) {
            resources.add((Resource) subj);
        }
        return resources;
    }

    private static <T> MutableList<T> createMutableListFromIterator(Iterator<T> iterator) {
        MutableList<T> list = Lists.mutable.empty();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    private MutableList<Individual> createMutableIndividualListFromStatementIterator(StmtIterator stmts) {
        MutableList<Individual> individuals = Lists.mutable.empty();
        while (stmts.hasNext()) {
            Statement stmt = stmts.nextStatement();
            Resource res = stmt.getSubject();
            Optional<Individual> optIndividual = getNamedIndividualByUri(res.getURI());
            if (optIndividual.isPresent()) {
                individuals.add(optIndividual.get());
            }
        }
        return individuals;
    }

    public static boolean compareNamesSimple(String name1, String name2) {
        if (name1 == null || name1.isEmpty() || name2 == null || name2.isEmpty()) {
            return false;
        }
        return (name1.contains(name2) || name2.contains(name1));
    }

    public Optional<Resource> getDatatypeByName(String name) {
        switch (name) {
        case "EString":
        case "String":
        case "Char":
        case "EChar":
        case "ECharObject":
            return Optional.of(XSD.xstring);
        case "EInt":
        case "EIntegerObject":
        case "Integer":
        case "Int":
            return Optional.of(XSD.integer);
        case "EBoolean":
        case "EBooleanObject":
        case "Boolean":
            return Optional.of(XSD.xboolean);
        case "EByte":
        case "EByteObject":
        case "Byte":
            return Optional.of(XSD.xbyte);
        case "Currency":
            return Optional.of(XSD.decimal);
        case "EDate":
        case "Date":
            return Optional.of(XSD.date);
        case "Double":
        case "EDouble":
        case "EDoubleObject":
            return Optional.of(XSD.xdouble);
        case "Float":
        case "EFloat":
        case "EFloatObject":
            return Optional.of(XSD.xfloat);
        case "ELong":
        case "ELongObject":
        case "Long":
        case "UnlimitedNatural":
        case "EBigInteger":
        case "BigInteger":
            return Optional.of(XSD.xlong);
        case "Single":
        case "EShort":
        case "EShortObject":
            return Optional.of(XSD.xshort);
        case "Variant":
        default:
            if (logger.isDebugEnabled()) {
                String msg = "getDatatypeByName() -> Default case triggered for: " + name;
                logger.debug(msg);
            }
            return Optional.empty();
        }
    }
}
