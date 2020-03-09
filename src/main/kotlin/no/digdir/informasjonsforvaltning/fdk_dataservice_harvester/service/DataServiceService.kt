package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.FusekiConnection
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.addDefaultPrefixes
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createRDFResponse
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Service

@Service
class DataServiceService(private val fusekiConnection: FusekiConnection) {

    fun countDataServiceCatalogs(): Int =
        fusekiConnection.fetchCompleteModel()
            .listResourcesWithProperty(RDF.type, DCAT.Catalog)
            .toList()
            .size

    fun getAllDataServiceCatalogs(returnType: JenaType): Any =
        fusekiConnection
            .fetchCompleteModel()
            .addDefaultPrefixes()
            .createRDFResponse(returnType)

    fun getDataServiceCatalog(id: String, returnType: JenaType): Any? =
        fusekiConnection
            .fetchByGraphName(id)
            .addDefaultPrefixes()
            .createRDFResponse(returnType)

}