package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.CatalogDBO
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.DataServiceDBO
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.addDefaultPrefixes
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.isResourceProperty
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.parseRDFResponse
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.ungzip
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


fun CatalogAndDataServiceModels.catalogDiffersFromDB(dbo: CatalogDBO?): Boolean =
    if (dbo == null) true
    else !harvestedCatalog.isIsomorphicWith(parseRDFResponse(ungzip(dbo.turtleHarvested), JenaType.TURTLE, null))

fun DataServiceModel.differsFromDB(dbo: DataServiceDBO): Boolean =
    !harvestedService.isIsomorphicWith(parseRDFResponse(ungzip(dbo.turtleHarvested), JenaType.TURTLE, null))

fun Resource.parsePropertyToCalendar(property: Property): List<Calendar>? =
    listProperties(property)
        ?.toList()
        ?.mapNotNull { it.string }
        ?.map {
            val cal = Calendar.getInstance()
            val ldt = LocalDateTime.parse(it, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            cal.time = Date.from(ldt.atZone(ZoneId.of("Z")).toInstant())
            cal
        }

fun splitCatalogsFromModel(harvested: Model): List<CatalogAndDataServiceModels> =
    harvested.listResourcesWithProperty(RDF.type, DCAT.Catalog)
        .toList()
        .map { catalogResource ->
            val catalogServices: List<DataServiceModel> = catalogResource.listProperties(DCAT.service)
                .toList()
                .map { dataset -> dataset.resource.extractDataService() }

            var catalogModelWithoutServices = catalogResource.listProperties().toModel()
            catalogModelWithoutServices.addDefaultPrefixes()

            catalogResource.listProperties().toList()
                .filter { it.isResourceProperty() }
                .forEach {
                    if (it.predicate != DCAT.service) {
                        catalogModelWithoutServices = catalogModelWithoutServices.addNonDataServiceResourceToModel(it.resource)
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
    serviceModel = serviceModel.addDefaultPrefixes()

    listProperties().toList()
        .filter { it.isResourceProperty() }
        .forEach {
            serviceModel = serviceModel.addNonDataServiceResourceToModel(it.resource)
        }

    return DataServiceModel(resource = this, harvestedService = serviceModel)
}

private fun Model.addNonDataServiceResourceToModel(resource: Resource): Model {
    val types = resource.listProperties(RDF.type)
        .toList()
        .map { it.`object` }

    if (!types.contains(DCAT.DataService)) {

        add(resource.listProperties())

        resource.listProperties().toList()
            .filter { it.isResourceProperty() }
            .forEach {
                add(it.resource.listProperties())
            }
    }

    return this
}

fun calendarFromTimestamp(timestamp: Long): Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    return calendar
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