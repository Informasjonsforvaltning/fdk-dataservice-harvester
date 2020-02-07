package no.fdk.imcat.contract

import no.fdk.imcat.utils.ApiTestContainer
import no.fdk.imcat.utils.apiGet
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.http.HttpStatus

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("contract")
class PingTest : ApiTestContainer() {

    @Test
    fun findAllHarvestedModels() {
        val response = apiGet("/ping", "application/json")
        assertEquals(HttpStatus.OK.value(), response["status"])
    }

}