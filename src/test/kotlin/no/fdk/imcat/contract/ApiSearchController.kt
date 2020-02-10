package no.fdk.imcat.contract

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize
import no.fdk.imcat.utils.ApiTestContainer
import no.fdk.imcat.utils.apiGet
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.http.HttpStatus
import org.junit.jupiter.api.Assertions.assertEquals
import com.jayway.jsonpath.matchers.JsonPathMatchers.*
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("contract")
class ApiSearchController : ApiTestContainer() {

    @Test
    fun findAll() {
        val response = apiGet("/apis", "application/json")
        assertEquals(response["status"], HttpStatus.OK.value())

        assertThat(
            response["body"],
            isJson(
                allOf(
                    withJsonPath("$.total", equalTo(8)),
                    withJsonPath("$.hits", hasSize(8)))))

    }

}