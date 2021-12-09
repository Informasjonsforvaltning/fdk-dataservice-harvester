package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import com.nhaarman.mockitokotlin2.*
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.DataServiceAdapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.ApplicationProperties
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.CatalogMeta
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.DataServiceMeta
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.CatalogRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.DataServiceRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.TurtleService
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.*
import org.apache.jena.rdf.model.Model
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

@Tag("unit")
class HarvesterTest {
    private val catalogRepository: CatalogRepository = mock()
    private val dataServiceRepository: DataServiceRepository = mock()
    private val turtleService: TurtleService = mock()
    private val valuesMock: ApplicationProperties = mock()
    private val adapter: DataServiceAdapter = mock()

    private val harvester = DataServiceHarvester(
        adapter, catalogRepository,
        dataServiceRepository, turtleService, valuesMock
    )

    private val responseReader = TestResponseReader()

    @Test
    fun harvestDataSourceSavedWhenDBIsEmpty() {
        whenever(adapter.getDataServices(TEST_HARVEST_SOURCE))
            .thenReturn(responseReader.readFile("harvest_response.ttl"))
        whenever(dataServiceRepository.findById(DATASERVICE_ID_0))
            .thenReturn(Optional.of(DATA_SERVICE_DBO_0))

        whenever(valuesMock.catalogUri)
            .thenReturn("http://localhost:5000/catalogs")
        whenever(valuesMock.dataserviceUri)
            .thenReturn("http://localhost:5000/dataservices")

        harvester.harvestDataServiceCatalog(TEST_HARVEST_SOURCE, TEST_HARVEST_DATE)

        argumentCaptor<Model, String>().apply {
            verify(turtleService, times(1)).saveAsHarvestSource(first.capture(), second.capture())
            assertTrue(
                first.firstValue.isIsomorphicWith(
                    responseReader.parseFile(
                        "harvest_response.ttl",
                        "TURTLE"
                    )
                )
            )
            Assertions.assertEquals(TEST_HARVEST_SOURCE.url, second.firstValue)
        }

        argumentCaptor<Model, String, Boolean>().apply {
            verify(turtleService, times(1)).saveAsCatalog(first.capture(), second.capture(), third.capture())
            assertTrue(first.allValues[0].isIsomorphicWith(responseReader.parseFile("catalog_0_no_records.ttl", "TURTLE")))
            assertEquals(listOf(CATALOG_ID_0), second.allValues)
            Assertions.assertEquals(listOf(false), third.allValues)
        }

        argumentCaptor<CatalogMeta>().apply {
            verify(catalogRepository, times(1)).save(capture())
            assertEquals(CATALOG_DBO_0, firstValue)
        }

        argumentCaptor<Model, String, Boolean>().apply {
            verify(turtleService, times(1)).saveAsDataService(first.capture(), second.capture(), third.capture())
            assertTrue(first.firstValue.isIsomorphicWith(responseReader.parseFile("parsed_dataservice_0.ttl", "TURTLE")))
            assertEquals(DATASERVICE_ID_0, second.firstValue)
            assertEquals(false, third.firstValue)
        }

        argumentCaptor<DataServiceMeta>().apply {
            verify(dataServiceRepository, times(1)).save(capture())
            assertEquals(DATA_SERVICE_DBO_0, firstValue)
        }
    }

    @Test
    fun harvestDataSourceNotPersistedWhenNoChangesFromDB() {
        val harvested = responseReader.readFile("harvest_response.ttl")
        whenever(adapter.getDataServices(TEST_HARVEST_SOURCE))
            .thenReturn(harvested)
        whenever(turtleService.getHarvestSource(TEST_HARVEST_SOURCE.url!!))
            .thenReturn(harvested)

        harvester.harvestDataServiceCatalog(TEST_HARVEST_SOURCE, TEST_HARVEST_DATE)

        argumentCaptor<Model, String>().apply {
            verify(turtleService, times(0)).saveAsHarvestSource(first.capture(), second.capture())
        }
        argumentCaptor<Model, String, Boolean>().apply {
            verify(turtleService, times(0)).saveAsCatalog(first.capture(), second.capture(), third.capture())
        }
        argumentCaptor<Model, String, Boolean>().apply {
            verify(turtleService, times(0)).saveAsDataService(first.capture(), second.capture(), third.capture())
        }

        argumentCaptor<CatalogMeta>().apply {
            verify(catalogRepository, times(0)).save(capture())
        }
        argumentCaptor<DataServiceMeta>().apply {
            verify(dataServiceRepository, times(0)).save(capture())
        }
    }

    @Test
    fun onlyCatalogMetaUpdatedWhenOnlyCatalogDataChangedFromDB() {
        whenever(adapter.getDataServices(TEST_HARVEST_SOURCE))
            .thenReturn(responseReader.readFile("harvest_response.ttl"))
        whenever(turtleService.getHarvestSource(TEST_HARVEST_SOURCE.url!!))
            .thenReturn(responseReader.readFile("harvest_response_catalog_diff.ttl"))

        whenever(valuesMock.catalogUri)
            .thenReturn("http://localhost:5000/catalogs")
        whenever(valuesMock.dataserviceUri)
            .thenReturn("http://localhost:5000/dataservices")

        whenever(catalogRepository.findById(CATALOG_DBO_0.uri))
            .thenReturn(Optional.of(CATALOG_DBO_0))
        whenever(dataServiceRepository.findById(DATA_SERVICE_DBO_0.uri))
            .thenReturn(Optional.of(DATA_SERVICE_DBO_0))

        whenever(turtleService.getCatalog(CATALOG_ID_0, false))
            .thenReturn(responseReader.readFile("harvest_response_catalog_diff.ttl"))
        whenever(turtleService.getDataService(DATASERVICE_ID_0, false))
            .thenReturn(responseReader.readFile("parsed_dataservice_0.ttl"))

        harvester.harvestDataServiceCatalog(TEST_HARVEST_SOURCE, NEW_TEST_HARVEST_DATE)

        argumentCaptor<Model, String>().apply {
            verify(turtleService, times(1)).saveAsHarvestSource(first.capture(), second.capture())
            assertTrue(first.firstValue.isIsomorphicWith(responseReader.parseFile("harvest_response.ttl", "TURTLE")))
            Assertions.assertEquals(TEST_HARVEST_SOURCE.url, second.firstValue)
        }

        argumentCaptor<CatalogMeta>().apply {
            verify(catalogRepository, times(1)).save(capture())
            assertEquals(CATALOG_DBO_0.copy(modified = NEW_TEST_HARVEST_DATE.timeInMillis), firstValue)
        }

        argumentCaptor<DataServiceMeta>().apply {
            verify(dataServiceRepository, times(0)).save(capture())
        }

        argumentCaptor<Model, String, Boolean>().apply {
            verify(turtleService, times(1)).saveAsCatalog(first.capture(), second.capture(), third.capture())
            assertTrue(first.firstValue.isIsomorphicWith(responseReader.parseFile("catalog_0_no_records.ttl", "TURTLE")))
            assertEquals(listOf(CATALOG_ID_0), second.allValues)
            Assertions.assertEquals(listOf(false), third.allValues)
        }

        argumentCaptor<Model, String, Boolean>().apply {
            verify(turtleService, times(0)).saveAsDataService(first.capture(), second.capture(), third.capture())
        }
    }

    @Test
    fun harvestWithErrorsIsNotPersisted() {
        whenever(adapter.getDataServices(TEST_HARVEST_SOURCE))
            .thenReturn(responseReader.readFile("harvest_response_with_errors.ttl"))

        harvester.harvestDataServiceCatalog(TEST_HARVEST_SOURCE, TEST_HARVEST_DATE)

        argumentCaptor<Model, String>().apply {
            verify(turtleService, times(0)).saveAsHarvestSource(first.capture(), second.capture())
        }
        argumentCaptor<Model, String, Boolean>().apply {
            verify(turtleService, times(0)).saveAsCatalog(first.capture(), second.capture(), third.capture())
        }
        argumentCaptor<Model, String, Boolean>().apply {
            verify(turtleService, times(0)).saveAsDataService(first.capture(), second.capture(), third.capture())
        }

        argumentCaptor<CatalogMeta>().apply {
            verify(catalogRepository, times(0)).save(capture())
        }
        argumentCaptor<DataServiceMeta>().apply {
            verify(dataServiceRepository, times(0)).save(capture())
        }
    }

}