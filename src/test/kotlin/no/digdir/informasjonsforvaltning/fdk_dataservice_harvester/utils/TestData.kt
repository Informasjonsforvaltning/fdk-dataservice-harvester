package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.dto.HarvestDataSource
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createIdFromUri
import java.util.*

const val API_PORT = 8080
const val API_TEST_PORT = 5555
const val LOCAL_SERVER_PORT = 5000

const val API_TEST_URI = "http://localhost:$API_TEST_PORT"
const val WIREMOCK_TEST_URI = "http://localhost:$LOCAL_SERVER_PORT"

const val DATASERVICE_ID_0 = "ea51178e-f843-3025-98c5-7d02ce887f90"
const val DATASERVICE_ID_1 = "4d69ecde-f1e8-3f28-8565-360746e8b5ef"
const val CATALOG_ID_0 = "e422e2a7-287f-349f-876a-dc3541676f21"
const val CATALOG_ID_1 = "65555cdb-6809-3cc4-bff1-aaa6d9426311"

val TEST_HARVEST_DATE: Calendar = Calendar.Builder().setTimeZone(TimeZone.getTimeZone("UTC")).setDate(2020, 2, 12).setTimeOfDay(11, 52, 16, 122).build()

val TEST_HARVEST_SOURCE = HarvestDataSource(
    url = "$WIREMOCK_TEST_URI/harvest",
    acceptHeaderValue = "text/turtle",
    dataType = "dataservice",
    dataSourceType = "DCAT-AP-NO"
)

val ERROR_HARVEST_SOURCE = HarvestDataSource(
    url = "$WIREMOCK_TEST_URI/error-harvest",
    acceptHeaderValue = "text/turtle",
    dataType = "dataservice",
    dataSourceType = "DCAT-AP-NO"
)

val TEST_HARVEST_SOURCE_ID = createIdFromUri("$WIREMOCK_TEST_URI/harvest")