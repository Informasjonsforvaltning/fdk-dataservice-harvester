package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.controller

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.jenaTypeFromAcceptHeader
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.DataServiceService
import org.apache.jena.riot.Lang
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

private val LOGGER = LoggerFactory.getLogger(CatalogsController::class.java)

@Controller
@CrossOrigin
@RequestMapping(
    value = ["/catalogs"],
    produces = ["text/turtle", "text/n3", "application/rdf+json", "application/ld+json", "application/rdf+xml", "application/n-triples"]
)
open class CatalogsController(private val dataServiceService: DataServiceService) {

    @GetMapping("/{id}")
    fun getCatalogById(
        @RequestHeader(HttpHeaders.ACCEPT) accept: String?,
        @PathVariable id: String
    ): ResponseEntity<String> {
        LOGGER.info("get DataService catalog with id $id")
        val returnType = jenaTypeFromAcceptHeader(accept)

        return if (returnType == Lang.RDFNULL) ResponseEntity(HttpStatus.NOT_ACCEPTABLE)
        else {
            dataServiceService.getCatalogById(id, returnType ?: Lang.TURTLE)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping
    fun getCatalogs(@RequestHeader(HttpHeaders.ACCEPT) accept: String?): ResponseEntity<String> {
        LOGGER.info("get all DataService catalogs")
        val returnType = jenaTypeFromAcceptHeader(accept)

        return if (returnType == Lang.RDFNULL) ResponseEntity(HttpStatus.NOT_ACCEPTABLE)
        else ResponseEntity(dataServiceService.getAll(returnType ?: Lang.TURTLE), HttpStatus.OK)
    }
}
