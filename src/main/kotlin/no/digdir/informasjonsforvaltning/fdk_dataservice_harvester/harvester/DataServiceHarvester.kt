package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.DataServiceAdapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.ApplicationProperties
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.HarvestDataSource
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.MetaFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.HarvestFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createIdFromUri
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createModel
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.extractMetaDataIdentifier
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.jenaTypeFromAcceptHeader
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.listOfCatalogResources
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.listOfDataServiceResources
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.parseRDFResponse
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.queryToGetMetaDataByUri
import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

private val LOGGER = LoggerFactory.getLogger(DataServiceHarvester::class.java)

@Service
class DataServiceHarvester(
    private val adapter: DataServiceAdapter,
    private val harvestFuseki: HarvestFuseki,
    private val metaFuseki: MetaFuseki,
    private val applicationProperties: ApplicationProperties
) {

    fun harvestDataServiceCatalog(source: HarvestDataSource, harvestDate: Calendar) {
        if(source.url != null) {
            LOGGER.debug("Starting harvest of ${source.url}")
            val jenaWriterType = jenaTypeFromAcceptHeader(source.acceptHeaderValue)

            if (jenaWriterType == null || jenaWriterType == JenaType.NOT_JENA) {
                LOGGER.error("Not able to harvest from ${source.url}, header ${source.acceptHeaderValue} is not acceptable ")
            } else {
                val harvested = adapter.getDataServices(source)
                    ?.let { parseRDFResponse(it, jenaWriterType, source.url) }

                if (harvested == null) LOGGER.info("Not able to harvest ${source.url}")
                else {
                    val dbId = createIdFromUri(source.url)
                    val dbData = harvestFuseki.fetchByGraphName(dbId)

                    if (dbData != null && harvested.isIsomorphicWith(dbData)) LOGGER.info("No changes from last harvest of ${source.url}")
                    else {
                        LOGGER.info("Changes detected, saving data from ${source.url} on graph $dbId, and updating FDK meta data")
                        harvestFuseki.saveWithGraphName(dbId, harvested)

                        updateMetaData(harvested, dbData, harvestDate)
                    }
                }
            }
        }
    }

    private fun updateMetaData(harvested: Model, oldData: Model?, harvestDate: Calendar) {
        harvested.listOfCatalogResources()
            .forEach {
                val catalogModel = it.createModel()
                if(!catalogModel.isIsomorphicWithOldData(it.uri, oldData)) {
                    it.updateCatalogMetaData(harvestDate, catalogModel, oldData)
                }
            }
    }

    private fun Resource.updateCatalogMetaData(harvestDate: Calendar, catalogModel: Model, oldData: Model?) {
        val dbModel = metaFuseki.queryDescribe(queryToGetMetaDataByUri(uri))
        val dbId = dbModel?.extractMetaDataIdentifier() ?: createIdFromUri(uri)
        val resourceUri = "${applicationProperties.catalogUri}/$dbId"
        val dbMetaData = dbModel?.getResource(resourceUri)

        val metaModel = ModelFactory.createDefaultModel()

        metaModel.createResource(resourceUri)
            .addProperty(RDF.type, DCAT.CatalogRecord)
            .addProperty(DCTerms.identifier, dbId)
            .addProperty(FOAF.primaryTopic, metaModel.createResource(uri))
            .addProperty(DCTerms.issued, metaModel.issuedDate(dbMetaData, harvestDate))
            .addModified(dbMetaData, harvestDate)

        metaFuseki.saveWithGraphName(dbId, metaModel)

        catalogModel.listResourcesWithProperty(RDF.type, DCAT.DataService)
            .forEach {
                if(!it.isIsomorphicWithOldData(oldData)) {
                    it.updateDataServiceMetaData(harvestDate, resourceUri)
                }
            }
    }

    private fun Resource.updateDataServiceMetaData(harvestDate: Calendar, catalogURI: String) {
        val dbModel = metaFuseki.queryDescribe(queryToGetMetaDataByUri(uri))
        val dbId = dbModel?.extractMetaDataIdentifier() ?: createIdFromUri(uri)
        val resourceUri = "${applicationProperties.dataserviceUri}/$dbId"
        val dbMetaData = dbModel?.getResource(resourceUri)

        val metaModel = ModelFactory.createDefaultModel()

        metaModel.createResource(resourceUri)
            .addProperty(RDF.type, DCAT.CatalogRecord)
            .addProperty(DCTerms.identifier, dbId)
            .addProperty(FOAF.primaryTopic, metaModel.createResource(uri))
            .addProperty(DCTerms.isPartOf,  metaModel.createResource(catalogURI))
            .addProperty(DCTerms.issued,  metaModel.issuedDate(dbMetaData, harvestDate))
            .addModified(dbMetaData, harvestDate)

        metaFuseki.saveWithGraphName(dbId, metaModel)
    }

}

private fun Resource.isIsomorphicWithOldData(fullModelFromDB: Model?): Boolean =
    fullModelFromDB?.getResource(uri)?.let {
        createModel().isIsomorphicWith(it.createModel())
    } ?: false

private fun Model.isIsomorphicWithOldData(uri: String, fullModelFromDB: Model?): Boolean =
    fullModelFromDB?.getResource(uri)?.let {
        isIsomorphicWith(it.createModel())
    } ?: false

private fun Model.issuedDate(dbResource: Resource?, harvestDate: Calendar): Literal =
    dbResource?.listProperties(DCTerms.issued)
        ?.toList()
        ?.firstOrNull()
        ?.literal
        ?: createTypedLiteral(harvestDate)

private fun Resource.addModified(dbResource: Resource?, harvestDate: Calendar) {
    addProperty(DCTerms.modified, model.createTypedLiteral(harvestDate))

    dbResource?.listProperties(DCTerms.modified)
        ?.toList()
        ?.forEach { addProperty(DCTerms.modified, it.string) }
}
