package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import com.nhaarman.mockitokotlin2.mock
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.FusekiConnection
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createRDFResponse
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@Tag("unit")
class MetaDataServiceTest {
    private val fuseki: FusekiConnection = mock()
    private val metaDataService = MetaDataService(fuseki)

    @Test
    fun addMetaData() {
        val withoutMetaData = ModelFactory.createDefaultModel()
        withoutMetaData.read(InputStreamReader(javaClass.classLoader.getResourceAsStream("catalog_1.ttl")!!, StandardCharsets.UTF_8), "", "TURTLE")

        val withMetaData = metaDataService.addMetaDataToModel(withoutMetaData)
    }
}