package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.FusekiProperties
import org.apache.jena.query.ReadWrite
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdfconnection.RDFConnection
import org.apache.jena.rdfconnection.RDFConnectionFuseki
import org.springframework.stereotype.Service

@Service
class MetaFuseki(private val fusekiProperties: FusekiProperties) {

    private fun metaConnection(): RDFConnection =
        RDFConnectionFuseki.create()
            .destination(this.fusekiProperties.metaUri)
            .queryEndpoint("${this.fusekiProperties.metaUri}/query")
            .updateEndpoint("${this.fusekiProperties.metaUri}/update")
            .build()

    fun fetchCompleteModel(): Model =
        metaConnection().use {
            it.begin(ReadWrite.READ)
            return it.fetchDataset().unionModel
        }

    fun fetchByGraphName(graphName: String): Model? =
        metaConnection().use {
            it.begin(ReadWrite.READ)
            return try {
                it.fetch(graphName)
            } catch (ex: Exception) {
                null
            }
        }

    fun queryDescribe(query: String): Model? =
        metaConnection().use {
            it.begin(ReadWrite.READ)
            return try {
                it.queryDescribe(query)
            } catch (ex: Exception) {
                null
            }
        }

    fun saveWithGraphName(graphName: String, model: Model) =
        metaConnection().use {
            it.begin(ReadWrite.WRITE)
            it.put(graphName, model)
        }

}