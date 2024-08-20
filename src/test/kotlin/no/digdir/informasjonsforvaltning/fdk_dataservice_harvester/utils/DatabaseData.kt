package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.*
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.UNION_ID
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.catalogTurtleID
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.dataServiceTurtleID
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.gzip
import org.bson.Document

private val responseReader = TestResponseReader()


val CATALOG_DBO_0 = CatalogMeta(
    uri = "https://testdirektoratet.no/model/dataservice-catalogs/0",
    fdkId = CATALOG_ID_0,
    issued = TEST_HARVEST_DATE.timeInMillis,
    modified = TEST_HARVEST_DATE.timeInMillis
)
val DATA_SERVICE_DBO_0 = DataServiceMeta(
    uri = "https://testdirektoratet.no/model/dataservice/0",
    fdkId = DATASERVICE_ID_0,
    isPartOf = "http://localhost:5050/catalogs/$CATALOG_ID_0",
    issued = TEST_HARVEST_DATE.timeInMillis,
    modified = TEST_HARVEST_DATE.timeInMillis
)

val CATALOG_DBO_1 = CatalogMeta(
    uri = "https://testdirektoratet.no/model/dataservice-catalogs/1",
    fdkId = CATALOG_ID_1,
    issued = TEST_HARVEST_DATE.timeInMillis,
    modified = TEST_HARVEST_DATE.timeInMillis
)
val DATA_SERVICE_DBO_1 = DataServiceMeta(
    uri = "https://testdirektoratet.no/model/dataservice/1",
    fdkId = DATASERVICE_ID_1,
    isPartOf = "http://localhost:5050/catalogs/$CATALOG_ID_1",
    issued = TEST_HARVEST_DATE.timeInMillis,
    modified = TEST_HARVEST_DATE.timeInMillis
)
val REMOVED_DATA_SERVICE_DBO = DataServiceMeta(
    uri = "https://testdirektoratet.no/model/dataservice/removed",
    fdkId = "removed",
    isPartOf = "http://localhost:5050/catalogs/$CATALOG_ID_1",
    removed = true,
    issued = TEST_HARVEST_DATE.timeInMillis,
    modified = TEST_HARVEST_DATE.timeInMillis
)

val UNION_DATA = TurtleDBO(
    id = catalogTurtleID(UNION_ID, true),
    turtle = gzip(responseReader.readFile("catalog_0.ttl"))
)

val HARVEST_DBO_0 = TurtleDBO(
    id = TEST_HARVEST_SOURCE.url!!,
    turtle = gzip(responseReader.readFile("harvest_response.ttl"))
)

val HARVEST_DBO_1 = TurtleDBO(
    id = "$WIREMOCK_TEST_URI/harvest1",
    turtle = gzip(responseReader.readFile("harvest_response_1.ttl"))
)

val CATALOG_TURTLE_0 = TurtleDBO(
    id = catalogTurtleID(CATALOG_ID_0, true),
    turtle = gzip(responseReader.readFile("catalog_0.ttl"))
)

val CATALOG_TURTLE_0_NO_RECORDS = TurtleDBO(
    id = catalogTurtleID(CATALOG_ID_0, false),
    turtle = gzip(responseReader.readFile("catalog_0_no_records.ttl"))
)

val DATA_SERVICE_TURTLE_0 = TurtleDBO(
    id = dataServiceTurtleID(DATASERVICE_ID_0, true),
    turtle = gzip(responseReader.readFile("dataservice_0.ttl"))
)

val DATA_SERVICE_TURTLE_0_NO_RECORDS = TurtleDBO(
    id = dataServiceTurtleID(DATASERVICE_ID_0, false),
    turtle = gzip(responseReader.readFile("parsed_dataservice_0.ttl"))
)

val CATALOG_TURTLE_1 = TurtleDBO(
    id = catalogTurtleID(CATALOG_ID_1, true),
    turtle = gzip(responseReader.readFile("catalog_1.ttl"))
)

val CATALOG_TURTLE_1_NO_RECORDS = TurtleDBO(
    id = catalogTurtleID(CATALOG_ID_1, false),
    turtle = gzip(responseReader.readFile("catalog_1_no_records.ttl"))
)

val DATA_SERVICE_TURTLE_1 = TurtleDBO(
    id = dataServiceTurtleID(DATASERVICE_ID_1, true),
    turtle = gzip(responseReader.readFile("dataservice_1.ttl"))
)

val DATA_SERVICE_TURTLE_1_NO_RECORDS = TurtleDBO(
    id = dataServiceTurtleID(DATASERVICE_ID_1, false),
    turtle = gzip(responseReader.readFile("parsed_dataservice_1.ttl"))
)

val REMOVED_DATA_SERVICE_TURTLE = TurtleDBO(
    id = dataServiceTurtleID("removed", true),
    turtle = gzip(responseReader.readFile("dataservice_1.ttl"))
)

val REMOVED_DATA_SERVICE_TURTLE_NO_RECORDS = TurtleDBO(
    id = dataServiceTurtleID("removed", false),
    turtle = gzip(responseReader.readFile("parsed_dataservice_1.ttl"))
)

fun turtleDBPopulation(): List<Document> =
    listOf(
        UNION_DATA, HARVEST_DBO_0, HARVEST_DBO_1, CATALOG_TURTLE_0, CATALOG_TURTLE_0_NO_RECORDS,
        CATALOG_TURTLE_1, CATALOG_TURTLE_1_NO_RECORDS, DATA_SERVICE_TURTLE_0, DATA_SERVICE_TURTLE_0_NO_RECORDS,
        DATA_SERVICE_TURTLE_1, DATA_SERVICE_TURTLE_1_NO_RECORDS, REMOVED_DATA_SERVICE_TURTLE, REMOVED_DATA_SERVICE_TURTLE_NO_RECORDS
    )
        .map { it.mapDBO() }

fun catalogDBPopulation(): List<Document> =
    listOf(CATALOG_DBO_0, CATALOG_DBO_1)
        .map { it.mapDBO() }

fun serviceDBPopulation(): List<Document> =
    listOf(DATA_SERVICE_DBO_0, DATA_SERVICE_DBO_1)
        .map { it.mapDBO() }

private fun CatalogMeta.mapDBO(): Document =
    Document()
        .append("_id", uri)
        .append("fdkId", fdkId)
        .append("issued", issued)
        .append("modified", modified)

private fun DataServiceMeta.mapDBO(): Document =
    Document()
        .append("_id", uri)
        .append("fdkId", fdkId)
        .append("isPartOf", isPartOf)
        .append("removed", removed)
        .append("issued", issued)
        .append("modified", modified)

private fun TurtleDBO.mapDBO(): Document =
    Document()
        .append("_id", id)
        .append("turtle", turtle)
