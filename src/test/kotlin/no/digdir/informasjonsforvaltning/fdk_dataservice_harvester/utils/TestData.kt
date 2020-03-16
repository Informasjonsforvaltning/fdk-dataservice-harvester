package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.dto.HarvestDataSource
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.ApiTestContainer.Companion.TEST_API
import java.time.ZoneId
import java.util.*

const val API_PORT = 8080
const val LOCAL_SERVER_PORT = 5000

const val FUSEKI_NETWORK_NAME = "fdk-fuseki-service"
const val FUSEKI_TEST_URI = "http://$FUSEKI_NETWORK_NAME:$API_PORT"

const val WIREMOCK_TEST_HOST = "host.testcontainers.internal"
const val WIREMOCK_TEST_URI = "http://$WIREMOCK_TEST_HOST:$LOCAL_SERVER_PORT"

val API_ENV_VALUES: Map<String, String> = mapOf(
    "SPRING_PROFILES_ACTIVE" to "contract-test",
    "FUSEKI_TEST_URI" to FUSEKI_TEST_URI,
    "WIREMOCK_TEST_HOST" to WIREMOCK_TEST_HOST,
    "WIREMOCK_TEST_URI" to WIREMOCK_TEST_URI,
    "WIREMOCK_TEST_PORT" to LOCAL_SERVER_PORT.toString()
)

const val DATASERVICE_ID_0 = "ea51178e-f843-3025-98c5-7d02ce887f90"
const val DATASERVICE_ID_1 = "4d69ecde-f1e8-3f28-8565-360746e8b5ef"
const val CATALOG_ID_0 = "e422e2a7-287f-349f-876a-dc3541676f21"
const val CATALOG_ID_1 = "65555cdb-6809-3cc4-bff1-aaa6d9426311"

fun getApiAddress(endpoint: String): String {
    return "http://${TEST_API.containerIpAddress}:${TEST_API.getMappedPort(API_PORT)}$endpoint"
}

val TEST_HARVEST_DATE: Calendar = Calendar.Builder().setTimeZone(TimeZone.getTimeZone("UTC")).setDate(2020, 2, 12).setTimeOfDay(11, 52, 16, 122).build()