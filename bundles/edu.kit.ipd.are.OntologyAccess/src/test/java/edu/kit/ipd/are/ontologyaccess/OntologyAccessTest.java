/**
 * 
 */
package edu.kit.ipd.are.ontologyaccess;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.jena.ontology.Individual;
import org.eclipse.collections.api.list.MutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Jan Keim
 *
 */
public class OntologyAccessTest {

    private static String testOntologyPath = "src/test/resources/ms_base.owl";
    private static OntologyAccess ontologyAccess;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ontologyAccess = OntologyAccess.ofFile(testOntologyPath);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        ontologyAccess = null;
    }

    /**
     * Test method for
     * {@link edu.kit.ipd.are.ontologyaccess.OntologyAccess#searchIndividual(java.util.function.Predicate)}.
     */
    @Test
    public void testSearchIndividual() {
        List<String> searchNames = Arrays.asList("defaultSystem", "Server2",
                "Assembly_TagWatermarking <TagWatermarking>");

        for (String searchName : searchNames) {

            MutableList<Individual> foundIndividuals = ontologyAccess.searchIndividual(getSearchIndividual(searchName));

            assertTrue("Need to find at least one individual", foundIndividuals.size() > 0);

            boolean found = false;
            for (Individual individual : foundIndividuals) {
                String label = individual.getLabel(null);
                if (searchName.equals(label)) {
                    found = true;
                }
            }
            assertTrue("Individual need to be found", found);
        }
    }

    /**
     * Test method for
     * {@link edu.kit.ipd.are.ontologyaccess.OntologyAccess#searchIndividual(java.util.function.Predicate)}.
     */
    @Test
    public void testSearchIndividualNonExistent() {

        List<String> searchNames = Arrays.asList("-=NonExistentIndividualName=-", "-=NonExistentIndividualName=-",
                "-=NonExistentIndividualName=-");

        for (String searchName : searchNames) {
            MutableList<Individual> foundIndividuals = ontologyAccess.searchIndividual(getSearchIndividual(searchName));

            assertTrue("No individual has to be found", foundIndividuals.size() == 0);
        }
    }

    private Predicate<Individual> getSearchIndividual(String searchName) {
        return (Individual i) -> {
            Optional<String> optName = ontologyAccess.getName(i.getURI());
            if (optName.isPresent()) {
                return OntologyAccess.compareNamesSimple(optName.get(), searchName);
            }
            return false;
        };
    }
}
