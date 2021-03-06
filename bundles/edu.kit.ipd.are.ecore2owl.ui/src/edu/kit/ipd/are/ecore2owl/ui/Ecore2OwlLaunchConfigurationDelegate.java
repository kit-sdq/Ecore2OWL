package edu.kit.ipd.are.ecore2owl.ui;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

import edu.kit.ipd.are.ecore2owl.core.Ecore2OWLTransformer;
import edu.kit.ipd.are.ecore2owl.core.Utility;

public class Ecore2OwlLaunchConfigurationDelegate extends LaunchConfigurationDelegate {
    private static Logger logger = Logger.getLogger(Ecore2OwlLaunchConfigurationDelegate.class);

    private String resolveFileURL(String url) {
        var resolvedURL = "";
        if (urlIsPlatformURL(url)) {
            try {
                resolvedURL = FileLocator.resolve(new URL(url)).getFile();
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
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
        String owlFile = getOwlOut(configuration);

        var start = Instant.now();
        transformModelToOntology(configuration, owlFile);
        var end = Instant.now();
        logger.info("Finished.");
        try {
            long duration = Duration.between(start, end).toMillis();
            logger.debug("Execution time (in ms): " + duration);
        } catch (ArithmeticException e) {
            long duration = Duration.between(start, end).toSeconds();
            logger.debug("Execution time (in seconds): " + duration);
        }

    }

    private void transformModelToOntology(ILaunchConfiguration configuration, String owlFile) throws CoreException {
        if (owlFile == null || owlFile.isEmpty()) {
            throw new IllegalArgumentException("Invalid output file!");
        }

        boolean autoLoadMetaModel = configuration.getAttribute(Ecore2OwlConfigurationAttributes.AUTOLOAD, false);
        String[] modelIn = getInput(configuration, Ecore2OwlConfigurationAttributes.MODEL_IN);

        var transformer = new Ecore2OWLTransformer();
        if (!autoLoadMetaModel) {
            logger.info("Start loading meta models.");
            String[] ecoreIn = getInput(configuration, Ecore2OwlConfigurationAttributes.ECORE_IN);
            for (String ecoreInput : ecoreIn) {
                if (!ecoreInput.isEmpty()) {
                    Utility.registerEcoreFile(ecoreInput);
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
        logger.info("Finished transformation of input models.");
        logger.info("Start saving the OWL file to " + owlFile);
        transformer.saveOntology(owlFile);
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
