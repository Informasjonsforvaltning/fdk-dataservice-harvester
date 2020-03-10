package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf

import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

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

}