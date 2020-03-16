package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.contract

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.ApiTestContainer
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.CATALOG_ID_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.TestResponseReader
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.apiGet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.http.HttpStatus
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("contract")
class CatalogsContract : ApiTestContainer() {
    private val responseReader = TestResponseReader()

    @Test
    fun findSpecific() {
        val response = apiGet("/catalogs/$CATALOG_ID_0", "application/rdf+xml")
        assumeTrue(HttpStatus.OK.value() == response["status"])

        val expected = responseReader.parseFile("catalog_0.ttl", "TURTLE")
        val responseModel = responseReader.parseResponse(response["body"] as String, "RDFXML")

        assertTrue(expected.isIsomorphicWith(responseModel))
    }

    @Test
    fun idDoesNotExist() {
        val response = apiGet("/catalogs/123", "text/turtle")
        assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
    }

    @Test
    fun findAll() {
        val response = apiGet("/catalogs", "text/turtle")
        assumeTrue(HttpStatus.OK.value() == response["status"])

        val expected = responseReader.parseFile("all_catalogs.ttl", "TURTLE")
        val responseModel = responseReader.parseResponse(response["body"] as String, "TURTLE")

        assertTrue(expected.isIsomorphicWith(responseModel))
    }

}