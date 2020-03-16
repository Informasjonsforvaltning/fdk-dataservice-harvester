package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.controller

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.generated.api.DcatApNoCatalogsApi
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.jenaTypeFromAcceptHeader
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.CatalogService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import javax.servlet.http.HttpServletRequest

private val LOGGER = LoggerFactory.getLogger(CatalogsController::class.java)

@Controller
open class CatalogsController(private val catalogService: CatalogService) : DcatApNoCatalogsApi {

    override fun getCatalogById(httpServletRequest: HttpServletRequest, id: String): ResponseEntity<String> {
        LOGGER.info("get DataService catalog with id $id")
        val returnType = jenaTypeFromAcceptHeader(httpServletRequest.getHeader("Accept"))

        return catalogService.getDataServiceCatalog(id, returnType)
            ?.let { ResponseEntity(it, HttpStatus.OK) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)
    }

    override fun getCatalogs(httpServletRequest: HttpServletRequest): ResponseEntity<String> {
        LOGGER.info("get all DataService catalogs")
        val returnType = jenaTypeFromAcceptHeader(httpServletRequest.getHeader("Accept"))
        return ResponseEntity(catalogService.getAllDataServiceCatalogs(returnType), HttpStatus.OK)
    }
}