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
        val catalogModel0 = responseReader.parseFile("db_catalog_0.json", "JSONLD")
        val catalogModel1 = responseReader.parseFile("db_catalog_1.json", "JSONLD")
        val dataserviceModel0 = responseReader.parseFile("db_dataservice_0.json", "JSONLD")
        val dataserviceModel1 = responseReader.parseFile("db_dataservice_1.json", "JSONLD")

        assertEquals(CATALOG_ID_0, catalogModel0.extractMetaDataIdentifier())
        assertEquals(CATALOG_ID_1, catalogModel1.extractMetaDataIdentifier())
        assertEquals(DATASERVICE_ID_0, dataserviceModel0.extractMetaDataIdentifier())
        assertEquals(DATASERVICE_ID_1, dataserviceModel1.extractMetaDataIdentifier())
    }

    @Test
    fun rdfModelParser() {
        val rdfBody: String = javaClass.classLoader.getResourceAsStream("all_catalogs.ttl")!!.reader().readText()

        val parsedRDFModel = parseRDFResponse(rdfBody, JenaType.TURTLE)

        val expected = responseReader.parseFile("all_catalogs.ttl", "TURTLE")

        Assertions.assertTrue(parsedRDFModel.isIsomorphicWith(expected))
    }
}