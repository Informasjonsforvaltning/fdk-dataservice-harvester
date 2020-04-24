package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import java.io.File

private val mockserver = WireMockServer(LOCAL_SERVER_PORT)

fun startMockServer() {
    if(!mockserver.isRunning) {
        mockserver.stubFor(get(urlEqualTo("/ping"))
                .willReturn(aResponse()
                        .withStatus(200))
        )
        mockserver.stubFor(get(urlEqualTo("/api/datasources"))
            .willReturn(okJson(jacksonObjectMapper().writeValueAsString(listOf(TEST_HARVEST_SOURCE, ERROR_HARVEST_SOURCE))))
        )
        mockserver.stubFor(get(urlMatching("/harvest"))
            .willReturn(ok(File("src/test/resources/harvest_response.ttl").readText())))
        mockserver.stubFor(get(urlMatching("/error-harvest"))
            .willReturn(ok(File("src/test/resources/harvest_response_with_errors.ttl").readText())))

        mockserver.start()
    }
}

fun stopMockServer() {

    if (mockserver.isRunning) mockserver.stop()

}