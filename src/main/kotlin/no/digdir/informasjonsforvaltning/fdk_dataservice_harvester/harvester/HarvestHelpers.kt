package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.Application
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.containsTriple
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createIdFromString
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createRDFResponse
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.isResourceProperty
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.parseRDFResponse
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement
import org.apache.jena.riot.Lang
import org.apache.jena.util.ResourceUtils
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.slf4j.LoggerFactory
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private val LOGGER = LoggerFactory.getLogger(Application::class.java)
private const val dateFormat: String = "yyyy-MM-dd HH:mm:ss Z"

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

            val catalogModelWithoutServices = catalogResource.extractCatalogModel()
                .recursiveBlankNodeSkolem(catalogResource.uri)

            val catalogModel = ModelFactory.createDefaultModel()
            catalogServices.forEach { catalogModel.add(it.harvestedService) }

            CatalogAndDataServiceModels(
                resource = catalogResource,
                harvestedCatalog = catalogModel.union(catalogModelWithoutServices),
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

fun Resource.extractCatalogModel(): Model {
    val catalogModelWithoutServices = ModelFactory.createDefaultModel()
    catalogModelWithoutServices.setNsPrefixes(model.nsPrefixMap)
    listProperties()
        .toList()
        .forEach { catalogModelWithoutServices.addCatalogProperties(it) }
    return catalogModelWithoutServices
}

private fun Model.addCatalogProperties(property: Statement): Model =
    when {
        property.predicate != DCAT.service && property.isResourceProperty() ->
            add(property).recursiveAddNonDataServiceResource(property.resource)
        property.predicate != DCAT.service -> add(property)
        property.isResourceProperty() && property.resource.isURIResource -> add(property)
        else -> this
    }

fun Resource.extractDataService(): DataServiceModel {
    val serviceModel = listProperties().toModel()
    serviceModel.setNsPrefixes(model.nsPrefixMap)

    listProperties().toList()
        .filter { it.isResourceProperty() }
        .forEach {
            serviceModel.recursiveAddNonDataServiceResource(it.resource)
        }

    return DataServiceModel(resource = this, harvestedService = serviceModel.recursiveBlankNodeSkolem(uri))
}

private fun Model.recursiveAddNonDataServiceResource(resource: Resource): Model {
    if (resourceShouldBeAdded(resource)) {
        add(resource.listProperties())

        resource.listProperties().toList()
            .filter { it.isResourceProperty() }
            .forEach { recursiveAddNonDataServiceResource(it.resource) }
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

private fun Model.recursiveBlankNodeSkolem(baseURI: String): Model {
    val anonSubjects = listSubjects().toList().filter { it.isAnon }
    return if (anonSubjects.isEmpty()) this
    else {
        anonSubjects
            .filter { it.doesNotContainAnon() }
            .forEach {
                ResourceUtils.renameResource(it, "$baseURI/.well-known/skolem/${it.createSkolemID()}")
            }
        this.recursiveBlankNodeSkolem(baseURI)
    }
}

fun formatNowWithOsloTimeZone(): String =
    ZonedDateTime.now(ZoneId.of("Europe/Oslo"))
        .format(DateTimeFormatter.ofPattern(dateFormat))

fun Calendar.formatWithOsloTimeZone(): String =
    ZonedDateTime.from(toInstant().atZone(ZoneId.of("Europe/Oslo")))
        .format(DateTimeFormatter.ofPattern(dateFormat))

private fun Resource.doesNotContainAnon(): Boolean =
    listProperties().toList()
        .filter { it.isResourceProperty() }
        .map { it.resource }
        .filter { it.listProperties().toList().size > 0 }
        .none { it.isAnon }

private fun Resource.createSkolemID(): String =
    createIdFromString(
        listProperties().toModel()
            .createRDFResponse(Lang.N3)
            .replace("\\s".toRegex(), "")
            .toCharArray()
            .sorted()
            .toString()
    )

class HarvestException(url: String) : Exception("Harvest failed for $url")
