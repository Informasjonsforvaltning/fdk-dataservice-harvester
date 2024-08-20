package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.contract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.DuplicateIRI
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.ApiTestContext
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATASERVICE_ID_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATA_SERVICE_DBO_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATA_SERVICE_DBO_1
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
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    properties = ["spring.profiles.active=contract-test"],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("contract")
class DataServicesContract : ApiTestContext() {
    private val responseReader = TestResponseReader()
    private val mapper = jacksonObjectMapper()

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
            val response = authorizedRequest(
                port,
                "/dataservices/$DATASERVICE_ID_0/remove",
                null,
                HttpMethod.POST
            )
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

        @Test
        fun forbiddenWithNonSysAdminRole() {
            val response = authorizedRequest(
                port,
                "/dataservices/$DATASERVICE_ID_0/remove",
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.POST
            )
            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun notFoundWhenIdNotInDB() {
            val response = authorizedRequest(
                port,
                "/dataservices/123/remove",
                JwtToken(Access.ROOT).toString(),
                HttpMethod.POST
            )
            assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }

        @Test
        fun okWithSysAdminRole() {
            val response = authorizedRequest(
                port,
                "/dataservices/$DATASERVICE_ID_0/remove",
                JwtToken(Access.ROOT).toString(),
                HttpMethod.POST
            )
            assertEquals(HttpStatus.OK.value(), response["status"])
        }
    }

    @Nested
    internal inner class RemoveDuplicates {

        @Test
        fun unauthorizedForNoToken() {
            val body = listOf(DuplicateIRI(iriToRemove = DATA_SERVICE_DBO_0.uri, iriToRetain = DATA_SERVICE_DBO_1.uri))
            val response = authorizedRequest(
                port,
                "/dataservices/remove-duplicates",
                null,
                HttpMethod.POST,
                mapper.writeValueAsString(body)
            )
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

        @Test
        fun forbiddenWithNonSysAdminRole() {
            val body = listOf(DuplicateIRI(iriToRemove = DATA_SERVICE_DBO_0.uri, iriToRetain = DATA_SERVICE_DBO_1.uri))
            val response = authorizedRequest(
                port,
                "/dataservices/remove-duplicates",
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.POST,
                mapper.writeValueAsString(body)
            )
            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun badRequestWhenRemoveIRINotInDB() {
            val body = listOf(DuplicateIRI(iriToRemove = "https://123.no", iriToRetain = DATA_SERVICE_DBO_1.uri))
            val response =
                authorizedRequest(
                    port,
                    "/dataservices/remove-duplicates",
                    JwtToken(Access.ROOT).toString(),
                    HttpMethod.POST,
                    mapper.writeValueAsString(body)
                )
            assertEquals(HttpStatus.BAD_REQUEST.value(), response["status"])
        }

        @Test
        fun okWithSysAdminRole() {
            val body = listOf(DuplicateIRI(iriToRemove = DATA_SERVICE_DBO_0.uri, iriToRetain = DATA_SERVICE_DBO_1.uri))
            val response = authorizedRequest(
                port,
                "/dataservices/remove-duplicates",
                JwtToken(Access.ROOT).toString(),
                HttpMethod.POST,
                mapper.writeValueAsString(body)
            )
            assertEquals(HttpStatus.OK.value(), response["status"])
        }
    }

}
