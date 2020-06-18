package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.ApplicationProperties
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.HarvestFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.MetaFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.MissingHarvestException
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.addDefaultPrefixes
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createRDFResponse
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.extractMetaDataTopic
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.queryToGetMetaDataByCatalogUri
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val LOGGER = LoggerFactory.getLogger(DataServiceService::class.java)

@Service
class DataServiceService(
    private val harvestFuseki: HarvestFuseki,
    private val metaFuseki: MetaFuseki,
    private val applicationProperties: ApplicationProperties
) {

    fun countMetaData(): Int =
        metaFuseki.fetchCompleteModel()
            .listResourcesWithProperty(RDF.type, DCAT.CatalogRecord)
            .toList()
            .size

    fun getAll(returnType: JenaType): String =
        harvestFuseki.fetchCompleteModel()
            .union(metaFuseki.fetchCompleteModel())
            .addDefaultPrefixes()
            .createRDFResponse(returnType)

    fun getDataserviceById(id: String, returnType: JenaType): String? {
        val query = "DESCRIBE <${applicationProperties.dataserviceUri}/$id>"
        LOGGER.info(query)
        return metaFuseki.queryDescribe(query)
            ?.let { metaData ->
                val topicURI = metaData.extractMetaDataTopic()
                if (topicURI != null) metaData.union(getByURI(topicURI))
                else null
            }
            ?.addDefaultPrefixes()
            ?.createRDFResponse(returnType)
    }

    fun getCatalogById(id: String, returnType: JenaType): String? {
        val query = "DESCRIBE <${applicationProperties.catalogUri}/$id>"
        LOGGER.info(query)
        return metaFuseki.queryDescribe(query)
            ?.let { metaData ->

                val metaDatasets: Model = metaFuseki
                    .queryDescribe(queryToGetMetaDataByCatalogUri("${applicationProperties.catalogUri}/$id"))
                    ?: ModelFactory.createDefaultModel()

                val topicURI = metaData.extractMetaDataTopic()
                if (topicURI != null) {
                    metaData.union(getByURI(topicURI))
                        .union(metaDatasets)
                }
                else null
            }
            ?.addDefaultPrefixes()
            ?.createRDFResponse(returnType)
    }

    private fun getByURI(uri: String): Model {
        val literalsQuery = "DESCRIBE <$uri>"
        val propertiesQuery = "DESCRIBE * WHERE { <$uri> ?p ?o }"

        val harvestedData = harvestFuseki.queryDescribe(literalsQuery)
            ?.union(harvestFuseki.queryDescribe(propertiesQuery) ?: ModelFactory.createDefaultModel())

        if (harvestedData == null) throw MissingHarvestException()
        else return harvestedData
    }

}
