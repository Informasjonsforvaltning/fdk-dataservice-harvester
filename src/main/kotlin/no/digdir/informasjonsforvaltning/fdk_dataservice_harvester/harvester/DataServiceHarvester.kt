package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.DataServiceAdapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.FusekiConnection
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
    private val fuseki: FusekiConnection,
    private val metaDataService: MetaDataService
) {

    fun harvestDataServiceCatalog(url: String) {
        adapter.getDataServiceCatalog(url)
            ?.let { parseRDFResponse(it, JenaType.TURTLE) }
            ?.let { metaDataService.addMetaDataToModel(it) }
            ?.run {
                fuseki.updateFullModel(this)
                listOfCatalogResources().forEach{ fuseki.saveWithGraphName(it.extractMetaDataIdentifier(), it.createCatalogModel()) }
                listOfDataServiceResources().forEach{ fuseki.saveWithGraphName(it.extractMetaDataIdentifier(), it.createDataserviceModel()) }
            }
    }

}