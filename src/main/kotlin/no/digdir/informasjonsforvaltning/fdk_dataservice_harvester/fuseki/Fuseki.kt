package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki

import org.apache.jena.query.DatasetAccessorFactory
import org.apache.jena.query.ParameterizedSparqlString
import org.apache.jena.query.Query
import org.apache.jena.query.QueryExecution
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.QueryFactory
import org.apache.jena.query.QueryParseException
import org.apache.jena.query.ResultSet
import org.apache.jena.rdf.model.Model
import org.apache.jena.update.UpdateExecutionFactory
import org.apache.jena.update.UpdateFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

private val logger = LoggerFactory.getLogger(Fuseki::class.java)

@Service
class Fuseki (private val fusekiProperties: FusekiProperties) {
    private val serviceUri = "${this.fusekiProperties.fusekiUri}${this.fusekiProperties.dataserviceEndpoint}"
    private val updateServiceUri = "${this.fusekiProperties.fusekiUri}${this.fusekiProperties.dataserviceEndpoint}/update"
    private val timeout: Long = 20L

    var prefixes = java.lang.String.join("\n",
        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>",
        "PREFIX difiMeta: <http://dcat.difi.no/metadata/>",
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
        "PREFIX dct: <http://purl.org/dc/terms/>"
    )

    fun sparqlUpdate(sparql: String, map: Map<String, String?>) {
        val parameterizedSparql = ParameterizedSparqlString()
            .apply { commandText = prefixes + sparql }

        map.keys
            .forEach { key: String -> parameterizedSparql.setLiteral(key, map[key]) }

        try {
            logger.debug("sparqlUpdate: $parameterizedSparql")
            UpdateExecutionFactory
                .createRemoteForm(UpdateFactory.create(parameterizedSparql.toString()), updateServiceUri)
                .execute()
        } catch (exception: QueryParseException) {
            logger.error("Error parsing query: p={}", parameterizedSparql.toString())
            throw exception
        }
    }

    fun isReachable(): Boolean {
        val httpUrlConnection = URL(serviceUri).openConnection() as HttpURLConnection
        httpUrlConnection.requestMethod = "HEAD"

        return try {
            httpUrlConnection.responseCode == HttpURLConnection.HTTP_OK
        } catch (noInternetConnection: UnknownHostException) {
            false
        }
    }

    fun select(sparql: String, map: Map<String, String>): ResultSet {
        val parameterizedSparql = ParameterizedSparqlString()
            .apply { commandText = prefixes + sparql }

        map.keys
            .forEach { key: String -> parameterizedSparql.setLiteral(key, map[key]) }

        return QueryFactory
            .create(parameterizedSparql.toString())
            .let { query ->
                logger.debug("Fuseki:select: {}", query.toString())
                getQueryExecution(query).execSelect()
            }
    }

    fun ask(query: String): Boolean {
        logger.trace(query)
        val queryExecution = QueryExecutionFactory
            .sparqlService(serviceUri,prefixes + query)
        queryExecution.setTimeout(timeout, TimeUnit.SECONDS)
        return queryExecution.execAsk()
    }

    fun ask(sparql: String, map: Map<String, String>): Boolean {
        val parameterizedSparql = ParameterizedSparqlString()
            .apply { commandText = prefixes + sparql }

        map.keys
            .forEach { key: String -> parameterizedSparql.setLiteral(key, map[key]) }

        return QueryFactory
            .create(parameterizedSparql.toString())
            .let { query ->
                logger.trace(query.toString())
                getQueryExecution(query).execAsk()
            }
    }

    private fun getQueryExecution(query: Query): QueryExecution {
        val queryExecution = QueryExecutionFactory.sparqlService(serviceUri, query)
        queryExecution.setTimeout(timeout, TimeUnit.SECONDS)
        return queryExecution
    }

    fun update(name: String, model: Model) {
        logger.info("Updating graph {} with data", name)
        val accessor = DatasetAccessorFactory.createHTTP(serviceUri)
        accessor.putModel(name, model)
    }


    fun drop(name: String) {
        logger.info("Dropping graph <{}> from $updateServiceUri", name)
        val request = UpdateFactory.create()
        request.add("DROP GRAPH <$name>")
        UpdateExecutionFactory
            .createRemote(request, updateServiceUri)
            .execute()
    }

    fun construct(query: String): Model? {
        logger.trace(query)
        val queryExecution = QueryExecutionFactory.sparqlService(serviceUri,prefixes + query)
        queryExecution.setTimeout(timeout, TimeUnit.SECONDS)
        return queryExecution.execConstruct()
    }

    fun select(query: String): ResultSet? {
        logger.trace(query)
        val queryExecution = QueryExecutionFactory.sparqlService(serviceUri,prefixes + query)
        queryExecution.setTimeout(timeout, TimeUnit.SECONDS)
        return queryExecution.execSelect()
    }


    fun describe(sparql: String, map: Map<String, String>): Model {
        val parameterizedSparql = ParameterizedSparqlString()
            .apply { commandText = prefixes + sparql }

        map.keys
            .forEach { key: String -> parameterizedSparql.setLiteral(key, map[key]) }

        return QueryFactory
            .create(parameterizedSparql.toString())
            .let { query -> getQueryExecution(query).execDescribe() }
    }

    fun graph(graphName: String): Model =
        DatasetAccessorFactory.createHTTP(serviceUri).getModel(graphName)

    fun graph(): Model =
        graph("urn:x-arq:UnionGraph")
}