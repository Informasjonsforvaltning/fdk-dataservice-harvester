package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import com.nhaarman.mockitokotlin2.*
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.DataServiceAdapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.FusekiAdapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.ApplicationProperties
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.CatalogDBO
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.DataServiceDBO
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.MiscellaneousTurtle
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.CatalogRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.DataServiceRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.MiscellaneousRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.gzip
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

@Tag("unit")
class HarvesterTest {
    private val catalogRepository: CatalogRepository = mock()
    private val dataServiceRepository: DataServiceRepository = mock()
    private val miscRepository: MiscellaneousRepository = mock()
    private val valuesMock: ApplicationProperties = mock()
    private val adapter: DataServiceAdapter = mock()
    private val fusekiAdapter: FusekiAdapter = mock()

    private val harvester = DataServiceHarvester(
        adapter, fusekiAdapter, catalogRepository,
        dataServiceRepository, miscRepository, valuesMock
    )

    private val responseReader = TestResponseReader()

    @Test
    fun harvestDataSourceSavedWhenDBIsEmpty() {
        whenever(adapter.getDataServices(TEST_HARVEST_SOURCE))
            .thenReturn(responseReader.readFile("harvest_response.ttl"))

        whenever(valuesMock.catalogUri)
            .thenReturn("http://localhost:5000/catalogs")
        whenever(valuesMock.dataserviceUri)
            .thenReturn("http://localhost:5000/dataservices")

        harvester.harvestDataServiceCatalog(TEST_HARVEST_SOURCE, TEST_HARVEST_DATE)

        argumentCaptor<MiscellaneousTurtle>().apply {
            verify(miscRepository, times(1)).save(capture())
            assertEquals(HARVEST_DBO_0, firstValue)
        }

        argumentCaptor<List<CatalogDBO>>().apply {
            verify(catalogRepository, times(1)).saveAll(capture())
            assertEquals(1, firstValue.size)
            assertEquals(CATALOG_DBO_0, firstValue.first())
        }

        argumentCaptor<List<DataServiceDBO>>().apply {
            verify(dataServiceRepository, times(1)).saveAll(capture())
            assertEquals(1, firstValue.size)
            assertEquals(DATA_SERVICE_DBO_0, firstValue.first())
        }
    }

    @Test
    fun harvestDataSourceNotPersistedWhenNoChangesFromDB() {
        whenever(adapter.getDataServices(TEST_HARVEST_SOURCE))
            .thenReturn(responseReader.readFile("harvest_response.ttl"))

        whenever(miscRepository.findById(TEST_HARVEST_SOURCE.url!!))
            .thenReturn(Optional.of(HARVEST_DBO_0))

        whenever(valuesMock.catalogUri)
            .thenReturn("http://localhost:5000/catalogs")
        whenever(valuesMock.dataserviceUri)
            .thenReturn("http://localhost:5000/dataservices")

        harvester.harvestDataServiceCatalog(TEST_HARVEST_SOURCE, TEST_HARVEST_DATE)

        argumentCaptor<MiscellaneousTurtle>().apply {
            verify(miscRepository, times(0)).save(capture())
        }

        argumentCaptor<List<CatalogDBO>>().apply {
            verify(catalogRepository, times(0)).saveAll(capture())
        }

        argumentCaptor<List<DataServiceDBO>>().apply {
            verify(dataServiceRepository, times(0)).saveAll(capture())
        }

    }

    @Test
    fun onlyCatalogMetaUpdatedWhenOnlyCatalogDataChangedFromDB() {
        whenever(adapter.getDataServices(TEST_HARVEST_SOURCE))
            .thenReturn(responseReader.readFile("harvest_response.ttl"))

        val catalogDiffTurtle = gzip(responseReader.readFile("harvest_response_catalog_diff.ttl"))

        whenever(miscRepository.findById("http://localhost:5000/harvest0"))
            .thenReturn(Optional.of(HARVEST_DBO_0.copy(turtle = catalogDiffTurtle)))

        whenever(catalogRepository.findById("https://testdirektoratet.no/model/dataservice-catalogs/0"))
            .thenReturn(Optional.of(CATALOG_DBO_0.copy(turtleHarvested = catalogDiffTurtle)))

        whenever(dataServiceRepository.findById("https://testdirektoratet.no/model/dataservice/0"))
            .thenReturn(Optional.of(DATA_SERVICE_DBO_0))

        whenever(valuesMock.catalogUri)
            .thenReturn("http://localhost:5000/catalogs")
        whenever(valuesMock.dataserviceUri)
            .thenReturn("http://localhost:5000/dataservices")

        val expectedCatalogDBO = CATALOG_DBO_0.copy(
            modified = NEW_TEST_HARVEST_DATE.timeInMillis,
            turtleCatalog = gzip(responseReader.readFile("catalog_0_catalog_diff.ttl"))
        )

        harvester.harvestDataServiceCatalog(TEST_HARVEST_SOURCE, NEW_TEST_HARVEST_DATE)

        argumentCaptor<MiscellaneousTurtle>().apply {
            verify(miscRepository, times(1)).save(capture())
            assertEquals(HARVEST_DBO_0, firstValue)
        }

        argumentCaptor<List<CatalogDBO>>().apply {
            verify(catalogRepository, times(1)).saveAll(capture())
            assertEquals(1, firstValue.size)
            assertEquals(expectedCatalogDBO, firstValue.first())
        }

        argumentCaptor<List<DataServiceDBO>>().apply {
            verify(dataServiceRepository, times(1)).saveAll(capture())
            assertEquals(0, firstValue.size)
        }

    }

    @Test
    fun harvestWithErrorsIsNotPersisted() {
        whenever(adapter.getDataServices(TEST_HARVEST_SOURCE))
            .thenReturn(responseReader.readFile("harvest_response_with_errors.ttl"))

        whenever(valuesMock.catalogUri)
            .thenReturn("http://localhost:5000/catalogs")
        whenever(valuesMock.dataserviceUri)
            .thenReturn("http://localhost:5000/dataservices")

        harvester.harvestDataServiceCatalog(TEST_HARVEST_SOURCE, TEST_HARVEST_DATE)

        harvester.harvestDataServiceCatalog(TEST_HARVEST_SOURCE, TEST_HARVEST_DATE)

        argumentCaptor<MiscellaneousTurtle>().apply {
            verify(miscRepository, times(0)).save(capture())
        }

        argumentCaptor<List<CatalogDBO>>().apply {
            verify(catalogRepository, times(0)).saveAll(capture())
        }

        argumentCaptor<List<DataServiceDBO>>().apply {
            verify(dataServiceRepository, times(0)).saveAll(capture())
        }
    }

}