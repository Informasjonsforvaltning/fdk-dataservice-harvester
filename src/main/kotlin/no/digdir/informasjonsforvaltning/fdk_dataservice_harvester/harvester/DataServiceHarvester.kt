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
import java.util.*

private val LOGGER = LoggerFactory.getLogger(DataServiceHarvester::class.java)

@Service
class DataServiceHarvester(
    private val adapter: DataServiceAdapter,
    private val catalogRepository: CatalogRepository,
    private val dataServiceRepository: DataServiceRepository,
    private val turtleService: TurtleService,
    private val applicationProperties: ApplicationProperties
) {

    fun harvestDataServiceCatalog(source: HarvestDataSource, harvestDate: Calendar) =
        if (source.url != null) {
            LOGGER.debug("Starting harvest of ${source.url}")
            val jenaWriterType = jenaTypeFromAcceptHeader(source.acceptHeaderValue)

            val harvested = when (jenaWriterType) {
                null -> null
                Lang.RDFNULL -> null
                else -> adapter.getDataServices(source)?.let { parseRDFResponse(it, jenaWriterType, source.url) }
            }

            when {
                jenaWriterType == null -> LOGGER.error(Exception("Not able to harvest from ${source.url}, no accept header supplied").stackTraceToString())
                jenaWriterType == Lang.RDFNULL -> LOGGER.error(Exception("Not able to harvest from ${source.url}, header ${source.acceptHeaderValue} is not acceptable").stackTraceToString())
                harvested == null -> LOGGER.info("Not able to harvest ${source.url}")
                else -> checkHarvestedContainsChanges(harvested, source.url, harvestDate)
            }
        } else LOGGER.error(Exception("Harvest source is not defined").stackTraceToString())

    private fun checkHarvestedContainsChanges(harvested: Model, sourceURL: String, harvestDate: Calendar) {
        val dbId = createIdFromUri(sourceURL)
        val dbData = turtleService.getHarvestSource(sourceURL)
            ?.let { parseRDFResponse(it, Lang.TURTLE, null) }

        if (dbData != null && harvested.isIsomorphicWith(dbData)) {
            LOGGER.info("No changes from last harvest of $sourceURL")
        } else {
            LOGGER.info("Changes detected, saving data from $sourceURL on graph $dbId, and updating FDK meta data")
            turtleService.saveAsHarvestSource(harvested, sourceURL)

            updateDB(harvested, harvestDate)
        }
    }

    private fun updateDB(harvested: Model, harvestDate: Calendar) {
        splitCatalogsFromModel(harvested)
            .map { Pair(it, catalogRepository.findByIdOrNull(it.resource.uri)) }
            .filter { it.first.catalogHasChanges(it.second?.fdkId) }
            .forEach {
                val updatedCatalogMeta = it.first.mapToCatalogMeta(harvestDate, it.second)
                catalogRepository.save(updatedCatalogMeta)

                turtleService.saveAsCatalog(
                    model = it.first.harvestedCatalog,
                    fdkId = updatedCatalogMeta.fdkId,
                    withRecords = false
                )

                val fdkUri = "${applicationProperties.catalogUri}/${updatedCatalogMeta.fdkId}"

                it.first.services.forEach { service ->
                    service.updateDBOs(harvestDate, fdkUri)
                }
            }
    }

    private fun DataServiceModel.updateDBOs(
        harvestDate: Calendar,
        fdkCatalogURI: String
    ) {
        val dbMeta = dataServiceRepository.findByIdOrNull(resource.uri)
        if (serviceHasChanges(dbMeta?.fdkId)) {
            val modelMeta = mapToMetaDBO(harvestDate, fdkCatalogURI, dbMeta)
            dataServiceRepository.save(modelMeta)

            turtleService.saveAsDataService(
                model = harvestedService,
                fdkId = modelMeta.fdkId,
                withRecords = false
            )
        }
    }

    private fun CatalogAndDataServiceModels.mapToCatalogMeta(
        harvestDate: Calendar,
        dbMeta: CatalogMeta?
    ): CatalogMeta {
        val catalogURI = resource.uri
        val fdkId = dbMeta?.fdkId ?: createIdFromUri(catalogURI)
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
        val fdkId = dbMeta?.fdkId ?: createIdFromUri(resource.uri)
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
}
