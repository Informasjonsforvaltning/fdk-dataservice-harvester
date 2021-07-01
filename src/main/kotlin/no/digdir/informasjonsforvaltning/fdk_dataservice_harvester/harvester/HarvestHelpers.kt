package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.containsTriple
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.isResourceProperty
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.parseRDFResponse
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.riot.Lang
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF


fun CatalogAndDataServiceModels.harvestDiff(dbNoRecords: String?): Boolean =
    if (dbNoRecords == null) true
    else !harvestedCatalog.isIsomorphicWith(parseRDFResponse(dbNoRecords, Lang.TURTLE, null))

fun DataServiceModel.harvestDiff(dbNoRecords: String?): Boolean =
    if (dbNoRecords == null) true
    else !harvestedService.isIsomorphicWith(parseRDFResponse(dbNoRecords, Lang.TURTLE, null))

fun splitCatalogsFromModel(harvested: Model): List<CatalogAndDataServiceModels> =
    harvested.listResourcesWithProperty(RDF.type, DCAT.Catalog)
        .toList()
        .map { catalogResource ->
            val catalogServices: List<DataServiceModel> = catalogResource.listProperties(DCAT.service)
                .toList()
                .map { dataset -> dataset.resource.extractDataService() }

            var catalogModelWithoutServices = catalogResource.listProperties().toModel()
            catalogModelWithoutServices.setNsPrefixes(harvested.nsPrefixMap)

            catalogResource.listProperties().toList()
                .filter { it.isResourceProperty() }
                .forEach {
                    if (it.predicate != DCAT.service) {
                        catalogModelWithoutServices = catalogModelWithoutServices.recursiveAddNonDataServiceResource(it.resource, 5)
                    }
                }

            var catalogModel = catalogModelWithoutServices
            catalogServices.forEach { catalogModel = catalogModel.union(it.harvestedService) }

            CatalogAndDataServiceModels(
                resource = catalogResource,
                harvestedCatalog = catalogModel,
                harvestedCatalogWithoutDatasets = catalogModelWithoutServices,
                services = catalogServices
            )
        }

fun Resource.extractDataService(): DataServiceModel {
    var serviceModel = listProperties().toModel()
    serviceModel.setNsPrefixes(model.nsPrefixMap)

    listProperties().toList()
        .filter { it.isResourceProperty() }
        .forEach {
            serviceModel = serviceModel.recursiveAddNonDataServiceResource(it.resource, 5)
        }

    return DataServiceModel(resource = this, harvestedService = serviceModel)
}

private fun Model.recursiveAddNonDataServiceResource(resource: Resource, recursiveCount: Int): Model {
    val newCount = recursiveCount - 1

    if (resourceShouldBeAdded(resource)) {
        add(resource.listProperties())

        if (newCount > 0) {
            resource.listProperties().toList()
                .filter { it.isResourceProperty() }
                .forEach { recursiveAddNonDataServiceResource(it.resource, newCount) }
        }
    }

    return this
}

private fun Model.resourceShouldBeAdded(resource: Resource): Boolean {
    val types = resource.listProperties(RDF.type)
        .toList()
        .map { it.`object` }

    return when {
        types.contains(DCAT.DataService) -> false
        containsTriple("<${resource.uri}>", "a", "?o") -> false
        else -> true
    }
}

data class CatalogAndDataServiceModels (
    val resource: Resource,
    val harvestedCatalog: Model,
    val harvestedCatalogWithoutDatasets: Model,
    val services: List<DataServiceModel>,
)

data class DataServiceModel (
    val resource: Resource,
    val harvestedService: Model
)

class HarvestException(url: String) : Exception("Harvest failed for $url")
