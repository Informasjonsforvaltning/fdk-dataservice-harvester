package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.ApplicationProperties
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.HarvestFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.MetaFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.MissingHarvestException
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.queryToGetMetaDataByCatalogUri
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.queryToGetMetaDataByUri
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.CATALOG_ID_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.CATALOG_META_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATASERVICE_ID_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATASERVICE_META_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.HARVESTED
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.TestResponseReader
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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

            whenever(metaFuseki.queryDescribe("DESCRIBE <http://localhost:5000/catalogs/$CATALOG_ID_0>"))
                .thenReturn(ModelFactory.createDefaultModel())

            whenever(valuesMock.catalogUri)
                .thenReturn("http://localhost:5000/catalogs")

            val response = dataServiceService.getCatalogById("123", JenaType.TURTLE)

            assertNull(response)
        }

        @Test
        fun responseIsIsomorphicWithExpected() {
            val catalogMeta = responseReader.parseResponse(CATALOG_META_0, "TURTLE")
            val serviceMeta = responseReader.parseResponse(DATASERVICE_META_0, "TURTLE")
            val dbCatalog = responseReader.parseFile("catalog_0_no_uri_properties.ttl", "TURTLE")
            val dbDataService = responseReader.parseFile("dataservice_0_no_uri_properties.ttl", "TURTLE")

            whenever(metaFuseki.queryDescribe("DESCRIBE <http://localhost:5000/catalogs/$CATALOG_ID_0>"))
                .thenReturn(catalogMeta)

            whenever(metaFuseki.queryDescribe(queryToGetMetaDataByCatalogUri("http://localhost:5000/catalogs/$CATALOG_ID_0")))
                .thenReturn(serviceMeta)

            whenever(valuesMock.catalogUri)
                .thenReturn("http://localhost:5000/catalogs")

            whenever(harvestFuseki.queryDescribe("DESCRIBE <https://testdirektoratet.no/model/dataservice-catalogs/0>"))
                .thenReturn(dbCatalog)

            whenever(harvestFuseki.queryDescribe("DESCRIBE * WHERE { <https://testdirektoratet.no/model/dataservice-catalogs/0> ?p ?o }"))
                .thenReturn(dbDataService)

            val response = dataServiceService.getCatalogById(CATALOG_ID_0, JenaType.TURTLE)
            val expected = responseReader.parseFile("catalog_0.ttl", "TURTLE")

            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(response!!, "TURTLE")))
        }

        @Test
        fun exceptionWhenNoHarvestDataFound() {
            val dbMeta = responseReader.parseFile("no_prefix_catalog_meta_0.ttl", "TURTLE")

            whenever(metaFuseki.queryDescribe("DESCRIBE <http://localhost:5000/catalogs/$CATALOG_ID_0>"))
                .thenReturn(dbMeta)

            whenever(valuesMock.catalogUri)
                .thenReturn("http://localhost:5000/catalogs")

            assertThrows<MissingHarvestException> { dataServiceService.getCatalogById(CATALOG_ID_0, JenaType.TURTLE) }
        }
    }

    @Nested
    internal inner class DataServiceById {

        @Test
        fun responseIsNullWhenNotFoundInMetaDataDB() {
            whenever(metaFuseki.queryDescribe("DESCRIBE <http://localhost:5000/dataservices/123>"))
                .thenReturn(ModelFactory.createDefaultModel())

            whenever(valuesMock.dataserviceUri)
                .thenReturn("http://localhost:5000/dataservices")

            val response = dataServiceService.getDataserviceById("123", JenaType.TURTLE)

            assertNull(response)
        }

        @Test
        fun responseIsIsomorphicWithExpected() {
            val dbMeta = responseReader.parseResponse(DATASERVICE_META_0, "TURTLE")
            val dbDataService = responseReader.parseFile("dataservice_0_no_uri_properties.ttl", "TURTLE")

            whenever(metaFuseki.queryDescribe("DESCRIBE <http://localhost:5000/dataservices/$DATASERVICE_ID_0>"))
                .thenReturn(dbMeta)

            whenever(valuesMock.dataserviceUri)
                .thenReturn("http://localhost:5000/dataservices")

            whenever(harvestFuseki.queryDescribe("DESCRIBE <https://testdirektoratet.no/model/dataservice/0>"))
                .thenReturn(dbDataService)

            whenever(harvestFuseki.queryDescribe("DESCRIBE * WHERE { <https://testdirektoratet.no/model/dataservice/0> ?p ?o }"))
                .thenReturn(ModelFactory.createDefaultModel())


            val response = dataServiceService.getDataserviceById(DATASERVICE_ID_0, JenaType.TURTLE)
            val expected = responseReader.parseFile("dataservice_0.ttl", "TURTLE")

            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(response!!, "TURTLE")))
        }

    }
}