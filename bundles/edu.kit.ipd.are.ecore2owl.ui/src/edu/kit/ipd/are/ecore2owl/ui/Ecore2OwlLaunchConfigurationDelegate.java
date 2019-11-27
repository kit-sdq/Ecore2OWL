package edu.kit.ipd.are.ecore2owl.ui;

import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

import edu.kit.ipd.are.ecore2owl.Ecore2OWLTransformer;
import edu.kit.ipd.are.ontologyaccess.OntologyAccess;

public class Ecore2OwlLaunchConfigurationDelegate extends LaunchConfigurationDelegate {
    private static Logger logger = Logger.getLogger(Ecore2OwlLaunchConfigurationDelegate.class);

    private String resolveFileURL(String url) {
        String resolvedURL = "";
        if (urlIsPlatformURL(url)) {
            try {
                resolvedURL = FileLocator.resolve(new URL(url))
                                         .getFile();
            } catch (IOException e) {
                logger.warn(e.getMessage(), e.getCause());
            }
        } else {
            resolvedURL = url;
        }

        return resolvedURL;
    }

    private boolean urlIsPlatformURL(String url) {
        return url.startsWith("platform:");
    }

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {
        String owlFile = getOwlOut(configuration);
        boolean modelToOntologyNeeded = configuration.getAttribute(Ecore2OwlConfigurationAttributes.LOAD_FROM_MODEL, false);

        // create ontology from models, if needed
        OntologyAccess ontAcc = null;
        if (modelToOntologyNeeded) {
            ontAcc = transformModelToOntology(configuration, owlFile);
        }
        logger.info("Finished transformation of input models.");
    }

    private OntologyAccess transformModelToOntology(ILaunchConfiguration configuration, String owlFile)
            throws CoreException {
        boolean autoLoadMetaModel = configuration.getAttribute(Ecore2OwlConfigurationAttributes.AUTOLOAD, false);
        String[] modelIn = getInput(configuration, Ecore2OwlConfigurationAttributes.MODEL_IN);

        Ecore2OWLTransformer transformer = new Ecore2OWLTransformer(owlFile);
        if (!autoLoadMetaModel) {
            logger.info("Start loading and transforming meta models.");
            String[] ecoreIn = getInput(configuration, Ecore2OwlConfigurationAttributes.ECORE_IN);
            for (String ecoreInput : ecoreIn) {
                if (!ecoreInput.isEmpty()) {
                    transformer.transformEcore(ecoreInput);
                }
            }
        }

        logger.info("Start transforming models.");
        for (String modelInput : modelIn) {
            if (!modelInput.isEmpty()) {
                logger.debug("Processing model input: " + modelInput);
                transformer.transformModel(modelInput, autoLoadMetaModel);
            }
        }
        return transformer.getOntologyAccess();
    }

    private String[] getInput(ILaunchConfiguration configuration, String attribute) {
        String input;
        try {
            input = configuration.getAttribute(attribute, "");
        } catch (CoreException e) {
            logger.warn(e.getMessage(), e.getCause());
            return new String[0];
        }
        return input.split(";");
    }

    private String getOwlOut(ILaunchConfiguration configuration) {
        String[] out = getInput(configuration, Ecore2OwlConfigurationAttributes.OWL_OUT);
        if (out.length != 1) {
            throw new IllegalArgumentException("Invalid output file!");
        }
        return resolveFileURL(out[0]);
    }
}
