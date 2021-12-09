package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.Application
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.containsTriple
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.isResourceProperty
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.parseRDFResponse
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement
import org.apache.jena.riot.Lang
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger(Application::class.java)

fun CatalogAndDataServiceModels.harvestDiff(dbNoRecords: String?): Boolean =
    if (dbNoRecords == null) true
    else !harvestedCatalog.isIsomorphicWith(parseRDFResponse(dbNoRecords, Lang.TURTLE, null))

fun DataServiceModel.harvestDiff(dbNoRecords: String?): Boolean =
    if (dbNoRecords == null) true
    else !harvestedService.isIsomorphicWith(parseRDFResponse(dbNoRecords, Lang.TURTLE, null))

fun splitCatalogsFromModel(harvested: Model, sourceURL: String): List<CatalogAndDataServiceModels> =
    harvested.listResourcesWithProperty(RDF.type, DCAT.Catalog)
        .toList()
        .filterBlankNodeCatalogsAndServices(sourceURL)
        .map { catalogResource ->
            val catalogServices: List<DataServiceModel> = catalogResource.listProperties(DCAT.service)
                .toList()
                .map { it.resource }
                .filterBlankNodeCatalogsAndServices(sourceURL)
                .map { it.extractDataService() }

            val catalogModelWithoutServices = ModelFactory.createDefaultModel()
            catalogModelWithoutServices.setNsPrefixes(harvested.nsPrefixMap)

            catalogResource.listProperties()
                .toList()
                .forEach { catalogModelWithoutServices.addCatalogProperties(it) }

            var catalogModel = catalogModelWithoutServices
            catalogServices.forEach { catalogModel = catalogModel.union(it.harvestedService) }

            CatalogAndDataServiceModels(
                resource = catalogResource,
                harvestedCatalog = catalogModel,
                harvestedCatalogWithoutDatasets = catalogModelWithoutServices,
                services = catalogServices
            )
        }

private fun List<Resource>.filterBlankNodeCatalogsAndServices(sourceURL: String): List<Resource> =
    filter {
        if (it.isURIResource) true
        else {
            LOGGER.error(
                "Failed harvest of catalog or data service for $sourceURL, unable to harvest blank node catalogs and data services",
                Exception("unable to harvest blank node catalogs and data services")
            )
            false
        }
    }

private fun Model.addCatalogProperties(property: Statement): Model =
    when {
        property.predicate != DCAT.service && property.isResourceProperty() ->
            add(property).recursiveAddNonDataServiceResource(property.resource, 5)
        property.predicate != DCAT.service -> add(property)
        property.isResourceProperty() && property.resource.isURIResource -> add(property)
        else -> this
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
        !resource.isURIResource -> true
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
