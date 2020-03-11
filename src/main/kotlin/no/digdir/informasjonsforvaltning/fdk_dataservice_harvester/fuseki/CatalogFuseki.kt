package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki

import org.apache.jena.query.ReadWrite
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdfconnection.RDFConnection
import org.apache.jena.rdfconnection.RDFConnectionFuseki
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


private val logger = LoggerFactory.getLogger(DataServiceFuseki::class.java)

@Service
open class CatalogFuseki(private val fusekiProperties: FusekiProperties) {

    private fun catalogConnection(): RDFConnection =
        RDFConnectionFuseki.create()
            .destination(this.fusekiProperties.catalogUri)
            .queryEndpoint("${this.fusekiProperties.catalogUri}/query")
            .updateEndpoint("${this.fusekiProperties.catalogUri}/update")
            .build()

    fun fetchCompleteModel(): Model =
        catalogConnection().use {
            it.begin(ReadWrite.READ)
            return it.fetchDataset().unionModel
        }

    fun fetchByGraphName(graphName: String): Model? =
        catalogConnection().use {
            it.begin(ReadWrite.READ)
            return try {
                it.fetch(graphName)
            } catch (ex: Exception) {
                null
            }
        }

    fun saveWithGraphName(graphName: String, model: Model) =
        catalogConnection().use {
            it.begin(ReadWrite.WRITE)
            it.put(graphName, model)
        }

}