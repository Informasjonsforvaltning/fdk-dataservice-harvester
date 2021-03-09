package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.DataServiceAdapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.FusekiAdapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.ApplicationProperties
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.*
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.*
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.CatalogRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.DataServiceRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.MiscellaneousRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.gzip
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.ungzip
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

private val LOGGER = LoggerFactory.getLogger(DataServiceHarvester::class.java)

@Service
class DataServiceHarvester(
    private val adapter: DataServiceAdapter,
    private val fusekiAdapter: FusekiAdapter,
    private val catalogRepository: CatalogRepository,
    private val dataServiceRepository: DataServiceRepository,
    private val miscRepository: MiscellaneousRepository,
    private val applicationProperties: ApplicationProperties
) {

    fun updateUnionModel() {
        var unionModel = ModelFactory.createDefaultModel()

        catalogRepository.findAll()
            .map { parseRDFResponse(ungzip(it.turtleCatalog), Lang.TURTLE, null) }
            .forEach { unionModel = unionModel.union(it) }

        fusekiAdapter.storeUnionModel(unionModel)

        miscRepository.save(
            MiscellaneousTurtle(
                id = UNION_ID,
                isHarvestedSource = false,
                turtle = gzip(unionModel.createRDFResponse(Lang.TURTLE))
            )
        )
    }

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
                jenaWriterType == null -> LOGGER.error("Not able to harvest from ${source.url}, no accept header supplied")
                jenaWriterType == Lang.RDFNULL -> LOGGER.error("Not able to harvest from ${source.url}, header ${source.acceptHeaderValue} is not acceptable ")
                harvested == null -> LOGGER.info("Not able to harvest ${source.url}")
                else -> checkHarvestedContainsChanges(harvested, source.url, harvestDate)
            }
        } else LOGGER.error("Harvest source is not defined")

    private fun checkHarvestedContainsChanges(harvested: Model, sourceURL: String, harvestDate: Calendar) {
        val dbId = createIdFromUri(sourceURL)
        val dbData = miscRepository
            .findByIdOrNull(sourceURL)
            ?.let { parseRDFResponse(ungzip(it.turtle), Lang.TURTLE, null) }

        if (dbData != null && harvested.isIsomorphicWith(dbData)) {
            LOGGER.info("No changes from last harvest of $sourceURL")
        } else {
            LOGGER.info("Changes detected, saving data from $sourceURL on graph $dbId, and updating FDK meta data")
            miscRepository.save(
                MiscellaneousTurtle(
                    id = sourceURL,
                    isHarvestedSource = true,
                    turtle = gzip(harvested.createRDFResponse(Lang.TURTLE))
                )
            )

            updateDB(harvested, harvestDate)
        }
    }

    private fun updateDB(harvested: Model, harvestDate: Calendar) {
        val catalogsToSave = mutableListOf<CatalogDBO>()
        val servicesToSave = mutableListOf<DataServiceDBO>()

        splitCatalogsFromModel(harvested)
            .map { Pair(it, catalogRepository.findByIdOrNull(it.resource.uri)) }
            .filter { it.first.catalogDiffersFromDB(it.second) }
            .forEach {
                val catalogURI = it.first.resource.uri

                val fdkId = it.second?.fdkId ?: createIdFromUri(catalogURI)
                val fdkUri = "${applicationProperties.catalogUri}/$fdkId"

                val issued = it.second?.issued
                    ?.let { timestamp -> calendarFromTimestamp(timestamp) }
                    ?: harvestDate

                var catalogModel = it.first.harvestedCatalogWithoutDatasets

                catalogModel.createResource(fdkUri)
                    .addProperty(RDF.type, DCAT.CatalogRecord)
                    .addProperty(DCTerms.identifier, fdkId)
                    .addProperty(FOAF.primaryTopic, catalogModel.createResource(catalogURI))
                    .addProperty(DCTerms.issued, catalogModel.createTypedLiteral(issued))
                    .addProperty(DCTerms.modified, catalogModel.createTypedLiteral(harvestDate))

                val servicesWithChanges = it.first.services
                    .map { dataService ->
                        val dbDataset = dataServiceRepository.findByIdOrNull(dataService.resource.uri)
                        if (dbDataset == null || dataService.differsFromDB(dbDataset)) {
                            Pair(dataService.mapToUpdatedDBO(harvestDate, fdkUri, dbDataset), true)
                        } else {
                            Pair(dbDataset, false)
                        }
                    }

                servicesWithChanges
                    .map { pair -> pair.first }
                    .map { dataset -> parseRDFResponse(ungzip(dataset.turtleDataService), Lang.TURTLE, null) }
                    .forEach { model -> catalogModel = catalogModel.union(model) }

                servicesWithChanges
                    .filter { dsWithChanged -> dsWithChanged.second }
                    .forEach { dsPair -> servicesToSave.add(dsPair.first) }

                catalogsToSave.add(
                    CatalogDBO(
                        uri = catalogURI,
                        fdkId = fdkId,
                        issued = issued.timeInMillis,
                        modified = harvestDate.timeInMillis,
                        turtleHarvested = gzip(it.first.harvestedCatalog.createRDFResponse(Lang.TURTLE)),
                        turtleCatalog = gzip(catalogModel.createRDFResponse(Lang.TURTLE))
                    )
                )
            }

        catalogRepository.saveAll(catalogsToSave)
        dataServiceRepository.saveAll(servicesToSave)
    }

    private fun DataServiceModel.mapToUpdatedDBO(
        harvestDate: Calendar,
        catalogURI: String,
        dbService: DataServiceDBO?
    ): DataServiceDBO {
        val fdkId = dbService?.fdkId ?: createIdFromUri(resource.uri)
        val fdkUri = "${applicationProperties.dataserviceUri}/$fdkId"

        val metaModel = ModelFactory.createDefaultModel()
        metaModel.addDefaultPrefixes()

        val issued: Calendar = dbService?.issued
            ?.let { timestamp -> calendarFromTimestamp(timestamp) }
            ?: harvestDate

        metaModel.createResource(fdkUri)
            .addProperty(RDF.type, DCAT.CatalogRecord)
            .addProperty(DCTerms.identifier, fdkId)
            .addProperty(FOAF.primaryTopic, metaModel.createResource(resource.uri))
            .addProperty(DCTerms.isPartOf, metaModel.createResource(catalogURI))
            .addProperty(DCTerms.issued, metaModel.createTypedLiteral(issued))
            .addProperty(DCTerms.modified, metaModel.createTypedLiteral(harvestDate))

        return DataServiceDBO(
            uri = resource.uri,
            fdkId = fdkId,
            isPartOf = catalogURI,
            issued = issued.timeInMillis,
            modified = harvestDate.timeInMillis,
            turtleHarvested = gzip(harvestedService.createRDFResponse(Lang.TURTLE)),
            turtleDataService = gzip(metaModel.union(harvestedService).createRDFResponse(Lang.TURTLE))
        )
    }
}