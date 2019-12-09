/**
 *
 */
package edu.kit.ipd.are.ecore2owl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.ontology.AllValuesFromRestriction;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.EnumeratedClass;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.vocabulary.XSD;
import org.apache.log4j.Logger;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

import edu.kit.ipd.are.ontologyaccess.OntologyAccess;

/**
 * Class for transforming Ecore models from the EMF into ontologies in OWL format.
 *
 * @author Jan Keim
 *
 */
public class Ecore2OWLTransformer {

    private static final Logger logger = Logger.getLogger(Ecore2OWLTransformer.class);

    public static final String E_ENUM = "EEnum";
    public static final String NS_URI_COMMENT_LANGUAGE = "nsURI";
    public static final String ENUM_VALUE_PROPERTY_SUFFIX = "EValue";
    public static final String ENUM_LITERAL_PROPERTY_SUFFIX = "ELiteral";
    public static final String PROPERTY_TO_CLASS_SEPARATOR = "_-_";
    public static final String E_CLASS = "EClass";
    public static final String E_PACKAGE = "EPackage";
    public static final String PROXY_SUPER_CLASS = "ProxyEClass";
    private static final String CLASS_TYPE = "classType";
    private static final String INTERFACE = "interface";
    private static final String ABSTRACT_CLASS = "abstract";

    private final String documentString;
    private OntologyAccess ontologyAccess = null;
    private Map<String, OntClass> createdEnums = new HashMap<>();
    private Map<EObject, String> eObjectNames = new HashMap<>();
    private Set<EPackage> processedPackages = new HashSet<>();
    private Set<EObject> processedEObjects = new HashSet<>();
    private OntClass eClassOntClass;
    private OntClass ePackageOntClass;
    private EPackage metaModelRoot;

    public Ecore2OWLTransformer(String outputDocumentFile) {
        if (outputDocumentFile == null || outputDocumentFile.isEmpty()) {
            throw new IllegalArgumentException("Invalid output file!");
        }
        documentString = outputDocumentFile;
    }

    /**
     * Loads an Ecore {@link Resource} from the as {@link String} given URL and returns the loaded {@link Resource}
     *
     * @param inputResourceUrl
     *            URL of the input resource, that should be loaded
     * @return loaded Resource
     */
    @Deprecated
    public static Resource loadEcoreResource(String inputResourceUrl) {
        // register and load metamodel
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.setResourceFactoryRegistry(Resource.Factory.Registry.INSTANCE);
        Map<String, Object> extensionToFactoryMap = resourceSet.getResourceFactoryRegistry()
                                                               .getExtensionToFactoryMap();

        extensionToFactoryMap.put("ecore", new EcoreResourceFactoryImpl());
        extensionToFactoryMap.putAll(Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap());

        URI uri;
        if (inputResourceUrl.startsWith("platform:")) {
            // input is no file but a platform-resource, so just use URI
            uri = URI.createURI(inputResourceUrl);
        } else {
            // input is a file and should be loaded from a file
            uri = URI.createFileURI(inputResourceUrl);
        }

        Resource metaModel = resourceSet.getResource(uri, true);
        try {
            metaModel.load(null);
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
        EcoreUtil.resolveAll(resourceSet);
        EcoreUtil.resolveAll(metaModel);
        return metaModel;
    }

    public OntologyAccess getOntologyAccess() {
        return ontologyAccess;
    }

    public void saveOntology() {
        logger.info("Saving created ontology");
        boolean saved = ontologyAccess.save(documentString);
        if (!saved) {
            logger.warn("Could not save ontology");
        }
    }

    @Deprecated
    public void transformEcore(String ecoreFile) {
        if (ecoreFile == null || ecoreFile.isEmpty()) {
            throw new IllegalArgumentException("Invalid input file!");
        }
        Resource resource = loadEcoreResource(ecoreFile);
        transformEcore(resource);
    }

    public void transformEcore(Resource inputEcore) {
        metaModelRoot = (EPackage) inputEcore.getContents()
                                             .get(0);

        preparePackageTransformation(metaModelRoot);

        if (eClassOntClass != null) {
            processEPackage(metaModelRoot);
        } else {
            logger.warn("Initialisation unsuccessful. Stopping now");
        }
    }

    private void preparePackageTransformation(EPackage ePackage) {
        if (ontologyAccess == null) {
            logger.debug("Initialising OntologyAccess");
            String nsURI = ePackage.getNsURI();
            String defaultNameSpace = nsURI + "/owl#";
            ontologyAccess = OntologyAccess.empty(defaultNameSpace);
            ontologyAccess.addNsPrefix("model", defaultNameSpace);
            ontologyAccess.setDefaultPrefix("model");
            eClassOntClass = ontologyAccess.addClass(E_CLASS);
            ePackageOntClass = ontologyAccess.addClass(E_PACKAGE);
        }
    }

    @Deprecated
    public void transformModel(String modelFile) {
        transformModel(modelFile, false);
    }

    @Deprecated
    public void transformModel(String modelFile, boolean resolveMetaModel) {
        if (modelFile == null || modelFile.isEmpty()) {
            throw new IllegalArgumentException("Invalid input file!");
        }
        Resource resource = loadEcoreResource(modelFile);
        transformModel(resource, resolveMetaModel);
    }

    public void transformModel(Resource inputModel, boolean resolveMetaModel) {
        if (resolveMetaModel) {
            EPackage ePackage = inputModel.getContents()
                                          .get(0)
                                          .eClass()
                                          .getEPackage();
            ePackage = getHighestSuperEPackage(ePackage);
            metaModelRoot = ePackage;

            preparePackageTransformation(metaModelRoot);

            processEPackage(metaModelRoot);
        }
        transformModel(inputModel);
    }

    private void transformModel(Resource inputModel) {
        String modelUri = inputModel.getURI()
                                    .toString();
        if (!modelIsConformToMetaModel(inputModel, metaModelRoot)) {
            logger.warn("Model is not conform with meta-model. Aborting the process for this model: " + modelUri);
            return;
        }

        EList<EObject> contents = inputModel.getContents();
        for (EObject object : contents) {
            processEObject(object);
        }
    }

    private EPackage getHighestSuperEPackage(EPackage ePackage) {
        while (true) {
            EPackage superEPackage = ePackage.getESuperPackage();
            if (superEPackage == null || superEPackage.equals(ePackage)) {
                break;
            }
            ePackage = superEPackage;
        }
        return ePackage;
    }

    private boolean modelIsConformToMetaModel(Resource inputModel, EPackage metaModelRoot) {
        // check each top level content if they PackageURI is contained in the metaModel
        List<String> allPackages = getAllESubpackages(metaModelRoot).stream()
                                                                    .map(EPackage::getNsURI)
                                                                    .collect(Collectors.toUnmodifiableList());
        for (EObject eObject : inputModel.getContents()) {
            String eObjectPackageNsUri = eObject.eClass()
                                                .getEPackage()
                                                .getNsURI();
            if (!allPackages.contains(eObjectPackageNsUri)) {
                return false;
            }
        }
        return true;
    }

    private List<EPackage> getAllESubpackages(EPackage metaModelRoot) {
        List<EPackage> subPackages = new ArrayList<>();
        if (metaModelRoot == null) {
            return subPackages;
        }
        subPackages.add(metaModelRoot);
        subPackages.addAll(metaModelRoot.getESubpackages());
        List<EPackage> addedPackages = new ArrayList<>(metaModelRoot.getESubpackages());
        List<EPackage> newlyFoundPackages = new ArrayList<>();
        while (!addedPackages.isEmpty()) {
            for (EPackage ePackage : addedPackages) {
                for (EPackage newEPackage : ePackage.getESubpackages()) {
                    if (!subPackages.contains(newEPackage)) {
                        newlyFoundPackages.add(newEPackage);
                    }
                }
            }
            subPackages.addAll(newlyFoundPackages);
            addedPackages = new ArrayList<>(newlyFoundPackages);
            newlyFoundPackages = new ArrayList<>();
        }
        return subPackages;
    }

    private void processEPackage(EPackage ePackage) {
        if (processedPackages.contains(ePackage)) {
            return;
        }
        EPackage superPackage = ePackage.getESuperPackage();
        String packageName = ePackage.getName();
        String packageNsURI = ePackage.getNsURI();
        String packageNsPrefix = ePackage.getNsPrefix();
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Start processing EPackage with name \"%s\", NS-Prefix \"%s\" and NS-URI \"%s\"",
                    packageName, packageNsPrefix, packageNsURI));
        }

        // create superclass for package
        OntClass packageClass;
        if (superPackage == null) {
            packageClass = ontologyAccess.addSubClassOf(packageName, ePackageOntClass);
        } else {
            OntClass superPackageClass = ontologyAccess.addClass(superPackage.getName());
            packageClass = ontologyAccess.addSubClassOf(packageName, superPackageClass);
        }
        // annotate the nsURI
        ontologyAccess.addComment(packageClass, packageNsURI, NS_URI_COMMENT_LANGUAGE);

        // first iterate over the contents and process each EEnum + EPackage, afterwards iterate again for the EClasses
        MutableList<EObject> unprocessedObjects = Lists.mutable.empty();
        for (EObject eObject : ePackage.eContents()) {
            if (eObject instanceof EEnum) {
                processEEnum((EEnum) eObject);
            } else if (eObject instanceof EPackage) {
                processEPackage((EPackage) eObject);
            } else {
                unprocessedObjects.add(eObject);
            }
        }

        for (EObject eObject : unprocessedObjects) {
            if (eObject instanceof EClass) {
                processEClass((EClass) eObject);
            }
        }

        processedPackages.add(ePackage);
    }

    private void processEClass(EClass eClass) {
        OntClass addedClass;
        // TODO how to properly model, that abstract classes and interfaces can have no instances
        addedClass = ontologyAccess.addClass(eClass.getName());
        if (eClass.isAbstract()) {
            ontologyAccess.addComment(addedClass, ABSTRACT_CLASS, CLASS_TYPE);
        } else if (eClass.isInterface()) {
            ontologyAccess.addComment(addedClass, INTERFACE, CLASS_TYPE);
        } else {
        }
        ontologyAccess.addSubClassing(addedClass, eClassOntClass);

        for (EClass superClass : eClass.getESuperTypes()) {
            String superClassName = superClass.getName();
            if (superClassName == null || superClassName.equals("null") || superClassName.isEmpty()) {
                continue;
            }

            if (ontologyAccess.classIsSubClassOf(addedClass, eClassOntClass)) {
                ontologyAccess.removeSubClassing(addedClass, eClassOntClass);
            }

            OntClass superClassOnto;
            if (!ontologyAccess.containsClass(superClassName)) {
                superClassOnto = ontologyAccess.addSubClassOf(superClassName, eClassOntClass);
            } else {
                superClassOnto = ontologyAccess.addClass(superClassName);
            }
            ontologyAccess.addSubClassProperty(addedClass, superClassOnto);

            // annotate uri to superclass, this way loaded proxyClasses get properly annotated
            String nsURI = superClass.getEPackage()
                                     .getNsURI();
            ontologyAccess.addComment(superClassOnto, nsURI, NS_URI_COMMENT_LANGUAGE);
        }

        // annotate uri (nsUri) of package the class is in
        createPackageUriAnnotation(eClass, addedClass);

        processEClassFeatures(eClass);
    }

    private void processEClassFeatures(EClass eClass) {
        // eClass.getEAllStructuralFeatures()
        eClass.getEStructuralFeatures()
              .forEach(feature -> {
                  if (feature instanceof EAttribute) {
                      processEAttribute((EAttribute) feature);
                  } else if (feature instanceof EReference) {
                      processEReference((EReference) feature);
                  }
              });
    }

    private void processEAttribute(EAttribute eAttribute) {
        EClass domain = eAttribute.getEContainingClass();
        EDataType range = eAttribute.getEAttributeType();
        if (range instanceof EEnum) {
            processEAttributeEnum(eAttribute);
            return;
        }
        String propertyName = createAttributePropertyName(eAttribute, domain);

        int lowerBound = eAttribute.getLowerBound();
        int upperBound = eAttribute.getUpperBound();

        OntClass domainOntClass = ontologyAccess.addClass(domain.getName());
        Optional<org.apache.jena.rdf.model.Resource> rangeDatatype = ontologyAccess.getDatatypeByName(range.getName());
        OntProperty dataProperty;
        if (rangeDatatype.isPresent()) {
            dataProperty = ontologyAccess.addDataProperty(propertyName, domainOntClass, rangeDatatype.get());
        } else {
            logger.debug("Had a problem with the datatype " + range.getName() + " when processing range for "
                    + eAttribute.getName() + " in " + domain.getName() + ". Maybe it is a generic type.");
            dataProperty = ontologyAccess.addDataProperty(propertyName, domainOntClass);
        }

        if (lowerBound == upperBound && upperBound == 1) {
            dataProperty = ontologyAccess.addFunctionalToProperty(dataProperty);
        } else {
            if (lowerBound > 0) {
                ontologyAccess.addMinCardinalityToProperty(dataProperty, lowerBound);
            }
            if (upperBound > 0) {
                ontologyAccess.addMaxCardinalityToProperty(dataProperty, upperBound);
            }
        }

        createPackageUriAnnotation(domain, dataProperty);
    }

    private void processEAttributeEnum(EAttribute eAttribute) {
        if (!(eAttribute.getEAttributeType() instanceof EEnum)) {
            throw new IllegalArgumentException("Attribute must be an Enum");
        }
        EClass domain = eAttribute.getEContainingClass();
        OntClass domainClass = ontologyAccess.addClass(domain.getName());
        EEnum eEnum = (EEnum) eAttribute.getEAttributeType();
        OntClass enumClass = getEnumClass(eEnum.getName());

        int lowerBound = eAttribute.getLowerBound();
        int upperBound = eAttribute.getUpperBound();
        String propertyName = createAttributePropertyName(eAttribute, domain);
        OntProperty property = createEnumObjectProperty(domainClass, enumClass, propertyName, lowerBound, upperBound);

        AllValuesFromRestriction allValuesFrom = ontologyAccess.addAllValuesFrom(property, enumClass);
        OntClass clazz = ontologyAccess.addClass(eEnum.getName());
        ontologyAccess.addSubClassing(clazz, allValuesFrom);

        createPackageUriAnnotation(eEnum, clazz);
        createPackageUriAnnotation(eEnum, property);
    }

    private OntClass getEnumClass(String name) {
        if (name == null || name.equals("null") || name.isEmpty()) {
            throw new IllegalArgumentException("invalid name");
        }
        OntClass enumClass = createdEnums.get(name);
        if (enumClass == null) {
            enumClass = ontologyAccess.addClass(name);
        }
        return enumClass;
    }

    private OntProperty createEnumObjectProperty(OntClass domainClass, OntClass enumClass, String name, int lowerBound,
            int upperBound) {
        OntProperty objectProperty;
        if (lowerBound == 1 && upperBound == 1) {
            objectProperty = ontologyAccess.addFunctionalObjectProperty(name, domainClass, enumClass);
        } else {
            objectProperty = ontologyAccess.addObjectProperty(name, domainClass, enumClass);
        }
        return objectProperty;
    }

    private void processEReference(EReference eReference) {
        EClass domain = eReference.getEContainingClass();
        EClassifier range = eReference.getEType();
        Optional<OntClass> domainClassOpt = ontologyAccess.getClass(domain.getName());
        if (!domainClassOpt.isPresent()) {
            processEPackage(domain.getEPackage());
        }
        OntClass domainClass = ontologyAccess.addClass(domain.getName());
        String rangeName = range.getName();
        OntClass rangeClass;
        if (range.eIsProxy()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found a proxy class, meaning a needed package (meta-model) could not be resolved."
                        + " The result will probably incomplete!");
            }
            rangeName = getProxyClass(range);
        }
        if (ontologyAccess.containsClass(rangeName)) {
            rangeClass = ontologyAccess.addClass(rangeName);
        } else {
            rangeClass = ontologyAccess.addClass(rangeName);
            ontologyAccess.addSubClassOf(rangeName, eClassOntClass);
            createPackageUriAnnotation(range, rangeClass);
        }

        String propertyName = createReferencePropertyName(eReference, domain);
        int lowerBound = eReference.getLowerBound();
        int upperBound = eReference.getUpperBound();
        OntProperty property = createEnumObjectProperty(domainClass, rangeClass, propertyName, lowerBound, upperBound);
        createPackageUriAnnotation(domain, property);
    }

    private void processEEnum(EEnum eEnum) {
        String enumName = eEnum.getName();
        OntClass enumSuperClass = ontologyAccess.addClass(E_ENUM);
        EnumeratedClass currEnumClass = ontologyAccess.addEnumeratedClass(enumName);
        ontologyAccess.addSubClassing(currEnumClass, enumSuperClass);
        createdEnums.put(eEnum.getName(), currEnumClass);

        createPackageUriAnnotation(eEnum, currEnumClass);

        // create the literal property for the enum
        String literalPropertyName = createPropertyName(ENUM_LITERAL_PROPERTY_SUFFIX, E_ENUM);
        DatatypeProperty literalDataProperty = ontologyAccess.addDataProperty(literalPropertyName, enumSuperClass,
                XSD.xstring);
        // create the data property, that enums have a value (type int)
        String valuePropertyName = createPropertyName(ENUM_VALUE_PROPERTY_SUFFIX, E_ENUM);
        DatatypeProperty valueDataProperty = ontologyAccess.addDataProperty(valuePropertyName, enumSuperClass,
                XSD.integer);

        for (EEnumLiteral literal : eEnum.getELiterals()) {
            String literalString = literal.getLiteral();
            String name = getEEnumLiteralName(eEnum, literalString);
            Individual individual = ontologyAccess.addNamedIndividual(currEnumClass, name);
            ontologyAccess.addIndividualToEnumeratedClass(currEnumClass, individual);

            ontologyAccess.addDataPropertyToIndividual(individual, literalDataProperty, literalString);
            ontologyAccess.addDataPropertyToIndividual(individual, valueDataProperty, literal.getValue());
        }
    }

    private String getEEnumLiteralName(EEnum eEnum, String literal) {
        return getEEnumLiteralName(eEnum.getName(), literal);
    }

    private String getEEnumLiteralName(String eEnumName, String literal) {
        return eEnumName + "_" + literal;
    }

    private String getEObjectIdentifier(EObject object) {
        if (object == null) {
            throw new IllegalArgumentException("EObject is null");
        }
        String name = null;
        // because eObject has no proper hashCode(), we have to check for equality this way
        for (Entry<EObject, String> entry : eObjectNames.entrySet()) {
            EObject key = entry.getKey();
            if (EcoreUtil.equals(key, object)) {
                name = entry.getValue();
                break;
            }
        }
        if (name == null) {
            name = getId(object);
            eObjectNames.put(object, name);
        }
        String className = object.eClass()
                                 .getName();
        name = className + name;
        return cleanName(name);
    }

    private String getId(EObject object) {
        return EcoreUtil.getID(object);
    }

    private void processEObject(EObject object) {
        EClass clazz = object.eClass();
        String className = clazz.getName();
        String objectIdentifier = getEObjectIdentifier(object);
        checkClassExistence(clazz);
        ontologyAccess.addNamedIndividual(className, objectIdentifier);

        // add eObject to the set of processed eObjects already here, because of recursive nature of the loop below,
        // that might end in a loop trying to process this object over and over again
        processedEObjects.add(object);

        // process the references and attributes (and other features)
        List<EStructuralFeature> features = clazz.getEAllStructuralFeatures()
                                                 .stream()
                                                 .filter(object::eIsSet)
                                                 .collect(Collectors.toList());
        for (EStructuralFeature feature : features) {
            Object featureObject = object.eGet(feature);
            if (featureObject == null) {
                logger.warn("Feature is null although it should be present: " + feature.toString());
                continue;
            }
            if (feature instanceof EReference) {
                processEReferenceFeature(featureObject, feature, object);
            } else if (feature instanceof EAttribute) {
                EAttribute attribute = (EAttribute) feature;
                if (attribute.getEAttributeType() instanceof EEnum) {
                    processEEnumFeature(featureObject, attribute, object);
                } else {
                    processEAttributeFeature(featureObject, attribute, object);
                }
            }
        }
    }

    private void processEReferenceFeature(Object featureObject, EStructuralFeature feature, EObject containerEObject) {
        EReference reference = (EReference) feature;
        if (featureObject instanceof EObject) {
            processEObjectFeature(featureObject, reference, containerEObject);
        } else if (featureObject instanceof EList<?>) {
            @SuppressWarnings("rawtypes")
            EList featureObjectList = (EList) featureObject;
            for (Object currObject : featureObjectList) {
                if (currObject instanceof EObject) {
                    processEObjectFeature(currObject, reference, containerEObject);
                } else {
                    logger.debug("Object in feature list is no EObject");
                }
            }
        }
    }

    private void processEEnumFeature(Object featureObject, EAttribute attribute, EObject containerEObject) {
        String objectIdentifier = getEObjectIdentifier(containerEObject);
        String attributePropertyName = createAttributePropertyName(attribute, attribute.getEContainingClass());

        String eEnumName = attribute.getEAttributeType()
                                    .getName();
        String featureObjectIdentifier = getEEnumLiteralName(eEnumName, featureObject.toString());

        Optional<ObjectProperty> property = ontologyAccess.addObjectPropertyOfIndividual(objectIdentifier,
                attributePropertyName, featureObjectIdentifier);
        if (property.isPresent()) {
            createPackageUriAnnotation(attribute.getEContainingClass(), property.get());
        }
    }

    private void processEAttributeFeature(Object featureObject, EAttribute attribute, EObject containerEObject) {
        EClass domain = attribute.getEContainingClass();
        String attributePropertyName = createAttributePropertyName(attribute, domain);
        String featureObjectSimpleClassName = featureObject.getClass()
                                                           .getSimpleName();
        String objectIdentifier = getEObjectIdentifier(containerEObject);
        Optional<Individual> optIndividual = ontologyAccess.getNamedIndividualByShortUri(objectIdentifier);
        if (!optIndividual.isPresent()) {
            String msg = "Could not find individual \"" + objectIdentifier + "\" while processing attribute features.";
            logger.warn(msg);
            return;
        }

        Optional<org.apache.jena.rdf.model.Resource> datatype = ontologyAccess.getDatatypeByName(
                featureObjectSimpleClassName);
        if (datatype.isPresent()) {
            Optional<DatatypeProperty> optDataProperty = ontologyAccess.getDataProperty(attributePropertyName);
            if (!optDataProperty.isPresent()) {
                // if data property didn't exist before, then process the containing class (again)
                // (happens with proxy classes)
                processEClass(attribute.getEContainingClass());
                optDataProperty = ontologyAccess.getDataProperty(attributePropertyName);
            }
            ontologyAccess.addDataPropertyToIndividual(optIndividual.get(), optDataProperty.orElseThrow(),
                    featureObject);

            if ("name".equals(attribute.getName()) || "entityName".equals(attribute.getName())) {
                // annotate name
                String name = featureObject.toString();
                ontologyAccess.addLabel(optIndividual.get(), name);
            } else if ("id".equals(attribute.getName())) {
                String id = featureObject.toString();
                ontologyAccess.addComment(optIndividual.get(), id, "id");
            }
        } else {
            String msg = "Had a problem with the datatype " + featureObjectSimpleClassName
                    + " when processing attribute " + attribute.getName();
            logger.debug(msg);
        }
    }

    private void processEObjectFeature(Object featureObject, EReference reference, EObject containerEObject) {
        String referencePropertyName = createReferencePropertyName(reference, reference.getEContainingClass());
        String containerName = getEObjectIdentifier(containerEObject);
        EObject featureEObject = (EObject) featureObject;
        String featureIdentifier = getEObjectIdentifier(featureEObject);

        checkClassExistence(containerEObject.eClass());
        if (!ontologyAccess.containsObjectProperty(referencePropertyName)) {
            processEClass(reference.getEContainingClass());
        }

        if (ontologyAccess.containsObjectPropertyForIndividuals(containerName, referencePropertyName,
                featureIdentifier)) {
            return;
        }

        Optional<ObjectProperty> property = ontologyAccess.addObjectPropertyOfIndividual(containerName,
                referencePropertyName, featureIdentifier);
        if (property.isPresent()) {
            createPackageUriAnnotation(reference.getEContainingClass(), property.get());
        }

        if (!processedEObjects.contains(featureEObject)) {
            processEObject(featureEObject);
        }

    }

    private void checkClassExistence(EClass clazz) {
        if (!ontologyAccess.containsClass(clazz.getName())) {
            // when the referenced object is a proxy (external), then the class and further info might not exist.
            // process the ePackage of the class to get needed information into the ontology
            processEPackage(clazz.getEPackage());
        }
    }

    private void createPackageUriAnnotation(EClassifier eClassifier, OntResource resource) {
        String nsUri = eClassifier.getEPackage()
                                  .getNsURI();
        ontologyAccess.addComment(resource, nsUri, NS_URI_COMMENT_LANGUAGE);
    }

    // helper methods for proxy classes. Usually they should not exist, but in some occasions they might come up.
    private String getProxyClass(URI proxyUri) {
        String proxy = proxyUri.fragment()
                               .replaceFirst("//", "");
        String proxyUriString = proxyUri.toString();
        if (logger.isDebugEnabled()) {
            String proxyDebug = String.format("Having Proxy-Class \"%s\" with URI \"%s\"", proxy, proxyUriString);
            logger.debug(proxyDebug);
        }
        ontologyAccess.addSubClassOf(PROXY_SUPER_CLASS, E_CLASS);
        OntClass proxyClass = ontologyAccess.addSubClassOf(proxy, PROXY_SUPER_CLASS);
        ontologyAccess.addComment(proxyClass, proxyUriString, NS_URI_COMMENT_LANGUAGE);

        return proxy;
    }

    private String getProxyClass(EObject proxyClazz) {
        URI proxyUri = EcoreUtil.getURI(proxyClazz);
        return getProxyClass(proxyUri);
    }

    private static String cleanName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Argument is null");
        }
        String cleanedName = name.replaceAll("\"", "");
        cleanedName = cleanedName.replaceAll("\'", "");
        cleanedName = cleanedName.replaceAll("<", "\\<");
        cleanedName = cleanedName.replaceAll(">", "\\>");
        return cleanedName;
    }

    public static String createPropertyName(String property, String domainName) {
        return cleanName(property + PROPERTY_TO_CLASS_SEPARATOR + domainName);
    }

    public static String createAttributePropertyName(EAttribute eAttribute, EClass domain) {
        return createPropertyName(eAttribute.getName(), domain.getName());
    }

    public static String createReferencePropertyName(EReference eReference, EClass domain) {
        return createPropertyName(eReference.getName(), domain.getName());
    }
}
