package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.TestResponseReader
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Tag("unit")
class DataServiceProperties {
    private val responseReader = TestResponseReader()

    @Test
    fun listChangedDatasets() {
        val catalog0 = responseReader.parseFile("harvest_diff_0.ttl", "TURTLE")
        val catalog1 = responseReader.parseFile("harvest_diff_1.ttl", "TURTLE")

        val changed = changedCatalogAndDataServices(catalog0, catalog1)

        val expected = mapOf(Pair("https://testdirektoratet.no/model/dataservice-catalogs/0", listOf(
            "https://testdirektoratet.no/model/dataservice/1",
            "https://testdirektoratet.no/model/dataservice/3")))

        assertEquals(expected["https://testdirektoratet.no/model/dataservice-catalogs/0"], changed["https://testdirektoratet.no/model/dataservice-catalogs/0"]?.sorted())
    }

    @Nested
    internal inner class ModelDifferences {

        @Test
        fun servicesAreEqual() {
            val catalog0 = responseReader.parseFile("harvest_diff_0.ttl", "TURTLE")
            val duplicate0 = responseReader.parseFile("harvest_diff_1.ttl", "TURTLE")

            assertFalse { serviceDiffersInModels("https://testdirektoratet.no/model/dataservice/0", catalog0, duplicate0) }
        }

        @Test
        fun missingServiceRegistersAsDifference() {
            val catalogModel = responseReader.parseFile("harvest_response.ttl", "TURTLE")
            val allCatalogsModel = responseReader.parseFile("all_catalogs.ttl", "TURTLE")

            assertTrue { serviceDiffersInModels("https://testdirektoratet.no/model/dataservice/1", allCatalogsModel, catalogModel) }
            assertFalse { serviceDiffersInModels("https://testdirektoratet.no/model/dataservice/0", allCatalogsModel, catalogModel) }
        }

        @Test
        fun serviceLiteralDifference() {
            val model0 = responseReader.parseResponse("""
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix dct:   <http://purl.org/dc/terms/> .

                <https://testdirektoratet.no/model/dataservice/0>
                    a                         dcat:DataService ;
                    dct:title                 "Test Service 0"@en .""".trimIndent(), "TURTLE")
            val model1 = responseReader.parseResponse("""
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix dct:   <http://purl.org/dc/terms/> .

                <https://testdirektoratet.no/model/dataservice/0>
                    a                         dcat:Dataset ;
                    dct:title                 "Different title"@en .""".trimIndent(), "TURTLE")

            assertTrue { serviceDiffersInModels("https://testdirektoratet.no/model/dataservice/0", model0, model1) }
        }

        @Test
        fun serviceBNodeDifference() {
            val model0 = responseReader.parseResponse("""
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix vcard: <http://www.w3.org/2006/vcard/ns#> .

                <https://testdirektoratet.no/model/dataservice/0>
                    a                         dcat:DataService ;
                    dcat:contactPoint         [ a                          vcard:Organization ;
                                                vcard:hasOrganizationName  "Testdirektoratet"@nb ;
                                                vcard:hasURL               <https://testdirektoratet.no>
                                              ] .""".trimIndent(), "TURTLE")
            val model1 = responseReader.parseResponse("""
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix vcard: <http://www.w3.org/2006/vcard/ns#> .

                <https://testdirektoratet.no/model/dataservice/0>
                    a                         dcat:DataService ;
                    dcat:contactPoint         [ a                          vcard:Organization ;
                                                vcard:hasOrganizationName  "Testdirektoratet"@nn ;
                                                vcard:hasURL               <https://testdirektoratet.no>
                                              ] .""".trimIndent(), "TURTLE")

            assertTrue { serviceDiffersInModels("https://testdirektoratet.no/model/dataservice/0", model0, model1) }
        }

        @Test
        fun serviceURINodeDifference() {
            val model0 = responseReader.parseResponse("""
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix vcard: <http://www.w3.org/2006/vcard/ns#> .

                <https://testdirektoratet.no/model/dataservice/0>
                    a                   dcat:DataService ;
                    dcat:contactPoint    <https://testdirektoratet.no/model/contact/0> .
                    
                <https://testdirektoratet.no/model/contact/0>
                    a                          vcard:Organization ;
                    vcard:hasOrganizationName  "Testdirektoratet"@nb ;
                    vcard:hasURL               <https://testdirektoratet.no> .""".trimIndent(), "TURTLE")
            val model1 = responseReader.parseResponse("""
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix vcard: <http://www.w3.org/2006/vcard/ns#> .

                <https://testdirektoratet.no/model/dataservice/0>
                    a                   dcat:DataService ;
                    dcat:contactPoint    <https://testdirektoratet.no/model/contact/0> .
                    
                <https://testdirektoratet.no/model/contact/0>
                    a                          vcard:Organization ;
                    vcard:hasOrganizationName  "Testdirektoratet"@nb ;
                    vcard:hasURL               <https://testdirektorat.no> .""".trimIndent(), "TURTLE")

            assertTrue { serviceDiffersInModels("https://testdirektoratet.no/model/dataservice/0", model0, model1) }
        }

    }
}