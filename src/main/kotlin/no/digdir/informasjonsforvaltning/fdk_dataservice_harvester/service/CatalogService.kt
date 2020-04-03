package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.CatalogFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.DataServiceFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.addDefaultPrefixes
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createIdFromUri
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createRDFResponse
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.listOfCatalogResources
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Service

@Service
class CatalogService(
    private val catalogFuseki: CatalogFuseki,
    private val dataServiceFuseki: DataServiceFuseki
) {

    fun countDataServiceCatalogs(): Int =
        catalogFuseki.fetchCompleteModel()
            .listResourcesWithProperty(RDF.type, DCAT.Catalog)
            .toList()
            .size

    fun getAllDataServiceCatalogs(returnType: JenaType): String =
        catalogFuseki
            .fetchCompleteModel()
            .addDefaultPrefixes()
            .addAllDataServices()
            .createRDFResponse(returnType)

    fun getDataServiceCatalog(id: String, returnType: JenaType): String? =
        catalogFuseki
            .fetchByGraphName(id)
            ?.addDefaultPrefixes()
            ?.addDataServices()
            ?.createRDFResponse(returnType)

    private fun Model.addAllDataServices(): Model =
        union(dataServiceFuseki.fetchCompleteModel())

    private fun Model.addDataServices(): Model {

        var unionModel = ModelFactory.createDefaultModel().union(this)

        val dataServiceIdList = mutableListOf<String>()

        listOfCatalogResources()
            .toList()
            .forEach { catalog ->
                catalog.listProperties(DCAT.service)
                    .toList()
                    .forEach { dataService ->
                        dataServiceIdList.add(createIdFromUri(dataService.resource.uri))
                    } }

        dataServiceIdList
            .toList()
            .mapNotNull { id -> dataServiceFuseki.fetchByGraphName(id) }
            .forEach { unionModel = unionModel.union(it) }

        return unionModel
    }

}