package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.DataserviceAdapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.FusekiConnection
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.parseRDFResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val LOGGER = LoggerFactory.getLogger(DataserviceHarvester::class.java)

@Service
class DataserviceHarvester(private val adapter: DataserviceAdapter, private val fuseki: FusekiConnection) {

    fun harvestDataservices() {
        val url = "https://raw.githubusercontent.com/Informasjonsforvaltning/dataservice-publisher/master/tests/catalog_2.ttl"
        adapter.getDataserviceCatalog(url)
            ?.let { parseRDFResponse(it, JenaType.TURTLE) }
            ?.let { fuseki.updateModel(it) }
    }
}