package edu.kit.ipd.are.ecore2owl.core;

enum MetaModel {

    PCM("https://informalin.github.io/knowledgebases/informalin_base_pcm.owl#", "pcm", "pcm"),
    // PCM("https://informalin.github.io/knowledgebases/informalin_base_ecore.owl#", "ecore", "pcm"),
    ECORE("https://informalin.github.io/knowledgebases/informalin_base_ecore.owl#", "ecore", "ecore");

    private final String iri;
    private final String nsPrefix;
    private final String name;

    MetaModel(String iri, String nsPrefix, String name) {
        this.iri = iri;
        this.nsPrefix = nsPrefix;
        this.name = name;
    }

    static MetaModel getMetaModelByName(String name) {
        for (var mm : MetaModel.values()) {
            if (mm.getName().equals(name)) {
                return mm;
            }
        }

        // default: ECORE
        return ECORE;
    }

    public String getIri() {
        return iri;
    }

    public String getNsPrefix() {
        return nsPrefix;
    }

    public String getName() {
        return name;
    }

}
