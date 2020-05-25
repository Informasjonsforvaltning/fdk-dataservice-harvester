package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.ApplicationProperties
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.HarvestFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.MetaFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.queryToGetMetaDataByUri
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.CATALOG_ID_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATASERVICE_ID_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATASERVICE_META_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.HARVESTED
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.TestResponseReader
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class DataServiceServiceTest {
    private val harvestFuseki: HarvestFuseki = mock()
    private val metaFuseki: MetaFuseki = mock()
    private val valuesMock: ApplicationProperties = mock()
    private val dataServiceService = DataServiceService(harvestFuseki, metaFuseki, valuesMock)

    private val responseReader = TestResponseReader()

    @Nested
    internal inner class CountMetaData {

        @Test
        fun handlesCountOfEmptyDB() {
            whenever(metaFuseki.fetchCompleteModel())
                .thenReturn(ModelFactory.createDefaultModel())

            val response = dataServiceService.countMetaData()

            assertEquals(0, response)
        }

        @Test
        fun countsCorrectly() {
            val metaModels = responseReader.parseFile("complete_meta_model.ttl", "TURTLE")

            whenever(metaFuseki.fetchCompleteModel())
                .thenReturn(metaModels)

            val response = dataServiceService.countMetaData()

            assertEquals(4, response)
        }

    }

    @Nested
    internal inner class AllCatalogs {

        @Test
        fun answerWithEmptyListWhenNoModelsSavedInFuseki() {
            whenever(metaFuseki.fetchCompleteModel())
                .thenReturn(ModelFactory.createDefaultModel())
            whenever(harvestFuseki.fetchCompleteModel())
                .thenReturn(ModelFactory.createDefaultModel())

            val expected = responseReader.parseResponse("", "TURTLE")

            val response = dataServiceService.getAll(JenaType.TURTLE)

            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(response, "TURTLE")))
        }

        @Test
        fun responseIsIsomorphicWithUnionOfModelsFromFuseki() {
            val harvestModel = responseReader.parseResponse(HARVESTED, "TURTLE")
            val metaModel = responseReader.parseFile("complete_meta_model.ttl", "TURTLE")

            whenever(metaFuseki.fetchCompleteModel())
                .thenReturn(metaModel)
            whenever(harvestFuseki.fetchCompleteModel())
                .thenReturn(harvestModel)

            val expected = metaModel.union(harvestModel)

            val response = dataServiceService.getAll(JenaType.TURTLE)

            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(response, "TURTLE")))
        }

    }

    @Nested
    internal inner class CatalogById {

        @Test
        fun responseIsNullWhenNotFoundInMetaDataDB() {

            whenever(metaFuseki.queryDescribe("DESCRIBE <http://host.testcontainers.internal:5000/catalogs/$CATALOG_ID_0>"))
                .thenReturn(ModelFactory.createDefaultModel())

            whenever(valuesMock.catalogUri)
                .thenReturn("http://host.testcontainers.internal:5000/catalogs")

            val response = dataServiceService.getCatalogById("123", JenaType.TURTLE)

            assertNull(response)
        }

        @Test
        fun responseIsIsomorphicWithExpected() {
            val dbMeta = responseReader.parseFile("no_prefix_catalog_meta_0.ttl", "TURTLE")
            val completeHarvestModel = responseReader.parseResponse(HARVESTED, "TURTLE")

            whenever(metaFuseki.queryDescribe("DESCRIBE <http://host.testcontainers.internal:5000/catalogs/$CATALOG_ID_0>"))
                .thenReturn(dbMeta)

            whenever(valuesMock.catalogUri)
                .thenReturn("http://host.testcontainers.internal:5000/catalogs")

            whenever(harvestFuseki.fetchCompleteModel())
                .thenReturn(completeHarvestModel)

            val response = dataServiceService.getCatalogById(CATALOG_ID_0, JenaType.TURTLE)
            val expected = responseReader.parseFile("catalog_0.ttl", "TURTLE")

            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(response!!, "TURTLE")))
        }

        @Test
        fun handlesNotBeingPresentInHarvestData() {
            val dbMeta = responseReader.parseFile("no_prefix_catalog_meta_0.ttl", "TURTLE")
            val catalog1 = responseReader.parseFile("no_prefix_catalog_1.ttl", "TURTLE")
            val dataservice1 = responseReader.parseFile("no_prefix_dataservice_1.ttl", "TURTLE")

            whenever(metaFuseki.queryDescribe("DESCRIBE <http://host.testcontainers.internal:5000/catalogs/$CATALOG_ID_0>"))
                .thenReturn(dbMeta)

            whenever(harvestFuseki.fetchCompleteModel())
                .thenReturn(catalog1.union(dataservice1))

            whenever(valuesMock.catalogUri)
                .thenReturn("http://host.testcontainers.internal:5000/catalogs")

            val response = dataServiceService.getCatalogById(CATALOG_ID_0, JenaType.TURTLE)

            assertTrue(dbMeta.isIsomorphicWith(responseReader.parseResponse(response!!, "TURTLE")))
        }
    }

    @Nested
    internal inner class DataServiceById {

        @Test
        fun responseIsNullWhenNotFoundInMetaDataDB() {
            whenever(metaFuseki.queryDescribe("DESCRIBE <http://host.testcontainers.internal:5000/dataservices/123>"))
                .thenReturn(ModelFactory.createDefaultModel())

            whenever(valuesMock.dataserviceUri)
                .thenReturn("http://host.testcontainers.internal:5000/dataservices")

            val response = dataServiceService.getDataserviceById("123", JenaType.TURTLE)

            assertNull(response)
        }

        @Test
        fun responseIsIsomorphicWithExpected() {
            val dbMeta = responseReader.parseResponse(DATASERVICE_META_0, "TURTLE")
            whenever(metaFuseki.queryDescribe("DESCRIBE <http://host.testcontainers.internal:5000/dataservices/$DATASERVICE_ID_0>"))
                .thenReturn(dbMeta)

            whenever(valuesMock.dataserviceUri)
                .thenReturn("http://host.testcontainers.internal:5000/dataservices")

            whenever(harvestFuseki.fetchCompleteModel())
                .thenReturn(responseReader.parseFile("complete_harvest_model.ttl", "TURTLE"))


            val response = dataServiceService.getDataserviceById(DATASERVICE_ID_0, JenaType.TURTLE)
            val expected = responseReader.parseFile("no_prefix_dataservice_0.ttl", "TURTLE")

            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(response!!, "TURTLE")))
        }

    }
}