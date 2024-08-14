package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester.formatNowWithOsloTimeZone
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.FdkIdAndUri
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.HarvestReport
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rabbit.RabbitMQPublisher
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.*
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.DataServiceRepository
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class DataServiceService(
    private val dataServiceRepository: DataServiceRepository,
    private val rabbitPublisher: RabbitMQPublisher,
    private val turtleService: TurtleService,
) {

    fun getAll(returnType: Lang, withRecords: Boolean): String =
        turtleService.getCatalogUnion(withRecords)
            ?.let {
                if (returnType == Lang.TURTLE) it
                else parseRDFResponse(it, Lang.TURTLE, null)?.createRDFResponse(returnType)
            }
            ?: ModelFactory.createDefaultModel().createRDFResponse(returnType)

    fun getDataServiceById(id: String, returnType: Lang, withRecords: Boolean): String? =
        turtleService.getDataService(id, withRecords)
            ?.let {
                if (returnType == Lang.TURTLE) it
                else parseRDFResponse(it, Lang.TURTLE, null)?.createRDFResponse(returnType)
            }

    fun getCatalogById(id: String, returnType: Lang, withRecords: Boolean): String? =
        turtleService.getCatalog(id, withRecords)
            ?.let {
                if (returnType == Lang.TURTLE) it
                else parseRDFResponse(it, Lang.TURTLE, null)?.createRDFResponse(returnType)
            }

    fun removeDataService(id: String) {
        val start = formatNowWithOsloTimeZone()
        val meta = dataServiceRepository.findAllByFdkId(id)
        if (meta.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No data service found with fdkID $id")
        } else if (meta.none { !it.removed }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Data service with fdkID $id has already been removed")
        } else {
            dataServiceRepository.saveAll(meta.map { it.copy(removed = true) })

            val uri = meta.first().uri
            rabbitPublisher.send(listOf(
                HarvestReport(
                    id = "manual-delete-$id",
                    url = uri,
                    harvestError = false,
                    startTime = start,
                    endTime = formatNowWithOsloTimeZone(),
                    removedResources = listOf(FdkIdAndUri(fdkId = id, uri = uri))
                )
            ))
        }
    }

}
