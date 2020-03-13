package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.DataServiceFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.DATASERVICE_ID_0
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils.TestResponseReader
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class DataServiceServiceTest {
    private val dataServiceFuseki: DataServiceFuseki = mock()
    private val dataServiceService = DataServiceService(dataServiceFuseki)

    private val responseReader = TestResponseReader()

    @Nested
    internal inner class AllDataServices {

        @Test
        fun answerWithEmptyListWhenNoModelsSavedInFuseki() {
            whenever(dataServiceFuseki.fetchCompleteModel())
                .thenReturn(ModelFactory.createDefaultModel())

            val expected = responseReader.parseResponse("", "TURTLE")

            val response = dataServiceService.getAllDataServices(JenaType.TURTLE)

            assertTrue(expected.isIsomorphicWith(responseReader.parseResponse(response, "TURTLE")))
        }

        @Test
        fun responseIsIsomorphicWithModelFromFuseki() {
            val db0 = responseReader.parseFile("no_prefix_dataservice_0.ttl", "TURTLE")
            val db1 = responseReader.parseFile("no_prefix_dataservice_1.ttl", "TURTLE")
            val dbModel = db0.union(db1)

            whenever(dataServiceFuseki.fetchCompleteModel())
                .thenReturn(dbModel)

            val response = dataServiceService.getAllDataServices(JenaType.TURTLE)

            assertTrue(dbModel.isIsomorphicWith(responseReader.parseResponse(response, "TURTLE")))
        }
    }

    @Nested
    internal inner class DataServiceById {

        @Test
        fun responseIsNullWhenNotFoundInFuseki() {
            whenever(dataServiceFuseki.fetchByGraphName("123"))
                .thenReturn(null)

            val response = dataServiceService.getDataService("123", JenaType.TURTLE)

            assertNull(response)
        }

        @Test
        fun responseIsIsomorphicWithModelFromFuseki() {
            val dbModel = responseReader.parseFile("no_prefix_dataservice_0.ttl", "TURTLE")
            whenever(dataServiceFuseki.fetchByGraphName(DATASERVICE_ID_0))
                .thenReturn(dbModel)

            val response = dataServiceService.getDataService(DATASERVICE_ID_0, JenaType.TURTLE)

            assertTrue(dbModel.isIsomorphicWith(responseReader.parseResponse(response!!, "TURTLE")))
        }

    }
}