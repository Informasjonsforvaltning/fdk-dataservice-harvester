package no.fdk.imcat.utils

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

        mockserver.stubFor(get(urlEqualTo("/api/registration/apis"))
                .willReturn(okJson(File("src/test/resources/test-apis.json").readText())))

        mockserver.stubFor(get(urlMatching("/api/publishers/([0-9]*)"))
            .willReturn(okJson(File("src/test/resources/org.json").readText())))

        mockserver.stubFor(get(urlMatching("/api/datasets/byuri(.*)"))
            .willReturn(okJson("{}")))

        mockserver.start()
    }
}

fun stopMockServer() {

    if (mockserver.isRunning) mockserver.stop()

}