package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.controller

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.generated.api.DcatApNoDataservicesApi
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.returnTypeFromAcceptHeader
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.DataServiceService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import javax.servlet.http.HttpServletRequest

private val LOGGER = LoggerFactory.getLogger(DataserviceController::class.java)

@Controller
open class DataservicesController(private val dataServiceService: DataServiceService) : DcatApNoDataservicesApi {

    override fun getDataServiceById(httpServletRequest: HttpServletRequest, id: String): ResponseEntity<String> {
        LOGGER.info("get DataService with id $id")
        val returnType = returnTypeFromAcceptHeader(httpServletRequest.getHeader("Accept"))

        return dataServiceService.getDataService(id, returnType)
            ?.let { ResponseEntity(it, HttpStatus.OK) }
            ?: ResponseEntity.notFound().build()
    }

    override fun getDataServices(httpServletRequest: HttpServletRequest): ResponseEntity<String> {
        LOGGER.info("get all DataServices")
        val returnType = returnTypeFromAcceptHeader(httpServletRequest.getHeader("Accept"))
        return ResponseEntity(dataServiceService.getAllDataServices(returnType), HttpStatus.OK)
    }
}