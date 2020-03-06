package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.FusekiConnection
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.generated.model.Catalog
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.parseCatalogs
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Service

@Service
class DataserviceService(private val fusekiConnection: FusekiConnection) {

    fun countDataserviceCatalogss(): Int =
        fusekiConnection.fetchCompleteModel()
            .listResourcesWithProperty(RDF.type, DCAT.Catalog)
            .toList()
            .size

    fun getAllCatalogs(): List<Catalog> =
        fusekiConnection
            .fetchCompleteModel()
            .parseCatalogs()

}