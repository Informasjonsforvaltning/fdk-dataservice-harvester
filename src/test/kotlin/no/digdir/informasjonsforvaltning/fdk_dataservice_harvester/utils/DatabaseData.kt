package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils

val DATASERVICE_META_0 = """
<http://localhost:5000/dataservices/ea51178e-f843-3025-98c5-7d02ce887f90>
        a       <http://www.w3.org/ns/dcat#CatalogRecord> ;
        <http://purl.org/dc/terms/identifier>
                "ea51178e-f843-3025-98c5-7d02ce887f90" ;
        <http://purl.org/dc/terms/issued>
                "2020-03-12T11:52:16.122Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;
        <http://purl.org/dc/terms/modified>
                "2020-03-12T11:52:16.122Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;
        <http://purl.org/dc/terms/isPartOf>
                <http://localhost:5000/catalogs/e422e2a7-287f-349f-876a-dc3541676f21> ;
        <http://xmlns.com/foaf/0.1/primaryTopic>
                <https://testdirektoratet.no/model/dataservice/0> .
""".trim()

val DATASERVICE_META_1 = """
<http://localhost:5000/dataservices/4d69ecde-f1e8-3f28-8565-360746e8b5ef>
        a       <http://www.w3.org/ns/dcat#CatalogRecord> ;
        <http://purl.org/dc/terms/identifier>
                "4d69ecde-f1e8-3f28-8565-360746e8b5ef" ;
        <http://purl.org/dc/terms/issued>
                "2020-03-12T11:52:16.122Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;
        <http://purl.org/dc/terms/modified>
                "2020-03-12T11:52:16.122Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;
        <http://purl.org/dc/terms/isPartOf>
                <http://localhost:5000/catalogs/65555cdb-6809-3cc4-bff1-aaa6d9426311> ;
        <http://xmlns.com/foaf/0.1/primaryTopic>
                <https://testdirektoratet.no/model/dataservice/1> .
""".trim()

val CATALOG_META_0 = """
<http://localhost:5000/catalogs/e422e2a7-287f-349f-876a-dc3541676f21>
        a       <http://www.w3.org/ns/dcat#CatalogRecord> ;
        <http://purl.org/dc/terms/identifier>
                "e422e2a7-287f-349f-876a-dc3541676f21" ;
        <http://purl.org/dc/terms/issued>
                "2020-03-12T11:52:16.122Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;
        <http://purl.org/dc/terms/modified>
                "2020-03-12T11:52:16.122Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;
        <http://xmlns.com/foaf/0.1/primaryTopic>
                <https://testdirektoratet.no/model/dataservice-catalogs/0> .
""".trim()

val CATALOG_META_1 = """
<http://localhost:5000/catalogs/65555cdb-6809-3cc4-bff1-aaa6d9426311>
        a       <http://www.w3.org/ns/dcat#CatalogRecord> ;
        <http://purl.org/dc/terms/identifier>
                "65555cdb-6809-3cc4-bff1-aaa6d9426311" ;
        <http://purl.org/dc/terms/issued>
                "2020-03-12T11:52:16.122Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;
        <http://purl.org/dc/terms/modified>
                "2020-03-12T11:52:16.122Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;
        <http://xmlns.com/foaf/0.1/primaryTopic>
                <https://testdirektoratet.no/model/dataservice-catalogs/1> .
""".trim()

val HARVESTED = """
@prefix dct:   <http://purl.org/dc/terms/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
@prefix vcard: <http://www.w3.org/2006/vcard/ns#> .
@prefix dcat:  <http://www.w3.org/ns/dcat#> .
@prefix foaf:  <http://xmlns.com/foaf/0.1/> .

<https://testdirektoratet.no/model/dataservice/0>
        a                         dcat:DataService ;
        dct:description           "Description of service 0"@nb ;
        dct:title                 "Test Service 0"@nb ;
        dcat:contactPoint         [ a                          vcard:Organization ;
                                    vcard:hasOrganizationName  "Testdirektoratet"@nb ;
                                    vcard:hasURL               <https://testdirektoratet.no>
                                  ] ;
        dcat:endpointDescription  <https://testdirektoratet.no/openapi/dataservice/0.yaml> .

<https://testdirektoratet.no/model/dataservice-catalogs/0>
        a              dcat:Catalog ;
        dct:publisher  <https://organization-catalogue.fellesdatakatalog.brreg.no/organizations/123456789> ;
        dct:title      "Dataservicekatalog for Testdirektoratet"@nb ;
        dcat:service   <https://testdirektoratet.no/model/dataservice/0> .

<https://testdirektoratet.no/model/dataservice-catalogs/1>
        a              dcat:Catalog ;
        dct:publisher  <https://organization-catalogue.fellesdatakatalog.brreg.no/organizations/123456789> ;
        dct:title      "Dataservicekatalog 1 for Testdirektoratet"@nb ;
        dcat:service   <https://testdirektoratet.no/model/dataservice/1> .

<https://testdirektoratet.no/model/dataservice/1>
        a                         dcat:DataService ;
        dct:description           "Description of service 1"@nb ;
        dct:title                 "Test Service 1"@nb ;
        dcat:contactPoint         [ a                          vcard:Organization ;
                                    vcard:hasOrganizationName  "Testdirektoratet"@nb ;
                                    vcard:hasURL               <https://testdirektoratet.no>
                                  ] ;
        dcat:endpointDescription  <https://testdirektoratet.no/openapi/dataservice/1.yaml> .
""".trim()