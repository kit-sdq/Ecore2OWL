package edu.kit.ipd.are.ecore2owl.core;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLParserPool;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.URIHandlerImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLParserPoolImpl;

public class PerformantXMIResourceFactoryImpl extends ResourceFactoryImpl {
    private List<Object> lookupTableSaving = Lists.mutable.empty();
    private List<Object> lookupTableLoading = Lists.mutable.empty();

    private XMLParserPool parserPool = new XMLParserPoolImpl();

    @Override
    public Resource createResource(URI uri) {
        XMIResource resource;
        resource = new XMIResourceImpl(uri) {
            @Override
            protected boolean useIDs() {
                return false;
            }
        };

        // configure resource
        Map<Object, Object> saveOptions = resource.getDefaultSaveOptions();
        saveOptions.put(XMLResource.OPTION_CONFIGURATION_CACHE, Boolean.TRUE);
        saveOptions.put(XMLResource.OPTION_USE_CACHED_LOOKUP_TABLE, lookupTableSaving);
        saveOptions.put(XMLResource.OPTION_USE_XML_NAME_TO_FEATURE_MAP, lookupTableLoading);
        saveOptions.put(XMLResource.OPTION_ENCODING, "UTF-8");
        saveOptions.put(XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE, Boolean.TRUE);
        saveOptions.put(XMLResource.OPTION_LINE_WIDTH, 80);
        saveOptions.put(XMLResource.OPTION_URI_HANDLER, new URIHandlerImpl.PlatformSchemeAware());
        // saveOptions.put(XMLResource.OPTION_USE_FILE_BUFFER, Boolean.TRUE);

        Map<Object, Object> loadOptions = resource.getDefaultLoadOptions();
        loadOptions.put(XMLResource.OPTION_DEFER_ATTACHMENT, Boolean.TRUE);
        loadOptions.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
        loadOptions.put(XMLResource.OPTION_USE_DEPRECATED_METHODS, Boolean.FALSE);
        loadOptions.put(XMLResource.OPTION_USE_PARSER_POOL, parserPool);

        if ("genmodel".equals(uri.fileExtension())) {
            loadOptions.put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, Boolean.TRUE);
        }

        return resource;
    }

}
