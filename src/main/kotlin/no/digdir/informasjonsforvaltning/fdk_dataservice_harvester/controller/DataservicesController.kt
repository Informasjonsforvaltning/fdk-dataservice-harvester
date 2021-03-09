package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.controller

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.generated.api.DcatApNoDataservicesApi
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.jenaTypeFromAcceptHeader
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.DataServiceService
import org.apache.jena.riot.Lang
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import javax.servlet.http.HttpServletRequest

private val LOGGER = LoggerFactory.getLogger(DataServicesController::class.java)

@Controller
open class DataServicesController(private val dataServiceService: DataServiceService) : DcatApNoDataservicesApi {

    override fun getDataServiceById(httpServletRequest: HttpServletRequest, id: String): ResponseEntity<String> {
        LOGGER.info("get DataService with id $id")
        val returnType = jenaTypeFromAcceptHeader(httpServletRequest.getHeader("Accept"))

        return if (returnType == Lang.RDFNULL) ResponseEntity(HttpStatus.NOT_ACCEPTABLE)
        else {
            dataServiceService.getDataServiceById(id, returnType ?: Lang.TURTLE)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

}