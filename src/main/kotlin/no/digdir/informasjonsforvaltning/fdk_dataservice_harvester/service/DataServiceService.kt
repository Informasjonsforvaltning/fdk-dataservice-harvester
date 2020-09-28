package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.UNION_ID
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.*
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.CatalogRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.DataServiceRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.MiscellaneousRepository
import org.apache.jena.rdf.model.ModelFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class DataServiceService(
    private val catalogRepository: CatalogRepository,
    private val dataServiceRepository: DataServiceRepository,
    private val miscellaneousRepository: MiscellaneousRepository
) {

    fun countMetaData(): Long =
        catalogRepository.count()

    fun getAll(returnType: JenaType): String =
        miscellaneousRepository.findByIdOrNull(UNION_ID)
            ?.let { ungzip(it.turtle) }
            ?.let {
                if (returnType == JenaType.TURTLE) it
                else parseRDFResponse(it, JenaType.TURTLE, null)?.createRDFResponse(returnType)
            }
            ?: ModelFactory.createDefaultModel().createRDFResponse(returnType)

    fun getDataServiceById(id: String, returnType: JenaType): String? =
        dataServiceRepository.findOneByFdkId(id)
            ?.let { ungzip(it.turtleDataService) }
            ?.let {
                if (returnType == JenaType.TURTLE) it
                else parseRDFResponse(it, JenaType.TURTLE, null)?.createRDFResponse(returnType)
            }

    fun getCatalogById(id: String, returnType: JenaType): String? =
        catalogRepository.findOneByFdkId(id)
            ?.let { ungzip(it.turtleCatalog) }
            ?.let {
                if (returnType == JenaType.TURTLE) it
                else parseRDFResponse(it, JenaType.TURTLE, null)?.createRDFResponse(returnType)
            }

}
