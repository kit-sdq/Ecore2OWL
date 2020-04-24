# Ecore2OWL
[![Travis Build Status](https://travis-ci.org/kit-sdq/Ecore2OWL.svg?branch=master)](https://travis-ci.org/kit-sdq/Ecore2OWL) 
![Java CI with Maven](https://github.com/kit-sdq/Ecore2OWL/workflows/Java%20CI%20with%20Maven/badge.svg?branch=master)
![Nightly Build](https://github.com/kit-sdq/Ecore2OWL/workflows/Nightly%20Build/badge.svg)

Tool to transform Ecore (Meta-)Models into OWL Ontologies.
The ontologies will be saved in a RDF/XML format.

## Installation
Currently, this is still work in progress and no dedicated installation besides the development option is available.
We plan to provide an updatesite to directly install it in eclipse. More information about that will be available as soon as a mature version can be released.

## How to use
When the tool is installed, you can access it via the run configuration menu (`Run`->`Run Configuration`). In there, there is the Ecore2OWL Application.
Create a (new) configuration for it and add the necessary information about the (ecore) model-files and where the output should be saved to.
You have two options: First, the meta-models will be loaded from your installed meta-models in the eclipse instance. Second, you can select the meta-models that shall be used for the resource resolution.
This step is needed to figure out the corresponding meta-models for your models.

You can select multiple models that are all processed and transformed into a single OWL-file.

## Development
Building this project can simply be done with the following command utilizing maven tycho to build the eclipse plugins:
```
mvn clean package
```

For development, check out this repo and import all projects into your eclipse workspace via `Import`->`Existing Projects into Workspace` and then selecting the folder where you checked out this repository. Make sure to tick the option to `Search for nested projects`.
With the projects in your workspace, you can test the plugins by starting a "inner eclipse" that also loads the plugins in development within your workspace. For this, add a run configuration (`Run`->`Run Configuration`) as Eclipse Application. Make sure that the execution environment is properly set und that the it runs a product (not an application). Also, it is usually best to run with all plug-ins, but you can also only select the plug-ins that you actually want and need.

Here is an overview of the different projects and what they do:
* bundles/edu.kit.ipd.are.ecore2owl.dependencies-collector: used to incorporate non-osgi-dependencies into the osgi-environment. This projects simply collects the dependencies via maven and generates a .jar that is put into the dependencies provider
* bundles/edu.kit.ipd.are.ecore2owl.dependencies-provider: used to incorporate non-osgi-dependencies into the osgi-environment. Loads the .jar (that is put there via the dep. collector during build) and provides access to all the dependencies for osgi-projects/plugins
* bundles/edu.kit.ipd.are.OntologyAccess: Project to provide a layer between the core logic and the ontology creation (via Apache Jena). This Layer simplifies and bundles the needed calls to the ontology.
* bundles/edu.kit.ipd.are.ecore2owl.core: Core logic for the transformation.
* bundles/edu.kit.ipd.are.ecore2owl.ui: Code for the integration into the eclipse UI (as run configuration).
* features/* : Feature definitions
* releng/* : Release Engineering including configuration for the updatesite as well as a (basic) configuration, i.e., parent-pom
* tests/* : Contains the tests
