package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.contract

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.*
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.jwk.Access
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.jwk.JwtToken
import org.junit.jupiter.api.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    properties = ["spring.profiles.active=contract-test"],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("contract")
class UpdateTest: ApiTestContext() {

    private val responseReader = TestResponseReader()

    @Test
    fun unauthorizedForNoToken() {
        val response = authorizedPost(port, "/update/meta", null, emptyMap())

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
    }

    @Test
    fun forbiddenForNonSysAdminRole() {
        val response = authorizedPost(port, "/update/meta", JwtToken(Access.ORG_WRITE).toString(), mapOf(Pair("X-API-KEY", "wrong-api-key")))

        assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
    }

    @Test
    fun noChangesWhenRunOnCorrectMeta() {
        val all = apiGet(port, "/events", "text/turtle")
        val catalog = apiGet(port, "/events/$CATALOG_ID_0", "text/turtle")
        val dataservice = apiGet(port, "/events/$DATASERVICE_ID_0", "text/turtle")

        val response = authorizedPost(port, "/update/meta", JwtToken(Access.ROOT).toString(), mapOf(Pair("X-API-KEY", "fdk-api-key")))

        assertEquals(HttpStatus.OK.value(), response["status"])

        val expectedAll = responseReader.parseResponse(all["body"] as String, "TURTLE")
        val expectedCatalog = responseReader.parseResponse(catalog["body"] as String, "TURTLE")
        val expectedInfoModel = responseReader.parseResponse(dataservice["body"] as String, "TURTLE")

        val allAfterUpdate = apiGet(port, "/events", "text/turtle")
        val catalogAfterUpdate = apiGet(port, "/events/$CATALOG_ID_0", "text/turtle")
        val infoModelAfterUpdate = apiGet(port, "/events/$DATASERVICE_ID_0", "text/turtle")

        val actualAll = responseReader.parseResponse(allAfterUpdate["body"] as String, "TURTLE")
        val actualCatalog = responseReader.parseResponse(catalogAfterUpdate["body"] as String, "TURTLE")
        val actualInfoModel = responseReader.parseResponse(infoModelAfterUpdate["body"] as String, "TURTLE")

        assertTrue(expectedAll.isIsomorphicWith(actualAll))
        assertTrue(expectedCatalog.isIsomorphicWith(actualCatalog))
        assertTrue(expectedInfoModel.isIsomorphicWith(actualInfoModel))
    }

}