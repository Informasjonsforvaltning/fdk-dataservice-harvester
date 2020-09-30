package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.CATALOG_ID_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.CATALOG_ID_1
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATASERVICE_ID_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATASERVICE_ID_1
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.TestResponseReader
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("unit")
class RDFUtils {
    private val responseReader = TestResponseReader()

    @Test
    fun createId() {
        assertEquals(DATASERVICE_ID_0, createIdFromUri("https://testdirektoratet.no/model/dataservice/0"))
        assertEquals(DATASERVICE_ID_1, createIdFromUri("https://testdirektoratet.no/model/dataservice/1"))
        assertEquals(CATALOG_ID_0, createIdFromUri("https://testdirektoratet.no/model/dataservice-catalogs/0"))
        assertEquals(CATALOG_ID_1, createIdFromUri("https://testdirektoratet.no/model/dataservice-catalogs/1"))
    }

    @Test
    fun extractIdFromModel() {
        val catalogModel0 = responseReader.parseFile("no_prefix_catalog_meta_0.ttl", "TURTLE")
        val catalogModel1 = responseReader.parseFile("no_prefix_catalog_meta_1.ttl", "TURTLE")
        val dataserviceModel0 = responseReader.parseFile("no_prefix_dataservice_meta_0.ttl", "TURTLE")
        val dataserviceModel1 = responseReader.parseFile("no_prefix_dataservice_meta_1.ttl", "TURTLE")

        assertEquals(CATALOG_ID_0, catalogModel0.extractMetaDataIdentifier())
        assertEquals(CATALOG_ID_1, catalogModel1.extractMetaDataIdentifier())
        assertEquals(DATASERVICE_ID_0, dataserviceModel0.extractMetaDataIdentifier())
        assertEquals(DATASERVICE_ID_1, dataserviceModel1.extractMetaDataIdentifier())
    }

    @Test
    fun rdfModelParser() {
        val parsedRDFModel = parseRDFResponse(responseReader.readFile("harvest_response.ttl"), JenaType.TURTLE, "test")

        val expected = responseReader.parseFile("harvest_response.ttl", "TURTLE")

        Assertions.assertTrue(parsedRDFModel!!.isIsomorphicWith(expected))
    }
}