package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.DataServiceAdapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.ApplicationProperties
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.*
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.*
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.CatalogRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.DataServiceRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.TurtleService
import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.Lang
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private val LOGGER = LoggerFactory.getLogger(DataServiceHarvester::class.java)
private const val dateFormat: String = "yyyy-MM-dd HH:mm:ss Z"

@Service
class DataServiceHarvester(
    private val adapter: DataServiceAdapter,
    private val catalogRepository: CatalogRepository,
    private val dataServiceRepository: DataServiceRepository,
    private val turtleService: TurtleService,
    private val applicationProperties: ApplicationProperties
) {

    fun harvestDataServiceCatalog(source: HarvestDataSource, harvestDate: Calendar, forceUpdate: Boolean): HarvestReport? =
        if (source.id != null && source.url != null) {
            try {
                LOGGER.debug("Starting harvest of ${source.url}")

                when (val jenaWriterType = jenaTypeFromAcceptHeader(source.acceptHeaderValue)) {
                    null -> {
                        LOGGER.error(
                            "Not able to harvest from ${source.url}, no accept header supplied",
                            HarvestException(source.url)
                        )
                        HarvestReport(
                            id = source.id,
                            url = source.url,
                            harvestError = true,
                            errorMessage = "Not able to harvest, no accept header supplied",
                            startTime = harvestDate.formatWithOsloTimeZone(),
                            endTime = formatNowWithOsloTimeZone()
                        )
                    }
                    Lang.RDFNULL -> {
                        LOGGER.error(
                            "Not able to harvest from ${source.url}, header ${source.acceptHeaderValue} is not acceptable",
                            HarvestException(source.url)
                        )
                        HarvestReport(
                            id = source.id,
                            url = source.url,
                            harvestError = true,
                            errorMessage = "Not able to harvest, no accept header supplied",
                            startTime = harvestDate.formatWithOsloTimeZone(),
                            endTime = formatNowWithOsloTimeZone()
                        )
                    }
                    else -> updateIfChanged(
                        parseRDFResponse(adapter.getDataServices(source), jenaWriterType, source.url),
                        source.id, source.url, harvestDate, forceUpdate
                    )
                }
            } catch (ex: Exception) {
                LOGGER.error("Harvest of ${source.url} failed", ex)
                HarvestReport(
                    id = source.id,
                    url = source.url,
                    harvestError = true,
                    errorMessage = ex.message,
                    startTime = harvestDate.formatWithOsloTimeZone(),
                    endTime = formatNowWithOsloTimeZone()
                )
            }
        } else {
            LOGGER.error("Harvest source is not valid", HarvestException("source not valid"))
            null
        }

    private fun updateIfChanged(harvested: Model, sourceId: String, sourceURL: String, harvestDate: Calendar, forceUpdate: Boolean): HarvestReport {
        val dbData = turtleService.getHarvestSource(sourceURL)
            ?.let { parseRDFResponse(it, Lang.TURTLE, null) }

        return if (!forceUpdate && dbData != null && harvested.isIsomorphicWith(dbData)) {
            LOGGER.info("No changes from last harvest of $sourceURL")
            HarvestReport(
                id = sourceId,
                url = sourceURL,
                harvestError = false,
                startTime = harvestDate.formatWithOsloTimeZone(),
                endTime = formatNowWithOsloTimeZone()
            )
        } else {
            LOGGER.info("Changes detected, saving data from $sourceURL")
            turtleService.saveAsHarvestSource(harvested, sourceURL)

            updateDB(harvested, harvestDate, sourceId, sourceURL, forceUpdate)
        }
    }

    private fun updateDB(harvested: Model, harvestDate: Calendar, sourceId: String, sourceURL: String, forceUpdate: Boolean): HarvestReport {
        val updatedCatalogs = mutableListOf<CatalogMeta>()
        val updatedServices = mutableListOf<DataServiceMeta>()
        val removedServices = mutableListOf<DataServiceMeta>()
        splitCatalogsFromModel(harvested, sourceURL)
            .map { Pair(it, catalogRepository.findByIdOrNull(it.resource.uri)) }
            .filter { forceUpdate || it.first.catalogHasChanges(it.second?.fdkId) }
            .forEach {
                val dbMeta = it.second
                val catalogMeta = if (dbMeta == null || it.first.catalogHasChanges(dbMeta.fdkId)) {
                    it.first.mapToCatalogMeta(harvestDate, it.second)
                        .also { updatedMeta -> catalogRepository.save(updatedMeta) }
                } else dbMeta

                updatedCatalogs.add(catalogMeta)

                turtleService.saveAsCatalog(
                    model = it.first.harvestedCatalog,
                    fdkId = catalogMeta.fdkId,
                    withRecords = false
                )

                val fdkUri = "${applicationProperties.catalogUri}/${catalogMeta.fdkId}"

                it.first.services.forEach { service ->
                    service.updateDBOs(harvestDate, fdkUri, forceUpdate)
                        ?.let { serviceMeta -> updatedServices.add(serviceMeta) }
                }

                removedServices.addAll(
                    getDataServicesRemovedThisHarvest(
                        fdkUri,
                        it.first.services.map { service -> service.resource.uri }
                    )
                )
            }
        removedServices.map { it.copy(removed = true) }.run { dataServiceRepository.saveAll(this) }
        LOGGER.debug("Harvest of $sourceURL completed")
        return HarvestReport(
            id = sourceId,
            url = sourceURL,
            harvestError = false,
            startTime = harvestDate.formatWithOsloTimeZone(),
            endTime = formatNowWithOsloTimeZone(),
            changedCatalogs = updatedCatalogs.map { FdkIdAndUri(fdkId = it.fdkId, uri = it.uri) },
            changedResources = updatedServices.map { FdkIdAndUri(fdkId = it.fdkId, uri = it.uri) },
            removedResources = removedServices.map { FdkIdAndUri(fdkId = it.fdkId, uri = it.uri) }
        )
    }

    private fun DataServiceModel.updateDBOs(
        harvestDate: Calendar,
        fdkCatalogURI: String,
        forceUpdate: Boolean
    ): DataServiceMeta? {
        val dbMeta = dataServiceRepository.findByIdOrNull(resource.uri)
        if (forceUpdate || serviceHasChanges(dbMeta?.fdkId)) {

            val modelMeta = if (dbMeta == null || serviceHasChanges(dbMeta.fdkId)) {
                mapToMetaDBO(harvestDate, fdkCatalogURI, dbMeta)
                    .also { updatedMeta -> dataServiceRepository.save(updatedMeta) }
            } else dbMeta

            turtleService.saveAsDataService(
                model = harvestedService,
                fdkId = modelMeta.fdkId,
                withRecords = false
            )
            return modelMeta
        } else return null
    }

    private fun CatalogAndDataServiceModels.mapToCatalogMeta(
        harvestDate: Calendar,
        dbMeta: CatalogMeta?
    ): CatalogMeta {
        val catalogURI = resource.uri
        val fdkId = dbMeta?.fdkId ?: createIdFromString(catalogURI)
        val issued = dbMeta?.issued
            ?.let { timestamp -> calendarFromTimestamp(timestamp) }
            ?: harvestDate

        return CatalogMeta(
            uri = catalogURI,
            fdkId = fdkId,
            issued = issued.timeInMillis,
            modified = harvestDate.timeInMillis
        )
    }

    private fun DataServiceModel.mapToMetaDBO(
        harvestDate: Calendar,
        catalogURI: String,
        dbMeta: DataServiceMeta?
    ): DataServiceMeta {
        val fdkId = dbMeta?.fdkId ?: createIdFromString(resource.uri)
        val issued: Calendar = dbMeta?.issued
            ?.let { timestamp -> calendarFromTimestamp(timestamp) }
            ?: harvestDate

        return DataServiceMeta(
            uri = resource.uri,
            fdkId = fdkId,
            isPartOf = catalogURI,
            issued = issued.timeInMillis,
            modified = harvestDate.timeInMillis
        )
    }

    private fun CatalogAndDataServiceModels.catalogHasChanges(fdkId: String?): Boolean =
        if (fdkId == null) true
        else harvestDiff(turtleService.getCatalog(fdkId, withRecords = false))

    private fun DataServiceModel.serviceHasChanges(fdkId: String?): Boolean =
        if (fdkId == null) true
        else harvestDiff(turtleService.getDataService(fdkId, withRecords = false))

    private fun formatNowWithOsloTimeZone(): String =
        ZonedDateTime.now(ZoneId.of("Europe/Oslo"))
            .format(DateTimeFormatter.ofPattern(dateFormat))

    private fun Calendar.formatWithOsloTimeZone(): String =
        ZonedDateTime.from(toInstant().atZone(ZoneId.of("Europe/Oslo")))
            .format(DateTimeFormatter.ofPattern(dateFormat))

    private fun getDataServicesRemovedThisHarvest(catalog: String, services: List<String>): List<DataServiceMeta> =
        dataServiceRepository.findAllByIsPartOf(catalog)
            .filter { !it.removed && !services.contains(it.uri) }
}
