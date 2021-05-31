package edu.kit.ipd.are.ecore2owl.core;

enum MetaModel {

    // PCM("https://informalin.github.io/knowledgebases/informalin_base_pcm.owl#", "pcm"),
    PCM("https://informalin.github.io/knowledgebases/informalin_base_ecore.owl#", "ecore"),
    ECORE("https://informalin.github.io/knowledgebases/informalin_base_ecore.owl#", "ecore");

    private final String iri;
    private final String nsPrefix;

    MetaModel(String iri, String nsPrefix) {
        this.iri = iri;
        this.nsPrefix = nsPrefix;
    }

    static MetaModel getMetaModelByName(String name) {
        switch (name) {
        case "pcm":
            return PCM;
        case "ecore":
            return ECORE;
        default:
            return ECORE;
        }
    }

    public String getIri() {
        return iri;
    }

    public String getNsPrefix() {
        return nsPrefix;
    }

}
