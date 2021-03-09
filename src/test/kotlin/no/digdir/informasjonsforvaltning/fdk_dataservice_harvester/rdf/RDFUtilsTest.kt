package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.CATALOG_ID_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.CATALOG_ID_1
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATASERVICE_ID_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATASERVICE_ID_1
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.TestResponseReader
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

}