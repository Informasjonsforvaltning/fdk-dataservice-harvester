package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.DcatApNo2Adapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.generated.model.Catalog
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.parseCatalog
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.parseRDFResponse
import org.apache.jena.rdf.model.StmtIterator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val LOGGER = LoggerFactory.getLogger(DcatApNo2Adapter::class.java)

@Service
class DataserviceHarvester(private val dcatApNo2Adapter: DcatApNo2Adapter) {

    fun harvestDataservices() : Catalog? {
        val url = "https://raw.githubusercontent.com/Informasjonsforvaltning/dataservice-publisher/master/tests/catalog_2.ttl"
        val responseBody = dcatApNo2Adapter.getDatasourceCatalog(url)
        val responseModel = parseRDFResponse(responseBody!!, JenaType.TURTLE)
        return responseModel.parseCatalog()
    }
}