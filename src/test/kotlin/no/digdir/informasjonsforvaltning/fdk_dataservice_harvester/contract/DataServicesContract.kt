package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.contract

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.ApiTestContainer
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATASERVICE_ID_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.TestResponseReader
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.apiGet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.http.HttpStatus
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("contract")
class DataServicesContract : ApiTestContainer() {
    val responseReader = TestResponseReader()

    @Test
    fun findSpecific() {
        val response = apiGet("/dataservices/$DATASERVICE_ID_0", "application/rdf+json")
        assertEquals(HttpStatus.OK.value(), response["status"])

        val expected = responseReader.getExpectedResponse("contract_dataservice_0.ttl", "TURTLE")
        val responseModel = responseReader.parseResponse(response["body"] as String, "RDF/JSON")

        assertTrue(expected.isIsomorphicWith(responseModel))
    }

    @Test
    fun idDoesNotExist() {
        val response = apiGet("/dataservices/123", "text/turtle")
        assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
    }

    @Test
    fun findAll() {
        val response = apiGet("/dataservices", "application/ld+json")
        assertEquals(HttpStatus.OK.value(), response["status"])

        val expected = responseReader.getExpectedResponse("contract_all_dataservices.ttl", "TURTLE")
        val responseModel = responseReader.parseResponse(response["body"] as String, "JSONLD")

        assertTrue(expected.isIsomorphicWith(responseModel))
    }
}