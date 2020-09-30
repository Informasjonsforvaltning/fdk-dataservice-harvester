package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.CatalogDBO
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.DataServiceDBO
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.MiscellaneousTurtle
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.UNION_ID
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.gzip
import org.bson.Document

private val responseReader = TestResponseReader()


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

val CATALOG_DBO_1 = CatalogDBO(
    uri = "https://testdirektoratet.no/model/dataservice-catalogs/1",
    fdkId = CATALOG_ID_1,
    issued = TEST_HARVEST_DATE.timeInMillis,
    modified = TEST_HARVEST_DATE.timeInMillis,
    turtleHarvested = gzip(responseReader.readFile("harvest_response_1.ttl")),
    turtleCatalog = gzip(responseReader.readFile("catalog_1.ttl"))
)
val DATA_SERVICE_DBO_1 = DataServiceDBO(
    uri = "https://testdirektoratet.no/model/dataservice/1",
    fdkId = DATASERVICE_ID_1,
    isPartOf = "http://localhost:5000/catalogs/$CATALOG_ID_1",
    issued = TEST_HARVEST_DATE.timeInMillis,
    modified = TEST_HARVEST_DATE.timeInMillis,
    turtleHarvested = gzip(responseReader.readFile("parsed_dataservice_1.ttl")),
    turtleDataService = gzip(responseReader.readFile("dataservice_1.ttl"))
)

val UNION_DATA = MiscellaneousTurtle(
    id = UNION_ID,
    isHarvestedSource = false,
    turtle = gzip(responseReader.readFile("catalog_0.ttl"))
)

val HARVEST_DBO_0 = MiscellaneousTurtle(
    id = TEST_HARVEST_SOURCE.url!!,
    isHarvestedSource = true,
    turtle = gzip(responseReader.readFile("harvest_response.ttl"))
)

val HARVEST_DBO_1 = MiscellaneousTurtle(
    id = "$WIREMOCK_TEST_URI/harvest1",
    isHarvestedSource = true,
    turtle = gzip(responseReader.readFile("harvest_response_1.ttl"))
)

fun miscDBPopulation(): List<Document> =
    listOf(UNION_DATA, HARVEST_DBO_0, HARVEST_DBO_1)
        .map { it.mapDBO() }

fun catalogDBPopulation(): List<Document> =
    listOf(CATALOG_DBO_0, CATALOG_DBO_1)
        .map { it.mapDBO() }

fun serviceDBPopulation(): List<Document> =
    listOf(DATA_SERVICE_DBO_0, DATA_SERVICE_DBO_1)
        .map { it.mapDBO() }

private fun CatalogDBO.mapDBO(): Document =
    Document()
        .append("_id", uri)
        .append("fdkId", fdkId)
        .append("issued", issued)
        .append("modified", modified)
        .append("turtleHarvested", turtleHarvested)
        .append("turtleCatalog", turtleCatalog)

private fun DataServiceDBO.mapDBO(): Document =
    Document()
        .append("_id", uri)
        .append("fdkId", fdkId)
        .append("isPartOf", isPartOf)
        .append("issued", issued)
        .append("modified", modified)
        .append("turtleHarvested", turtleHarvested)
        .append("turtleDataService", turtleDataService)

private fun MiscellaneousTurtle.mapDBO(): Document =
    Document()
        .append("_id", id)
        .append("isHarvestedSource", isHarvestedSource)
        .append("turtle", turtle)
