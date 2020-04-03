package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.CatalogFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.DataServiceFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.CATALOG_ID_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATASERVICE_ID_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATASERVICE_ID_1
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.TestResponseReader
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class CatalogServiceTest {
    private val dataServiceFuseki: DataServiceFuseki = mock()
    private val catalogFuseki: CatalogFuseki = mock()
    private val catalogService: CatalogService = CatalogService(catalogFuseki, dataServiceFuseki)

    private val responseReader = TestResponseReader()

    @Nested
    internal inner class CountDataServiceCatalogs {

        @Test
        fun handlesCountOfEmptyDB() {
            whenever(catalogFuseki.fetchCompleteModel())
                .thenReturn(ModelFactory.createDefaultModel())

            val response = catalogService.countDataServiceCatalogs()

            assertEquals(0, response)
        }

        @Test
        fun responseIsIsomorphicWithUnionOfModelsFromFuseki() {
            val dbCatalog0 = responseReader.parseFile("no_prefix_catalog_0.ttl", "TURTLE")
            val dbCatalog1 = responseReader.parseFile("no_prefix_catalog_1.ttl", "TURTLE")

            whenever(catalogFuseki.fetchCompleteModel())
                .thenReturn(dbCatalog0.union(dbCatalog1))

            val response = catalogService.countDataServiceCatalogs()

            assertEquals(2, response)
        }

    }

    @Nested
    internal inner class AllCatalogs {

        @Test
        fun answerWithEmptyListWhenNoModelsSavedInFuseki() {
            whenever(catalogFuseki.fetchCompleteModel())
                .thenReturn(ModelFactory.createDefaultModel())
            whenever(dataServiceFuseki.fetchCompleteModel())
                .thenReturn(ModelFactory.createDefaultModel())

            val expected = responseReader.parseResponse("", "TURTLE")

            val response = catalogService.getAllDataServiceCatalogs(JenaType.TURTLE)

            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(response, "TURTLE")))
        }

        @Test
        fun responseIsIsomorphicWithUnionOfModelsFromFuseki() {
            val dbCatalog0 = responseReader.parseFile("no_prefix_catalog_0.ttl", "TURTLE")
            val dbCatalog1 = responseReader.parseFile("no_prefix_catalog_1.ttl", "TURTLE")
            val dbDataService0 = responseReader.parseFile("no_prefix_dataservice_0.ttl", "TURTLE")
            val dbDataService1 = responseReader.parseFile("no_prefix_dataservice_1.ttl", "TURTLE")

            whenever(catalogFuseki.fetchCompleteModel())
                .thenReturn(dbCatalog0.union(dbCatalog1))
            whenever(dataServiceFuseki.fetchCompleteModel())
                .thenReturn(dbDataService0.union(dbDataService1))

            val expected = dbCatalog0.union(dbCatalog1).union(dbDataService0).union(dbDataService1)

            val response = catalogService.getAllDataServiceCatalogs(JenaType.TURTLE)

            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(response, "TURTLE")))
        }

    }

    @Nested
    internal inner class CatalogById {

        @Test
        fun responseIsNullWhenNotFoundInFuseki() {
            whenever(dataServiceFuseki.fetchByGraphName("123"))
                .thenReturn(null)

            val response = catalogService.getDataServiceCatalog("123", JenaType.TURTLE)

            assertNull(response)
        }

        @Test
        fun responseIsIsomorphicWithUnionOfModelsFromFuseki() {
            val dbCatalog = responseReader.parseFile("no_prefix_catalog_0.ttl", "TURTLE")
            val dbDataService = responseReader.parseFile("no_prefix_dataservice_0.ttl", "TURTLE")

            whenever(catalogFuseki.fetchByGraphName(CATALOG_ID_0))
                .thenReturn(dbCatalog)

            whenever(dataServiceFuseki.fetchByGraphName(DATASERVICE_ID_0))
                .thenReturn(dbDataService)

            val response = catalogService.getDataServiceCatalog(CATALOG_ID_0, JenaType.TURTLE)

            assertTrue(dbCatalog.union(dbDataService).isIsomorphicWith(responseReader.parseResponse(response!!, "TURTLE")))
        }

        @Test
        fun handlesMissingDataService() {
            val dbCatalog = responseReader.parseFile("no_prefix_catalog_0.ttl", "TURTLE")

            whenever(catalogFuseki.fetchByGraphName(CATALOG_ID_0))
                .thenReturn(dbCatalog)

            whenever(dataServiceFuseki.fetchByGraphName(DATASERVICE_ID_0))
                .thenReturn(null)

            val response = catalogService.getDataServiceCatalog(CATALOG_ID_0, JenaType.TURTLE)

            assertTrue(dbCatalog.isIsomorphicWith(responseReader.parseResponse(response!!, "TURTLE")))
        }


    }
}