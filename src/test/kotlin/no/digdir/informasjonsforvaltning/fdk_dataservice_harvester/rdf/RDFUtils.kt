package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.CATALOG_ID_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.CATALOG_ID_1
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATASERVICE_ID_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATASERVICE_ID_1
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals

@Tag("unit")
class RDFUtils {

    @Test
    fun extractDataServiceCatalogsFromModel() {
        val rdfBody: String = javaClass.classLoader.getResourceAsStream("catalogs.ttl")!!.reader().readText()

        val extracted = parseRDFResponse(rdfBody, JenaType.TURTLE)
            .listOfCatalogResources()
            .map { it.model }
            .forEach { println(it.createRDFResponse(JenaType.TURTLE)) }


    }

    @Test
    fun extractDataServicesFromModel() {
        val rdfBody: String = javaClass.classLoader.getResourceAsStream("catalog_2.ttl")!!.reader().readText()

        val parsedRDFModel = parseRDFResponse(rdfBody, JenaType.TURTLE)

        val expected = ModelFactory.createDefaultModel()
        expected.read(InputStreamReader(javaClass.classLoader.getResourceAsStream("catalog_2.ttl")!!, StandardCharsets.UTF_8), "", "TURTLE")

        Assertions.assertTrue(parsedRDFModel.isIsomorphicWith(expected))
    }

    @Test
    fun rdfModelParser() {
        val rdfBody: String = javaClass.classLoader.getResourceAsStream("catalog_2.ttl")!!.reader().readText()

        val parsedRDFModel = parseRDFResponse(rdfBody, JenaType.TURTLE)

        val expected = ModelFactory.createDefaultModel()
        expected.read(InputStreamReader(javaClass.classLoader.getResourceAsStream("catalog_2.ttl")!!, StandardCharsets.UTF_8), "", "TURTLE")

        Assertions.assertTrue(parsedRDFModel.isIsomorphicWith(expected))
    }

    @Test
    fun createId() {
        assertEquals(DATASERVICE_ID_0, createIdFromUri("https://testdirektoratet.no/model/dataservice/0"))
        assertEquals(DATASERVICE_ID_1, createIdFromUri("https://testdirektoratet.no/model/dataservice/1"))
        assertEquals(CATALOG_ID_0, createIdFromUri("https://testdirektoratet.no/model/dataservice-catalogs/0"))
        assertEquals(CATALOG_ID_1, createIdFromUri("https://testdirektoratet.no/model/dataservice-catalogs/1"))
    }
}