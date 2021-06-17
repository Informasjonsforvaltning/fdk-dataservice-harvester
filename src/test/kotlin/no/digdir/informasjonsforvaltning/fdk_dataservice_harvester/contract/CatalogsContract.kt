package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.contract

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.ApiTestContext
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.CATALOG_ID_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.TestResponseReader
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.apiGet
import org.apache.jena.riot.Lang
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    properties = ["spring.profiles.active=contract-test"],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("contract")
class CatalogsContract : ApiTestContext() {
    private val responseReader = TestResponseReader()

    @Test
    fun findSpecific() {
        val response = apiGet(port, "/catalogs/$CATALOG_ID_0", "application/rdf+xml")
        assumeTrue(HttpStatus.OK.value() == response["status"])

        val expected = responseReader.parseFile("catalog_0_no_records.ttl", "TURTLE")
        val responseModel = responseReader.parseResponse(response["body"] as String, "RDFXML")

        assertTrue(expected.isIsomorphicWith(responseModel))
    }

    @Test
    fun findSpecificWithREcords() {
        val response = apiGet(port, "/catalogs/$CATALOG_ID_0?catalogrecords=true", "application/trix")
        assumeTrue(HttpStatus.OK.value() == response["status"])

        val expected = responseReader.parseFile("catalog_0.ttl", "TURTLE")
        val responseModel = responseReader.parseResponse(response["body"] as String, Lang.TRIX.name)

        assertTrue(expected.isIsomorphicWith(responseModel))
    }

    @Test
    fun idDoesNotExist() {
        val response = apiGet(port, "/catalogs/123", "text/turtle")
        assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
    }

    @Test
    fun findAll() {
        val response = apiGet(port, "/catalogs", "application/trig")
        assumeTrue(HttpStatus.OK.value() == response["status"])

        val expected = responseReader.parseFile("all_catalogs_no_records.ttl", "TURTLE")
        val responseModel = responseReader.parseResponse(response["body"] as String, Lang.TRIG.name)

        assertTrue(expected.isIsomorphicWith(responseModel))
    }

    @Test
    fun findAllWithRecords() {
        val response = apiGet(port, "/catalogs?catalogrecords=true", "text/turtle")
        assumeTrue(HttpStatus.OK.value() == response["status"])

        val expected = responseReader.parseFile("all_catalogs.ttl", "TURTLE")
        val responseModel = responseReader.parseResponse(response["body"] as String, "TURTLE")

        assertTrue(expected.isIsomorphicWith(responseModel))
    }

}