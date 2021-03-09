package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import com.nhaarman.mockitokotlin2.*
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.FusekiAdapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.ApplicationProperties
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.CatalogRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.DataServiceRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.*
import org.apache.jena.rdf.model.Model
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("unit")
class UpdateServiceTest {
    private val catalogRepository: CatalogRepository = mock()
    private val dataServiceRepository: DataServiceRepository = mock()
    private val valuesMock: ApplicationProperties = mock()
    private val fusekiAdapter: FusekiAdapter = mock()
    private val turtleService: TurtleService = mock()
    private val updateService = UpdateService(valuesMock, fusekiAdapter, catalogRepository, dataServiceRepository, turtleService)

    private val responseReader = TestResponseReader()

    @Nested
    internal inner class UpdateMetaData {

        @Test
        fun catalogRecordsIsRecreatedFromMetaDBO() {
            whenever(catalogRepository.findAll())
                .thenReturn(listOf(CATALOG_DBO_0))
            whenever(dataServiceRepository.findAll())
                .thenReturn(listOf(DATA_SERVICE_DBO_0, DATA_SERVICE_DBO_1))
            whenever(dataServiceRepository.findAllByIsPartOf("http://localhost:5000/catalogs/$CATALOG_ID_0"))
                .thenReturn(listOf(DATA_SERVICE_DBO_0, DATA_SERVICE_DBO_1))
            whenever(turtleService.getCatalog(CATALOG_ID_0, false))
                .thenReturn(responseReader.readFile("harvest_response.ttl"))
            whenever(turtleService.getDataService(DATASERVICE_ID_0, false))
                .thenReturn(responseReader.readFile("parsed_dataservice_0.ttl"))
            whenever(turtleService.getDataService(DATASERVICE_ID_1, false))
                .thenReturn(responseReader.readFile("parsed_dataservice_1.ttl"))

            whenever(valuesMock.catalogUri)
                .thenReturn("http://localhost:5000/catalogs")
            whenever(valuesMock.dataserviceUri)
                .thenReturn("http://localhost:5000/dataservices")

            updateService.updateMetaData()

            val expectedCatalog = responseReader.parseFile("catalog_0.ttl", "TURTLE")
            val expectedDataService0 = responseReader.parseFile("dataservice_0.ttl", "TURTLE")
            val expectedDataService1 = responseReader.parseFile("dataservice_1.ttl", "TURTLE")

            argumentCaptor<Model, String, Boolean>().apply {
                verify(turtleService, times(2)).saveAsDataService(first.capture(), second.capture(), third.capture())
                assertTrue(first.firstValue.isIsomorphicWith(expectedDataService0))
                assertTrue(first.secondValue.isIsomorphicWith(expectedDataService1))
                assertEquals(listOf(DATASERVICE_ID_0, DATASERVICE_ID_1), second.allValues)
                assertEquals(listOf(true, true), third.allValues)
            }

            argumentCaptor<Model, String, Boolean>().apply {
                verify(turtleService, times(1)).saveAsCatalog(first.capture(), second.capture(), third.capture())
                assertTrue(first.firstValue.isIsomorphicWith(expectedCatalog))
                assertEquals(CATALOG_ID_0, second.firstValue)
                assertEquals(listOf(true), third.allValues)
            }
        }

    }

    @Nested
    internal inner class UpdateUnionModel {

        @Test
        fun updateUnionModel() {
            whenever(catalogRepository.findAll())
                .thenReturn(listOf(CATALOG_DBO_0, CATALOG_DBO_1))

            whenever(turtleService.getCatalog(CATALOG_ID_0, true))
                .thenReturn(responseReader.readFile("catalog_0.ttl"))
            whenever(turtleService.getCatalog(CATALOG_ID_1, true))
                .thenReturn(responseReader.readFile("catalog_1.ttl"))

            updateService.updateUnionModels()

            val catalogUnion = responseReader.parseFile("all_catalogs.ttl", "TURTLE")

            argumentCaptor<Model>().apply {
                verify(fusekiAdapter, times(1)).storeUnionModel(capture())
                assertTrue(firstValue.isIsomorphicWith(catalogUnion))
            }

            argumentCaptor<Model>().apply {
                verify(turtleService, times(1)).saveAsCatalogUnion(capture())
                assertTrue(firstValue.isIsomorphicWith(catalogUnion))
            }
        }
    }
}