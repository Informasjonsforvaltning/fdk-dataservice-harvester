package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.generated.model.Catalog
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.generated.model.Contact
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.generated.model.Dataservice
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.catalog1
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

    @Test
    fun rdfParsedToCatalog() {
        val rdfBody: String = javaClass.classLoader.getResourceAsStream("catalog_1.ttl")!!.reader().readText()

        val parsedCatalog = parseRDFResponse(rdfBody, JenaType.TURTLE).parseCatalogs().first()

        Assertions.assertEquals(catalog1, parsedCatalog)
    }

    @Test
    fun parserHandlesNullValues() {
        val emptyBody: String = javaClass.classLoader.getResourceAsStream("empty.ttl")!!.reader().readText()
        val nullValues: String = javaClass.classLoader.getResourceAsStream("catalog_0.ttl")!!.reader().readText()

        Assertions.assertDoesNotThrow{ parseRDFResponse(emptyBody, JenaType.TURTLE).parseCatalogs() }

        val parsedNullCatalog = parseRDFResponse(nullValues, JenaType.TURTLE).parseCatalogs().first()

        val expected = Catalog().apply {
            id = "http://localhost:8080/catalogs/0"
            dataservices = listOf(Dataservice().apply {
                id = "http://localhost:8080/dataservices/0"
            })
        }

        Assertions.assertEquals(expected, parsedNullCatalog)
    }

}