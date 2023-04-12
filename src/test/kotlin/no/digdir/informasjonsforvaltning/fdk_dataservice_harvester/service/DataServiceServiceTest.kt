package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.*
import org.apache.jena.riot.Lang
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class DatasetServiceTest {
    private val turtleService: TurtleService = mock()
    private val dataServiceService = DataServiceService(turtleService)

    private val responseReader = TestResponseReader()

    @Nested
    internal inner class AllCatalogs {

        @Test
        fun responseIsometricWithEmptyModelForEmptyDB() {
            whenever(turtleService.getCatalogUnion(true))
                .thenReturn(null)

            val expected = responseReader.parseResponse("", "TURTLE")

            val responseTurtle = dataServiceService.getAll(Lang.TURTLE, true)
            val responseJsonLD = dataServiceService.getAll(Lang.JSONLD, true)

            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(responseTurtle, "TURTLE")))
            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(responseJsonLD, "JSON-LD")))
        }

        @Test
        fun getAllHandlesTurtleAndOtherRDF() {
            whenever(turtleService.getCatalogUnion(true))
                .thenReturn(javaClass.classLoader.getResource("all_catalogs.ttl")!!.readText())
            whenever(turtleService.getCatalogUnion(false))
                .thenReturn(javaClass.classLoader.getResource("all_catalogs_no_records.ttl")!!.readText())

            val expected = responseReader.parseFile("all_catalogs.ttl", "TURTLE")
            val expectedNoRecords = responseReader.parseFile("all_catalogs_no_records.ttl", "TURTLE")

            val responseTurtle = dataServiceService.getAll(Lang.TURTLE, true)
            val responseN3 = dataServiceService.getAll(Lang.N3, true)
            val responseNTriples = dataServiceService.getAll(Lang.NTRIPLES, false)

            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(responseTurtle, "TURTLE")))
            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(responseN3, "N3")))
            assertTrue(expectedNoRecords.isIsomorphicWith(responseReader.parseResponse(responseNTriples, "N-TRIPLES")))
        }

    }

    @Nested
    internal inner class CatalogById {

        @Test
        fun responseIsNullWhenNoCatalogIsFound() {
            whenever(turtleService.getCatalog("123", true))
                .thenReturn(null)

            val response = dataServiceService.getDataServiceById("123", Lang.TURTLE, true)

            assertNull(response)
        }

        @Test
        fun responseIsIsomorphicWithExpectedModel() {
            whenever(turtleService.getCatalog(CATALOG_ID_0, true))
                .thenReturn(javaClass.classLoader.getResource("catalog_0.ttl")!!.readText())
            whenever(turtleService.getCatalog(CATALOG_ID_0, false))
                .thenReturn(javaClass.classLoader.getResource("catalog_0_no_records.ttl")!!.readText())

            val responseTurtle = dataServiceService.getCatalogById(CATALOG_ID_0, Lang.TURTLE, true)
            val responseJsonRDF = dataServiceService.getCatalogById(CATALOG_ID_0, Lang.RDFJSON, false)

            val expected = responseReader.parseFile("catalog_0.ttl", "TURTLE")
            val expectedNoRecords = responseReader.parseFile("catalog_0_no_records.ttl", "TURTLE")

            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(responseTurtle!!, "TURTLE")))
            assertTrue(expectedNoRecords.isIsomorphicWith(responseReader.parseResponse(responseJsonRDF!!, "RDF/JSON")))
        }

    }

    @Nested
    internal inner class DatasetById {

        @Test
        fun responseIsNullWhenNoCatalogIsFound() {
            whenever(turtleService.getDataService("123", true))
                .thenReturn(null)

            val response = dataServiceService.getDataServiceById("123", Lang.TURTLE, true)

            assertNull(response)
        }

        @Test
        fun responseIsIsomorphicWithExpectedModel() {
            whenever(turtleService.getDataService(DATASERVICE_ID_0, true))
                .thenReturn(javaClass.classLoader.getResource("dataservice_0.ttl")!!.readText())
            whenever(turtleService.getDataService(DATASERVICE_ID_0, false))
                .thenReturn(javaClass.classLoader.getResource("parsed_dataservice_0.ttl")!!.readText())

            val responseTurtle = dataServiceService.getDataServiceById(DATASERVICE_ID_0, Lang.TURTLE, false)
            val responseRDFXML = dataServiceService.getDataServiceById(DATASERVICE_ID_0, Lang.RDFXML, true)

            val expected = responseReader.parseFile("dataservice_0.ttl", "TURTLE")
            val expectedNoRecords = responseReader.parseFile("parsed_dataservice_0.ttl", "TURTLE")

            assertTrue(expectedNoRecords.isIsomorphicWith(responseReader.parseResponse(responseTurtle!!, "TURTLE")))
            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(responseRDFXML!!, "RDF/XML")))
        }

    }
}