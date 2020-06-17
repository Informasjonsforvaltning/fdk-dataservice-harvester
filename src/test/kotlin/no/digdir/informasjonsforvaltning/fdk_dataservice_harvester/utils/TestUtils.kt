package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createRDFResponse
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.net.URL
import org.springframework.http.HttpStatus
import java.net.HttpURLConnection

private val logger = LoggerFactory.getLogger(ApiTestContext::class.java)

fun apiGet(endpoint: String, acceptHeader: String?): Map<String,Any> {

    return try {
        val connection = URL("$API_TEST_URI$endpoint").openConnection() as HttpURLConnection
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

fun addTestDataToFuseki(turtleBody: String, endpoint: String, port: Int) {
    val rdfReader = TestResponseReader()
    val body = rdfReader.parseResponse(turtleBody, "TURTLE").createRDFResponse(JenaType.JSON_LD).toByteArray()
    with(URL("http://localhost:$port/fuseki/$endpoint").openConnection() as HttpURLConnection) {
        setRequestProperty("Content-Type", "application/ld+json")
        requestMethod = "PUT"
        doOutput = true
        val os = outputStream
        os.write(body)
        os.close()
        connect()
        if (!isOK(responseCode)) logger.error("fuseki add to $endpoint failed: $responseCode")
    }
}

private fun isOK(response: Int?): Boolean =
    if(response == null) false
    else HttpStatus.resolve(response)?.is2xxSuccessful == true
