package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.FusekiConnection
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester.DataserviceHarvester
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Service

@Service
class DataserviceService(
    private val dataserviceHarvester: DataserviceHarvester,
    private val fusekiConnection: FusekiConnection
) {

    fun countDataserviceCatalogss(): Int =
        fusekiConnection.fetchCompleteModel()
            .listResourcesWithProperty(RDF.type, DCAT.Catalog)
            .toList()
            .size

}