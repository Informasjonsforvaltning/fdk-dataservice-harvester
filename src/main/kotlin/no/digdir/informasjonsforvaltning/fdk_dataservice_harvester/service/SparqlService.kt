package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.HarvestFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.QueryResponseFormat
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.addDefaultPrefixes
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createRDFResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val LOGGER = LoggerFactory.getLogger(SparqlService::class.java)

@Service
class SparqlService(private val harvestFuseki: HarvestFuseki) {

    fun sparqlDescribe(query: String): String? =
        when {
            query.toUpperCase().contains("DESCRIBE") -> {
                harvestFuseki.queryDescribe(query)
                    ?.addDefaultPrefixes()
                    ?.createRDFResponse(JenaType.TURTLE)
            }
            else -> {
                LOGGER.info("describe query does not contain describe, will not be executed: $query")
                throw IllegalArgumentException()
            }
        }


    fun sparqlSelect(query: String, format: String): String? =
        when {
            query.toUpperCase().contains("SELECT") ->
                harvestFuseki.querySelect(query, format.getQueryResponseFormat()
                )
            else -> {
                LOGGER.info("select query does not contain select, will not be executed: $query")
                throw IllegalArgumentException()
            }
        }

    fun sparqlConstruct(query: String): String? =
        when {
            query.toUpperCase().contains("CONSTRUCT") -> {
                harvestFuseki.queryConstruct(query)
                    ?.addDefaultPrefixes()
                    ?.createRDFResponse(JenaType.TURTLE)
            }
            else -> {
                LOGGER.info("construct query does not contain construct, will not be executed: $query")
                throw IllegalArgumentException()
            }
        }

    fun sparqlAsk(query: String): Boolean =
        when {
            query.toUpperCase().contains("ASK") -> harvestFuseki.queryAsk(query)
            else -> {
                LOGGER.info("ask query does not contain ask, will not be executed: $query")
                throw IllegalArgumentException()
            }
        }
}

private fun String.getQueryResponseFormat(): QueryResponseFormat = when (this) {
    "text/plain" -> QueryResponseFormat.TEXT
    "application/json" -> QueryResponseFormat.JSON
    "application/xml" -> QueryResponseFormat.XML
    else -> QueryResponseFormat.TEXT
}
