package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.ApplicationProperties
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.CatalogMeta
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.DataServiceMeta
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.addMetaPrefixes
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.calendarFromTimestamp
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.containsTriple
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.parseRDFResponse
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.CatalogRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.DataServiceRepository
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Service

@Service
class UpdateService(
    private val applicationProperties: ApplicationProperties,
    private val catalogRepository: CatalogRepository,
    private val dataServiceRepository: DataServiceRepository,
    private val turtleService: TurtleService
) {

    fun updateUnionModels() {
        var catalogUnion = ModelFactory.createDefaultModel()

        catalogRepository.findAll()
            .forEach {
                turtleService.getCatalog(it.fdkId, withRecords = true)
                    ?.let { turtle -> parseRDFResponse(turtle, Lang.TURTLE, null) }
                    ?.run { catalogUnion = catalogUnion.union(this) }
            }

        turtleService.saveAsCatalogUnion(catalogUnion)
    }

    fun updateMetaData() {
        dataServiceRepository.findAll()
            .forEach { dataService ->
                val dataServiceMeta = dataService.createMetaModel()

                turtleService.getDataService(dataService.fdkId, withRecords = false)
                    ?.let { conceptNoRecords -> parseRDFResponse(conceptNoRecords, Lang.TURTLE, null) }
                    ?.let { conceptModelNoRecords -> dataServiceMeta.union(conceptModelNoRecords) }
                    ?.run { turtleService.saveAsDataService(this, fdkId = dataService.fdkId, withRecords = true) }
            }

        catalogRepository.findAll()
            .forEach { catalog ->
                val catalogNoRecords = turtleService.getCatalog(catalog.fdkId, withRecords = false)
                    ?.let { parseRDFResponse(it, Lang.TURTLE, null) }

                if (catalogNoRecords != null) {
                    val catalogURI = "${applicationProperties.catalogUri}/${catalog.fdkId}"
                    var catalogMeta = catalog.createMetaModel()

                    dataServiceRepository.findAllByIsPartOf(catalogURI)
                        .filter { it.modelContainsDataService(catalogNoRecords) }
                        .forEach { dataService ->
                            val serviceMetaModel = dataService.createMetaModel()
                            catalogMeta = catalogMeta.union(serviceMetaModel)
                        }

                    turtleService.saveAsCatalog(
                        catalogMeta.union(catalogNoRecords),
                        fdkId = catalog.fdkId,
                        withRecords = true
                    )
                }
            }

        updateUnionModels()
    }

    private fun CatalogMeta.createMetaModel(): Model {
        val fdkUri = "${applicationProperties.catalogUri}/$fdkId"

        val metaModel = ModelFactory.createDefaultModel()
        metaModel.addMetaPrefixes()

        metaModel.createResource(fdkUri)
            .addProperty(RDF.type, DCAT.CatalogRecord)
            .addProperty(DCTerms.identifier, fdkId)
            .addProperty(FOAF.primaryTopic, metaModel.createResource(uri))
            .addProperty(DCTerms.issued, metaModel.createTypedLiteral(calendarFromTimestamp(issued)))
            .addProperty(DCTerms.modified, metaModel.createTypedLiteral(calendarFromTimestamp(modified)))

        return metaModel
    }

    private fun DataServiceMeta.createMetaModel(): Model {
        val fdkUri = "${applicationProperties.dataserviceUri}/$fdkId"

        val metaModel = ModelFactory.createDefaultModel()
        metaModel.addMetaPrefixes()

        metaModel.createResource(fdkUri)
            .addProperty(RDF.type, DCAT.CatalogRecord)
            .addProperty(DCTerms.identifier, fdkId)
            .addProperty(FOAF.primaryTopic, metaModel.createResource(uri))
            .addProperty(DCTerms.isPartOf, metaModel.createResource(isPartOf))
            .addProperty(DCTerms.issued, metaModel.createTypedLiteral(calendarFromTimestamp(issued)))
            .addProperty(DCTerms.modified, metaModel.createTypedLiteral(calendarFromTimestamp(modified)))

        return metaModel
    }

    private fun DataServiceMeta.modelContainsDataService(model: Model): Boolean =
        model.containsTriple("<${uri}>", "a", "<${DCAT.DataService.uri}>")

}
