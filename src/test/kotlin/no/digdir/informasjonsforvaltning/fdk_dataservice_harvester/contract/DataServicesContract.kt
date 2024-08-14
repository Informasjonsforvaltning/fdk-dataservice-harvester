package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.contract

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.ApiTestContext
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATASERVICE_ID_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.TestResponseReader
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.apiGet
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.authorizedRequest
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.jwk.Access
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.jwk.JwtToken
import org.apache.jena.riot.Lang
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Nested
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
class DataServicesContract : ApiTestContext() {
    private val responseReader = TestResponseReader()

    @Test
    fun findSpecific() {
        val response = apiGet(port, "/dataservices/$DATASERVICE_ID_0", "application/rdf+json")
        assumeTrue(HttpStatus.OK.value() == response["status"])

        val expected = responseReader.parseFile("parsed_dataservice_0.ttl", "TURTLE")
        val responseModel = responseReader.parseResponse(response["body"] as String, "RDF/JSON")

        assertTrue(expected.isIsomorphicWith(responseModel))
    }

    @Test
    fun findSpecificWithRecords() {
        val response = apiGet(port, "/dataservices/$DATASERVICE_ID_0?catalogrecords=true", "application/n-quads")
        assumeTrue(HttpStatus.OK.value() == response["status"])

        val expected = responseReader.parseFile("dataservice_0.ttl", "TURTLE")
        val responseModel = responseReader.parseResponse(response["body"] as String, Lang.NQUADS.name)

        assertTrue(expected.isIsomorphicWith(responseModel))
    }

    @Test
    fun idDoesNotExist() {
        val response = apiGet(port, "/dataservices/123", "text/turtle")
        assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
    }

    @Nested
    internal inner class RemoveDataServiceById {

        @Test
        fun unauthorizedForNoToken() {
            val response = authorizedRequest(port, "/dataservices/$DATASERVICE_ID_0", null, "DELETE")
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

        @Test
        fun forbiddenWithNonSysAdminRole() {
            val response = authorizedRequest(
                port,
                "/dataservices/$DATASERVICE_ID_0",
                JwtToken(Access.ORG_WRITE).toString(),
                "DELETE"
            )
            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun notFoundWhenIdNotInDB() {
            val response =
                authorizedRequest(port, "/dataservices/123", JwtToken(Access.ROOT).toString(), "DELETE")
            assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }

        @Test
        fun okWithSysAdminRole() {
            val response = authorizedRequest(
                port,
                "/dataservices/$DATASERVICE_ID_0",
                JwtToken(Access.ROOT).toString(),
                "DELETE"
            )
            assertEquals(HttpStatus.NO_CONTENT.value(), response["status"])
        }
    }

}
