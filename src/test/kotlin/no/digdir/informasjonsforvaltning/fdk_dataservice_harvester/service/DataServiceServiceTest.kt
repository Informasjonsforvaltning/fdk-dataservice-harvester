package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.*
import org.apache.jena.riot.Lang
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
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
            whenever(turtleService.getCatalogUnion())
                .thenReturn(null)

            val expected = responseReader.parseResponse("", "TURTLE")

            val responseTurtle = dataServiceService.getAll(Lang.TURTLE)
            val responseJsonLD = dataServiceService.getAll(Lang.JSONLD)

            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(responseTurtle, "TURTLE")))
            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(responseJsonLD, "JSON-LD")))
        }

        @Test
        fun getAllHandlesTurtleAndOtherRDF() {
            whenever(turtleService.getCatalogUnion())
                .thenReturn(javaClass.classLoader.getResource("all_catalogs.ttl")!!.readText())

            val expected = responseReader.parseFile("all_catalogs.ttl", "TURTLE")

            val responseTurtle = dataServiceService.getAll(Lang.TURTLE)
            val responseN3 = dataServiceService.getAll(Lang.N3)
            val responseNTriples = dataServiceService.getAll(Lang.NTRIPLES)

            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(responseTurtle, "TURTLE")))
            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(responseN3, "N3")))
            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(responseNTriples, "N-TRIPLES")))
        }

    }

    @Nested
    internal inner class CatalogById {

        @Test
        fun responseIsNullWhenNoCatalogIsFound() {
            whenever(turtleService.getCatalog("123", true))
                .thenReturn(null)

            val response = dataServiceService.getDataServiceById("123", Lang.TURTLE)

            assertNull(response)
        }

        @Test
        fun responseIsIsomorphicWithExpectedModel() {
            whenever(turtleService.getCatalog(CATALOG_ID_0, true))
                .thenReturn(javaClass.classLoader.getResource("catalog_0.ttl")!!.readText())

            val responseTurtle = dataServiceService.getCatalogById(CATALOG_ID_0, Lang.TURTLE)
            val responseJsonRDF = dataServiceService.getCatalogById(CATALOG_ID_0, Lang.RDFJSON)

            val expected = responseReader.parseFile("catalog_0.ttl", "TURTLE")

            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(responseTurtle!!, "TURTLE")))
            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(responseJsonRDF!!, "RDF/JSON")))
        }

    }

    @Nested
    internal inner class DatasetById {

        @Test
        fun responseIsNullWhenNoCatalogIsFound() {
            whenever(turtleService.getDataService("123", true))
                .thenReturn(null)

            val response = dataServiceService.getDataServiceById("123", Lang.TURTLE)

            assertNull(response)
        }

        @Test
        fun responseIsIsomorphicWithExpectedModel() {
            whenever(turtleService.getDataService(DATASERVICE_ID_0, true))
                .thenReturn(javaClass.classLoader.getResource("dataservice_0.ttl")!!.readText())

            val responseTurtle = dataServiceService.getDataServiceById(DATASERVICE_ID_0, Lang.TURTLE)
            val responseRDFXML = dataServiceService.getDataServiceById(DATASERVICE_ID_0, Lang.RDFXML)

            val expected = responseReader.parseFile("dataservice_0.ttl", "TURTLE")

            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(responseTurtle!!, "TURTLE")))
            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(responseRDFXML!!, "RDF/XML")))
        }

    }
}