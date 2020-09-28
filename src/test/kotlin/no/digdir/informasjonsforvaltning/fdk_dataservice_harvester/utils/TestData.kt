package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.CatalogDBO
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.DataServiceDBO
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.HarvestDataSource
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.MiscellaneousTurtle
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createIdFromUri
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.gzip
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap
import java.util.*

private val responseReader = TestResponseReader()

const val API_PORT = 8080
const val API_TEST_PORT = 5555
const val LOCAL_SERVER_PORT = 5000

const val API_TEST_URI = "http://localhost:$API_TEST_PORT"
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

val CATALOG_DBO_0 = CatalogDBO(
    uri = "https://testdirektoratet.no/model/dataservice-catalogs/0",
    fdkId = CATALOG_ID_0,
    issued = TEST_HARVEST_DATE.timeInMillis,
    modified = TEST_HARVEST_DATE.timeInMillis,
    turtleHarvested = gzip(responseReader.readFile("harvest_response.ttl")),
    turtleCatalog = gzip(responseReader.readFile("catalog_0.ttl"))
)
val DATA_SERVICE_DBO_0 = DataServiceDBO(
    uri = "https://testdirektoratet.no/model/dataservice/0",
    fdkId = DATASERVICE_ID_0,
    isPartOf = "http://localhost:5000/catalogs/$CATALOG_ID_0",
    issued = TEST_HARVEST_DATE.timeInMillis,
    modified = TEST_HARVEST_DATE.timeInMillis,
    turtleHarvested = gzip(responseReader.readFile("parsed_dataservice_0.ttl")),
    turtleDataService = gzip(responseReader.readFile("dataservice_0.ttl"))
)

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

val HARVEST_DBO_0 = MiscellaneousTurtle(
    id = TEST_HARVEST_SOURCE.url!!,
    isHarvestedSource = true,
    turtle = gzip(responseReader.readFile("harvest_response.ttl"))
)

val TEST_HARVEST_SOURCE_ID = createIdFromUri("$WIREMOCK_TEST_URI/harvest")