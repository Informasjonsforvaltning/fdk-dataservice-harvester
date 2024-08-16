package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.ApiTestContext.Companion.mongoContainer
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import java.io.BufferedReader
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.net.HttpURLConnection
import java.net.URI

fun apiGet(port: Int, endpoint: String, acceptHeader: String?): Map<String, Any> {

    return try {
        val connection = URI("http://localhost:$port$endpoint").toURL().openConnection() as HttpURLConnection
        if (acceptHeader != null) connection.setRequestProperty("Accept", acceptHeader)
        connection.connect()

        if (isOK(connection.responseCode)) {
            val responseBody = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            mapOf(
                "body" to responseBody,
                "header" to connection.headerFields.toString(),
                "status" to connection.responseCode
            )
        } else {
            mapOf(
                "status" to connection.responseCode,
                "header" to " ",
                "body" to " "
            )
        }
    } catch (e: Exception) {
        mapOf(
            "status" to e.toString(),
            "header" to " ",
            "body" to " "
        )
    }
}

fun authorizedRequest(
    port: Int,
    endpoint: String,
    token: String?,
    method: HttpMethod = HttpMethod.POST,
    body: String? = null,
    headers: Map<String, String> = emptyMap()
): Map<String, Any> {
    val request = RestTemplate()
    request.requestFactory = HttpComponentsClientHttpRequestFactory()
    val url = "http://localhost:$port$endpoint"
    val httpHeaders = HttpHeaders()
    token?.let { httpHeaders.setBearerAuth(it) }
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    headers.forEach { httpHeaders.set(it.key, it.value) }
    val entity: HttpEntity<String> = HttpEntity(body, httpHeaders)

    return try {
        val response = request.exchange(url, method, entity, String::class.java)
        mapOf(
            "body" to response.body,
            "header" to response.headers.toString(),
            "status" to response.statusCode.value()
        )
    } catch (e: HttpClientErrorException) {
        mapOf(
            "status" to e.statusCode.value(),
            "header" to " ",
            "body" to e.toString()
        )
    } catch (e: Exception) {
        mapOf(
            "status" to e.toString(),
            "header" to " ",
            "body" to " "
        )
    }
}

private fun isOK(response: Int?): Boolean =
    if (response == null) false
    else HttpStatus.resolve(response)?.is2xxSuccessful == true

fun populateDB() {
    val connectionString =
        ConnectionString("mongodb://${MONGO_USER}:${MONGO_PASSWORD}@localhost:${mongoContainer.getMappedPort(MONGO_PORT)}/dataServiceHarvester?authSource=admin&authMechanism=SCRAM-SHA-1")
    val pojoCodecRegistry = CodecRegistries.fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
    )

    val client: MongoClient = MongoClients.create(connectionString)
    val mongoDatabase = client.getDatabase("dataServiceHarvester").withCodecRegistry(pojoCodecRegistry)

    val miscCollection = mongoDatabase.getCollection("turtle")
    miscCollection.insertMany(turtleDBPopulation())

    val catalogCollection = mongoDatabase.getCollection("catalogMeta")
    catalogCollection.insertMany(catalogDBPopulation())

    val serviceCollection = mongoDatabase.getCollection("dataserviceMeta")
    serviceCollection.insertMany(serviceDBPopulation())

    client.close()
}
