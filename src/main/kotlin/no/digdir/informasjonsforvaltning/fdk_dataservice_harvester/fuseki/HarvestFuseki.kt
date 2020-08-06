package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.FusekiProperties
import org.apache.jena.query.ReadWrite
import org.apache.jena.query.ResultSet
import org.apache.jena.query.ResultSetFormatter
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdfconnection.RDFConnection
import org.apache.jena.rdfconnection.RDFConnectionFuseki
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

private val LOGGER = LoggerFactory.getLogger(HarvestFuseki::class.java)

@Service
class HarvestFuseki(private val fusekiProperties: FusekiProperties) {

    private fun dataServiceConnection(): RDFConnection =
        RDFConnectionFuseki.create()
            .destination(this.fusekiProperties.dataserviceUri)
            .queryEndpoint("${this.fusekiProperties.dataserviceUri}/query")
            .updateEndpoint("${this.fusekiProperties.dataserviceUri}/update")
            .build()

    fun fetchCompleteModel(): Model =
        dataServiceConnection().use {
            it.begin(ReadWrite.READ)
            return it.fetchDataset().unionModel
        }

    fun fetchByGraphName(graphName: String): Model? =
        dataServiceConnection().use {
            it.begin(ReadWrite.READ)
            return try {
                it.fetch(graphName)
            } catch (ex: Exception) {
                null
            }
        }

    fun queryAsk(query: String): Boolean =
        dataServiceConnection().use {
            it.begin(ReadWrite.READ)
            try {
                it.queryAsk(query)
            } catch (ex: Exception) {
                LOGGER.error("sparql-ask exception: $ex")
                false
            }
        }

    fun queryConstruct(query: String): Model? =
        dataServiceConnection().use {
            it.begin(ReadWrite.READ)
            try {
                it.queryConstruct(query)
            } catch (ex: Exception) {
                LOGGER.error("sparql-construct exception: $ex")
                null
            }
        }

    fun queryDescribe(query: String): Model? =
        dataServiceConnection().use {
            it.begin(ReadWrite.READ)
            return try {
                it.queryDescribe(query)
            } catch (ex: Exception) {
                LOGGER.error("sparql exception: $ex")
                null
            }
        }

    fun querySelect(query: String, format: QueryResponseFormat): String? =
        dataServiceConnection().use {
            it.begin(ReadWrite.READ)
            var result: String? = null
            try {
                it.queryResultSet(query) { qs ->
                    result = when (format) {
                        QueryResponseFormat.TEXT -> ResultSetFormatter.asText(qs).valuesOrNull(format)
                        QueryResponseFormat.JSON -> querySelectWithJsonResponse(qs)?.valuesOrNull(format)
                        QueryResponseFormat.XML -> ResultSetFormatter.asXMLString(qs).valuesOrNull(format)
                    }
                }
            } catch (ex: Exception) {
                LOGGER.error("sparql-select exception: $ex")
                throw ex
            }
            return result
        }

    private fun querySelectWithJsonResponse(qs: ResultSet): String? {
        val outputStream = ByteArrayOutputStream()
        ResultSetFormatter.outputAsJSON(outputStream, qs)
        return String(outputStream.toByteArray())
    }

    fun saveWithGraphName(graphName: String, model: Model) =
        dataServiceConnection().use {
            it.begin(ReadWrite.WRITE)
            it.put(graphName, model)
        }

}

private fun String.valuesOrNull(format: QueryResponseFormat): String? =
    when (format) {
        QueryResponseFormat.TEXT -> if (this.split('\n').size > 5) this else null
        QueryResponseFormat.JSON -> if (this.split("\"bindings\": [")[1]
                .replace(Regex("[^A-Za-z0-9]"), "")
                .isNotEmpty()) this else null
        QueryResponseFormat.XML -> if (this.split("<results>")[1]
                .split("</results>")[0]
                .replace(Regex("[^A-Za-z0-9]"), "")
                .isNotEmpty()) this else null
    }

enum class QueryResponseFormat {
    TEXT, JSON, XML
}
