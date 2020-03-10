package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.CatalogFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.DataServiceFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.addDefaultPrefixes
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createRDFResponse
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Service

@Service
class CatalogService(private val catalogFuseki: CatalogFuseki) {

    fun countDataServiceCatalogs(): Int =
        catalogFuseki.fetchCompleteModel()
            .listResourcesWithProperty(RDF.type, DCAT.Catalog)
            .toList()
            .size

    fun getAllDataServiceCatalogs(returnType: JenaType): String =
        catalogFuseki
            .fetchCompleteModel()
            .addDefaultPrefixes()
            .createRDFResponse(returnType)

    fun getDataServiceCatalog(id: String, returnType: JenaType): String? =
        catalogFuseki
            .fetchByGraphName(id)
            .addDefaultPrefixes()
            .createRDFResponse(returnType)

}