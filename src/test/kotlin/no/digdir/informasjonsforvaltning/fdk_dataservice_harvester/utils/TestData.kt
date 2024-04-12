package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.HarvestDataSource
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap
import java.util.*

const val LOCAL_SERVER_PORT = 5050
const val WIREMOCK_TEST_URI = "http://localhost:$LOCAL_SERVER_PORT"

const val MONGO_USER = "testuser"
const val MONGO_PASSWORD = "testpassword"
const val MONGO_PORT = 27017

val MONGO_ENV_VALUES: Map<String, String> = ImmutableMap.of(
    "MONGO_INITDB_ROOT_USERNAME", MONGO_USER,
    "MONGO_INITDB_ROOT_PASSWORD", MONGO_PASSWORD
)

const val DATASERVICE_ID_0 = "ea51178e-f843-3025-98c5-7d02ce887f90"
const val DATASERVICE_ID_1 = "4d69ecde-f1e8-3f28-8565-360746e8b5ef"
const val CATALOG_ID_0 = "e422e2a7-287f-349f-876a-dc3541676f21"
const val CATALOG_ID_1 = "65555cdb-6809-3cc4-bff1-aaa6d9426311"

val TEST_HARVEST_DATE: Calendar = Calendar.Builder().setTimeZone(TimeZone.getTimeZone("UTC")).setDate(2020, 2, 12).setTimeOfDay(11, 52, 16, 122).build()
val NEW_TEST_HARVEST_DATE: Calendar = Calendar.Builder().setTimeZone(TimeZone.getTimeZone("UTC")).setDate(2020, 6, 12).setTimeOfDay(11, 52, 16, 122).build()

val TEST_HARVEST_SOURCE = HarvestDataSource(
    id = "harvest",
    url = "$WIREMOCK_TEST_URI/harvest",
    acceptHeaderValue = "text/turtle",
    dataType = "dataservice",
    dataSourceType = "DCAT-AP-NO"
)

val ERROR_HARVEST_SOURCE = HarvestDataSource(
    id = "error-harvest",
    url = "$WIREMOCK_TEST_URI/error-harvest",
    acceptHeaderValue = "text/turtle",
    dataType = "dataservice",
    dataSourceType = "DCAT-AP-NO"
)