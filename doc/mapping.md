# Ecore2OWL Mappings

This document should document the pragmatics behind the mappings between Ecore and OWL.
Currently, there are 3 direct subclasses of `owl:Thing`: `EClass`, `EEnum`, `EPackage`. These stand for the corresponding types in Ecore.

## Package-level:
| Ecore | Transformation to OWL |
| --- | --- |
|`ePackage`|subclass of EPackage, Comment (language "nsuri") for reference to package in classes etc.|

## Class-level
| Ecore | Transformation to OWL |
| --- | --- |
| `eClass`  	| subclass of EClass
| Inheritance	| subclass of inherited class
| abstract 	| Comment "abstract" in language "classType"
| interface 	| Comment "interface" in language "classType"
| Attribute 	| DataProperty, set domain and range (according to type), and cardinality
| Reference 	| ObjectProperty, set domain and range (accordingly) as well as cardinality
| `EEnum` 		| Create the Enum-Class as subclass of EEnum. OneOf for the members (that are Individuals of the created enum-class). Also for the values of the Individuals create DataProperties for the value (Int) and for the literal (String)


## Object-level
| Ecore | Transformation to OWL |
| --- | --- |
| `eObject`		| NamedIndividual; Identifier is a UUID used for the URI
| Attribute	| create typed literal for the individual. If the attribute is a "name", then a label is added. If the attribute is an ID, then a comment in the language "id" is created accordingly.
| Reference 	| create Statement with the corresponding subject, property (reference), and object
| Lists 		| Treat each entry like a reference/attribute



## Further notes
Currently, names are set via rdfs:label, the URI is a UUID. However, the URI of classes are created using the name of the class. For enum literals, a pattern with the name of the enum and the literal is used: `ENUMNAME_LITERAL`.

On some occasions, a comment in a special language was used. This is mostly preliminary as a proper modeling of said properties was not necessary yet.