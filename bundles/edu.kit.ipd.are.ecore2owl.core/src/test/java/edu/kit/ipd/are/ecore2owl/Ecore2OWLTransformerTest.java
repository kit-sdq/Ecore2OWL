package edu.kit.ipd.are.ecore2owl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class Ecore2OWLTransformerTest {
    private static String testOntologyOutPath = "src/test/resources/ms_base.owl";
    private static Ecore2OWLTransformer transformer;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        transformer = new Ecore2OWLTransformer(testOntologyOutPath);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        transformer = null;
    }

    @Ignore
    @Test
    public void testTransformationPCM() {
        // TODO Problem: need registered resource factory

        String[] modelIn = { "./resources/ms_base/ms_base_usage_all.usagemodel",
                "./resources/ms_base/ms_base.allocation", "./resources/ms_base/ms_base.system",
                "./resources/ms_base/ms_base.repository", "./resources/ms_base/ms_base.resourceenvironment" };

        for (String modelInput : modelIn) {
            if (!modelInput.isEmpty()) {
                transformer.transformModel(modelInput, true);
            }
        }

        transformer.saveOntology();
    }

}
