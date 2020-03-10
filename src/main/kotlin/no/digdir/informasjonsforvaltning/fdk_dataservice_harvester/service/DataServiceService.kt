package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.DataServiceFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.addDefaultPrefixes
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createRDFResponse
import org.springframework.stereotype.Service

@Service
class DataServiceService(private val dataServiceFuseki: DataServiceFuseki) {

    fun getAllDataServices(returnType: JenaType): String =
        dataServiceFuseki
            .fetchCompleteModel()
            .addDefaultPrefixes()
            .createRDFResponse(returnType)

    fun getDataService(id: String, returnType: JenaType): String? =
        dataServiceFuseki
            .fetchByGraphName(id)
            ?.addDefaultPrefixes()
            ?.createRDFResponse(returnType)

}