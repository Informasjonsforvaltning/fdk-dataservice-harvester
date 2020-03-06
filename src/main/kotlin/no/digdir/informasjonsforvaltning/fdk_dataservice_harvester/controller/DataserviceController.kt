package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.controller

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.generated.api.DataservicesApi
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.generated.model.Catalog
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.DataserviceService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import javax.servlet.http.HttpServletRequest

private val LOGGER = LoggerFactory.getLogger(DataserviceController::class.java)

@Controller
open class DataserviceController(private val dataserviceService: DataserviceService): DataservicesApi {

    override fun getCatalogs(httpServletRequest: HttpServletRequest): ResponseEntity<List<Catalog>> {
        LOGGER.info("get all dataservice catalogs")
        return ResponseEntity(dataserviceService.getAllCatalogs(), HttpStatus.OK)
    }
}