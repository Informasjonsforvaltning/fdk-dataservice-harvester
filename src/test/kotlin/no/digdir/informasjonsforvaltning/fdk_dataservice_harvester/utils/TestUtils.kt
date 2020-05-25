package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createRDFResponse
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.ApiTestContainer.Companion.TEST_API
import org.apache.jena.rdf.model.Model
import java.io.BufferedReader
import java.net.URL
import org.springframework.http.HttpStatus
import java.io.File
import java.net.HttpURLConnection

fun apiGet(endpoint: String, acceptHeader: String?): Map<String,Any> {

    return try {
        val connection = URL(getApiAddress(endpoint)).openConnection() as HttpURLConnection
        if(acceptHeader != null) connection.setRequestProperty("Accept", acceptHeader)
        connection.connect()

        if(isOK(connection.responseCode)) {
            val responseBody = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            mapOf(
                "body"   to responseBody,
                "header" to connection.headerFields.toString(),
                "status" to connection.responseCode)
        } else {
            mapOf(
                "status" to connection.responseCode,
                "header" to " ",
                "body"   to " "
            )
        }
    } catch (e: Exception) {
        mapOf(
            "status" to e.toString(),
            "header" to " ",
            "body"   to " "
        )
    }
}

fun addTestDataToFuseki(turtleBody: String, endpoint: String) {
    val rdfReader = TestResponseReader()
    val body = rdfReader.parseResponse(turtleBody, "TURTLE").createRDFResponse(JenaType.JSON_LD)
    val header = "Content-Type:application/ld+json"
    val url = "http://fdk-fuseki-service:8080/fuseki/$endpoint"
    TEST_API.execInContainer("curl", "-i", "-H", header, "-X", "PUT", "--data", body, url)
}

private fun isOK(response: Int?): Boolean =
    if(response == null) false
    else HttpStatus.resolve(response)?.is2xxSuccessful == true
