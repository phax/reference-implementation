eDelivery xsd schemas v0.5
===

This contains edelivery xsd schema proposals for edelivery message payloads in 
Gate to Gate communication (v0.5).

- **edelivery.xsd** - edelivery message payloads in gate to gate communication:
  - identifier-query / identifier-response - find consignments (identifier subset only) matching the given identifier
  - uil-query / uil-response - find consignment (in a given subset) using uil
- **consignment-common.xsd** - efti common dataset consignment
- **consignment-identifier.xsd** - identifier subset of efti common dataset consignment.
- **consignment-dummy.xsd** - dummy efti common dataset consignment (same as identifier schema, used only for development purposes)

Model
-
Data model is based on 
- https://unece.org/trade/uncefact/unccl and
- https://svn.gefeg.com/svn/efti-publication/Draft/CDS/ds1.htm

Efti common dataset and identifier subset are both based on this same model.

Data model is visualized here:
- common dataset: https://model.fintraffic-efti-poc.aws.fintraffic.cloud/#efti
- identifier subset: https://model.fintraffic-efti-poc.aws.fintraffic.cloud/#identifier

Element names are derived from un/ccl as:
- use 82th column (Short Name) from un/ccl 
- remove invalid characters -, _, /
- first character in lower case
- rename iD -> id

Complex type names are derived from un/ccl as:
- concat columns 8 (Object Class Term Qualifiers) and 9 (Object Class Term)
- remove invalid characters -, _, /

Element max cardinality is derived from un/ccl as (name : type):
- id : Identifier -> 1
- code : CountryIdentifier -> 1
- else: use column 21 (Occurrence Max)

Examples 
-  
XML example documents:

- consignment.xml - example consignment xml document (identifier subset of efti common dataset)
- identifier-query - example payload for identifiers search request to other gate
- identifier-response - example payload for identifiers search response to other gate
- uil-query - example payload for uil query request to other gate
- uil-response - example payload for uil query response to other gate