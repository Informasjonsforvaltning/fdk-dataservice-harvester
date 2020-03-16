package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.DataServiceAdapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.ApplicationProperties
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.CatalogFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.DataServiceFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.TEST_HARVEST_DATE
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.TestResponseReader
import org.apache.jena.rdf.model.Model
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue


@Tag("unit")
class HarvesterTest {
    private val dataServiceFuseki: DataServiceFuseki = mock()
    private val catalogFuseki: CatalogFuseki = mock()
    private val valuesMock: ApplicationProperties = mock()
    private val adapter: DataServiceAdapter = mock()

    private val harvester = DataServiceHarvester(adapter, dataServiceFuseki, catalogFuseki, valuesMock)

    private val responseReader = TestResponseReader()

    @Test
    fun harvestDataSource() {
        whenever(adapter.getDataServiceCatalog("harvest-url"))
            .thenReturn(javaClass.classLoader.getResourceAsStream("harvest_response.ttl").reader().readText())

        whenever(valuesMock.catalogUri)
            .thenReturn("https://dataservice-harvester.staging.fellesdatakatalog.digdir.no/catalogs")
        whenever(valuesMock.dataserviceUri)
            .thenReturn("https://dataservice-harvester.staging.fellesdatakatalog.digdir.no/dataservices")

        val expectedCatalog = responseReader.parseFile("no_prefix_catalog_0.ttl", "TURTLE")
        val expectedDataService = responseReader.parseFile("no_prefix_dataservice_0.ttl", "TURTLE")

        harvester.harvestDataServiceCatalog("harvest-url", TEST_HARVEST_DATE)

        argumentCaptor<Model>().apply {
            verify(catalogFuseki, times(1)).saveWithGraphName(any(), capture())

            assertTrue(firstValue.isIsomorphicWith(expectedCatalog))
        }

        argumentCaptor<Model>().apply {
            verify(dataServiceFuseki, times(1)).saveWithGraphName(any(), capture())

            assertTrue(firstValue.isIsomorphicWith(expectedDataService))
        }

    }

}