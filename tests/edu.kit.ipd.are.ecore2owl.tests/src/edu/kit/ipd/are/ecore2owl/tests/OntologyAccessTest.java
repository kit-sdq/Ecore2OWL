package edu.kit.ipd.are.ecore2owl.tests;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import edu.kit.ipd.are.ecore2owl.ontology.OntologyAccess;

public class OntologyAccessTest {
    private static final String DEFAULT_NAMESPACE = "https://informalin.github.io/knowledgebases/examples/ontology.owl#";
    private static final String ECORE_ONTOLOGY_IRI = "https://informalin.github.io/knowledgebases/informalin_base_ecore.owl#";
    private static final String ECLASS_IRI = "ecore:OWLClass_EClass";
    private static final String EPACKAGE_IRI = "ecore:OWLClass_EPackage";

    private static final String DEFAULT_PREFIX = "model";
    private OntologyAccess ontologyAccess = null;

    @BeforeAll
    void setup() {
        ontologyAccess = OntologyAccess.empty(DEFAULT_NAMESPACE);
        ontologyAccess.addNsPrefix(DEFAULT_PREFIX, DEFAULT_NAMESPACE);
        ontologyAccess.setDefaultPrefix(DEFAULT_PREFIX);

        ontologyAccess.addOntologyImport(ECORE_ONTOLOGY_IRI);
        ontologyAccess.addNsPrefix("ecore", ECORE_ONTOLOGY_IRI);

        // TODO FIXME
        // Optional<OntClass> optEPackage = oa.getClassByIri(EPACKAGE_IRI);
        // if (optEPackage.isEmpty()) {
        // logger.warn("Could not find EPackage in ontology. Creating of ontology failed.");
        // return null;
        // } else {
        // ePackageOntClass = optEPackage.get();
        // }
        //
        // Optional<OntClass> optEClass = oa.getClassByIri(ECLASS_IRI);
        // if (optEClass.isEmpty()) {
        // logger.warn("Could not find EClass in ontology. Creating of ontology failed.");
        // return null;
        // } else {
        // eClassOntClass = optEClass.get();
        // }
        // eClassOntClass = ontologyAccess.addClassByIri("ecore:OWLClass_EClass"); // TODO
        // ePackageOntClass = ontologyAccess.addClass(E_PACKAGE);

    }

    @Test
    public void importTest() {
        // TODO
    }
}
