package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.DataServiceAdapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.CatalogFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.DataServiceFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createCatalogModel
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createDataserviceModel
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.extractMetaDataIdentifier
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.listOfCatalogResources
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.listOfDataServiceResources
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.parseRDFResponse
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.MetaDataService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val LOGGER = LoggerFactory.getLogger(DataServiceHarvester::class.java)

@Service
class DataServiceHarvester(
    private val adapter: DataServiceAdapter,
    private val dataServiceFuseki: DataServiceFuseki,
    private val catalogFuseki: CatalogFuseki,
    private val metaDataService: MetaDataService
) {

    fun harvestDataServiceCatalog(url: String) {
        adapter.getDataServiceCatalog(url)
            ?.let { parseRDFResponse(it, JenaType.TURTLE) }
            ?.let { metaDataService.addMetaDataToModel(it) }
            ?.run {
                listOfCatalogResources().forEach{ catalogFuseki.saveWithGraphName(it.extractMetaDataIdentifier(), it.createCatalogModel()) }
                listOfDataServiceResources().forEach{ dataServiceFuseki.saveWithGraphName(it.extractMetaDataIdentifier(), it.createDataserviceModel()) }
            }
    }

}