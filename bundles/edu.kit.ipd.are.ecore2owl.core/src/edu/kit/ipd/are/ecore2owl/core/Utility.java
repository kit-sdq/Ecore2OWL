package edu.kit.ipd.are.ecore2owl.core;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLResource;

public final class Utility {

    private Utility() {
        throw new IllegalAccessError();
    }

    static EPackage getHighestSuperEPackage(EPackage ePackage) {
        var superEPackage = ePackage.getESuperPackage();
        while (superEPackage != null) {
            superEPackage = ePackage.getESuperPackage();
            if (superEPackage == null || superEPackage.equals(ePackage)) {
                break;
            }
            ePackage = superEPackage;
        }
        return ePackage;
    }

    static boolean needsEPackageProcessing(EPackage ePackage) {
        var superPackage = getHighestSuperEPackage(ePackage);
        var mm = MetaModel.getMetaModelByName(superPackage.getName());
        return mm.equals(MetaModel.ECORE);
    }

    static String getNamespace(EPackage ePackage) {
        var superPackage = getHighestSuperEPackage(ePackage);
        var mm = MetaModel.getMetaModelByName(superPackage.getName());
        return mm.getNsPrefix();
    }

    static String getNamespace(EClassifier eClassifier) {
        var ePackage = eClassifier.getEPackage();
        return getNamespace(ePackage);
    }

    static String getNamespace(EObject eObject) {
        var clazz = eObject.eClass();
        return getNamespace(clazz);
    }

    static String cleanName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Argument is null");
        }
        String cleanedName = name.replace("\"", "");
        cleanedName = cleanedName.replace("\'", "");
        cleanedName = cleanedName.replace("<", "\\<");
        cleanedName = cleanedName.replace(">", "\\>");
        return cleanedName;
    }

    static String createPropertyName(String property, String domainName) {
        return cleanName(property + Ecore2OWLTransformer.PROPERTY_TO_CLASS_SEPARATOR + domainName);
    }

    static String createAttributePropertyName(EAttribute eAttribute, EClass domain) {
        return createPropertyName(eAttribute.getName(), domain.getName());
    }

    static String createReferencePropertyName(EReference eReference, EClass domain) {
        return createPropertyName(eReference.getName(), domain.getName());
    }

    /**
     * Register a meta-model presented in a ecore-file to the Package-Registry.
     *
     * @param ecoreFileUrl Path to the ecore-file representing the meta-model
     */
    public static void registerEcoreFile(String ecoreFileUrl) {
        // create URI
        URI modelUri;
        if (ecoreFileUrl.startsWith("platform:")) {
            // input is no file but a platform-resource, so just use URI
            modelUri = URI.createURI(ecoreFileUrl);
        } else {
            // input is a file and should be loaded from a file
            modelUri = URI.createFileURI(ecoreFileUrl);
        }

        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new PerformantXMIResourceFactoryImpl());

        ResourceSet resourceSet = new ResourceSetImpl();
        // enable extended metadata
        final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(EPackage.Registry.INSTANCE);
        resourceSet.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, extendedMetaData);

        var resource = resourceSet.getResource(modelUri, true);
        var eObject = resource.getContents().get(0);
        if (eObject instanceof EPackage) {
            EPackage p = (EPackage) eObject;
            EPackage.Registry.INSTANCE.put(p.getNsURI(), p);
        }
    }

}
