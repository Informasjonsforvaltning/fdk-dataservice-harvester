package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.DataServiceAdapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.ApplicationProperties
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.MetaFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.HarvestFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createRDFResponse
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.queryToGetMetaDataByUri
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATASERVICE_META_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.TEST_HARVEST_DATE
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.TEST_HARVEST_SOURCE
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.TestResponseReader
import org.apache.jena.rdf.model.Model
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

@Tag("unit")
class HarvesterTest {
    private val harvestFuseki: HarvestFuseki = mock()
    private val metaFuseki: MetaFuseki = mock()
    private val valuesMock: ApplicationProperties = mock()
    private val adapter: DataServiceAdapter = mock()

    private val harvester = DataServiceHarvester(adapter, harvestFuseki, metaFuseki, valuesMock)

    private val responseReader = TestResponseReader()

    @Test
    fun harvestDataSource() {
        whenever(adapter.getDataServices(TEST_HARVEST_SOURCE))
            .thenReturn(javaClass.classLoader.getResourceAsStream("harvest_response.ttl").reader().readText())

        whenever(valuesMock.catalogUri)
            .thenReturn("http://localhost:5000/catalogs")
        whenever(valuesMock.dataserviceUri)
            .thenReturn("http://localhost:5000/dataservices")

        whenever(metaFuseki.queryDescribe(queryToGetMetaDataByUri("https://testdirektoratet.no/model/dataservice-catalogs/0")))
            .thenReturn(responseReader.parseFile("no_prefix_catalog_meta_0_pre_harvest.ttl", "TURTLE"))

        val expectedSavedHarvest = responseReader.parseFile("harvest_response.ttl", "TURTLE")
        val expectedCatalogMetaData = responseReader.parseFile("no_prefix_catalog_meta_0_post_harvest.ttl", "TURTLE")
        val expectedDataServiceMetaData = responseReader.parseResponse(DATASERVICE_META_0, "TURTLE")

        harvester.harvestDataServiceCatalog(TEST_HARVEST_SOURCE, TEST_HARVEST_DATE)

        argumentCaptor<Model>().apply {
            verify(metaFuseki, times(2)).saveWithGraphName(any(), capture())
            assertTrue(firstValue.isIsomorphicWith(expectedCatalogMetaData))
            assertTrue(lastValue.isIsomorphicWith(expectedDataServiceMetaData))
        }

        argumentCaptor<Model>().apply {
            verify(harvestFuseki, times(1)).saveWithGraphName(any(), capture())
            assertTrue(firstValue.isIsomorphicWith(expectedSavedHarvest))
        }
    }

    @Test
    fun harvestWithErrorsIsNotPersisted() {
        whenever(adapter.getDataServices(TEST_HARVEST_SOURCE))
            .thenReturn(javaClass.classLoader.getResourceAsStream("harvest_response_with_errors.ttl").reader().readText())

        whenever(valuesMock.catalogUri)
            .thenReturn("http://localhost:5000/catalogs")
        whenever(valuesMock.dataserviceUri)
            .thenReturn("http://localhost:5000/dataservices")

        whenever(metaFuseki.queryDescribe(queryToGetMetaDataByUri("https://testdirektoratet.no/model/dataservice-catalogs/0")))
            .thenReturn(responseReader.parseFile("no_prefix_catalog_meta_0_pre_harvest.ttl", "TURTLE"))

        harvester.harvestDataServiceCatalog(TEST_HARVEST_SOURCE, TEST_HARVEST_DATE)

        argumentCaptor<Model>().apply {
            verify(metaFuseki, times(0)).saveWithGraphName(any(), capture())
        }

        argumentCaptor<Model>().apply {
            verify(harvestFuseki, times(0)).saveWithGraphName(any(), capture())
        }
    }

}