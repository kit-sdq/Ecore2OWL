package edu.kit.ipd.are.ecore2owl.ui;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

import edu.kit.ipd.are.ecore2owl.core.Ecore2OWLTransformer;
import edu.kit.ipd.are.ecore2owl.ontologyaccess.OntologyAccess;

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

        transformModelToOntology(configuration, owlFile);
        logger.info("Finished.");
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
                    Resource ecoreResource = loadEcoreResource(ecoreInput);
                    transformer.transformEcore(ecoreResource);
                }
            }
        }

        logger.info("Start transforming models.");
        for (String modelInput : modelIn) {
            if (!modelInput.isEmpty()) {
                logger.debug("Processing model input: " + modelInput);
                Resource ecoreResource = loadEcoreResource(modelInput);
                transformer.transformModel(ecoreResource, autoLoadMetaModel);
            }
        }
        logger.info("Finished transformation of input models.");
        logger.info("Start saving the OWL file.");
        transformer.saveOntology();
        return transformer.getOntologyAccess();
    }

    /**
     * Loads an Ecore {@link Resource} from the as {@link String} given URL and returns the loaded {@link Resource}
     *
     * @param inputResourceUrl
     *            URL of the input resource, that should be loaded
     * @return loaded Resource
     */
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
