package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki

import org.apache.jena.query.ReadWrite
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdfconnection.RDFConnection
import org.apache.jena.rdfconnection.RDFConnectionRemote
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


private val logger = LoggerFactory.getLogger(FusekiConnection::class.java)

@Service
class FusekiConnection(private val fusekiProperties: FusekiProperties) {

    private fun dataserviceConnection(): RDFConnection =
        RDFConnectionRemote.create()
            .destination(this.fusekiProperties.fusekiUri)
            .queryEndpoint("${this.fusekiProperties.fusekiUri}/query")
            .updateEndpoint("${this.fusekiProperties.fusekiUri}/update")
            .build()

    fun fetchCompleteModel(): Model {
        dataserviceConnection().use {
            it.begin(ReadWrite.READ)
            return it.fetch()
        }
    }

    fun fetchByGraphName(graphName: String): Model {
        dataserviceConnection().use {
            it.begin(ReadWrite.READ)
            return it.fetch(graphName)
        }
    }

    fun updateModel(model: Model) {
        dataserviceConnection().use {
            it.begin(ReadWrite.WRITE)
            it.load(model)
            it.commit()
        }
    }

    fun saveWithGraphName(graphName: String, model: Model) {
        dataserviceConnection().use {
            it.put(graphName, model)
        }
    }
}