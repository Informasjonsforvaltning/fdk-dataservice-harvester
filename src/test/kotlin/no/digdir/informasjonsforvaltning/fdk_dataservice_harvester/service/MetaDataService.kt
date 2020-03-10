package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import com.nhaarman.mockitokotlin2.mock
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.CatalogFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.DataServiceFuseki
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@Tag("unit")
class MetaDataServiceTest {
    private val dataServiceFuseki: DataServiceFuseki = mock()
    private val catalogFuseki: CatalogFuseki = mock()
    private val metaDataService = MetaDataService(dataServiceFuseki, catalogFuseki)

    @Test
    fun addMetaData() {
        val withoutMetaData = ModelFactory.createDefaultModel()
        withoutMetaData.read(InputStreamReader(javaClass.classLoader.getResourceAsStream("catalog_1.ttl")!!, StandardCharsets.UTF_8), "", "TURTLE")

        val withMetaData = metaDataService.addMetaDataToModel(withoutMetaData)
    }
}