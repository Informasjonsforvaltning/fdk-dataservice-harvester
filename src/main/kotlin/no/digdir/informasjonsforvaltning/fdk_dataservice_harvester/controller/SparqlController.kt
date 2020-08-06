package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.controller

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.SparqlService
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpServletRequest

@Controller
class SparqlController(private val sparqlService: SparqlService) {

    @GetMapping("/sparql/describe")
    fun sparqlDescribe(@RequestParam(value = "query", required = true) query: String): ResponseEntity<String> =
        try {
            sparqlService.sparqlDescribe(query)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NO_CONTENT)
        } catch (ex: IllegalArgumentException) {
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }

    @GetMapping("/sparql/construct")
    fun sparqlConstruct(@RequestParam(value = "query", required = true) query: String): ResponseEntity<String> =
        try {
            sparqlService.sparqlConstruct(query)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NO_CONTENT)
        } catch (ex: IllegalArgumentException) {
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }

    @GetMapping("/sparql/select")
    fun sparqlSelect(@RequestParam(value = "query", required = true) query: String, httpServletRequest: HttpServletRequest): ResponseEntity<String> =
        try {
            val acceptFormat = httpServletRequest.getHeader("Accept")
            sparqlService.sparqlSelect(query = query, format = acceptFormat)
                ?.let {
                    ResponseEntity(it, HttpStatus.OK)
                }
                ?: ResponseEntity(HttpStatus.NO_CONTENT)
        } catch (ex: Exception) {
            when (ex) {
                is IllegalArgumentException -> ResponseEntity(HttpStatus.BAD_REQUEST)
                is QueryExceptionHTTP -> ResponseEntity(HttpStatus.valueOf(ex.statusCode))
                else -> ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }

    @GetMapping("/sparql/ask")
    fun sparqlAsk(@RequestParam(value = "query", required = true) query: String): ResponseEntity<Boolean> =
        try {
            ResponseEntity(sparqlService.sparqlAsk(query), HttpStatus.OK)
        } catch (ex: Exception) {
            when (ex) {
                is java.lang.IllegalArgumentException -> ResponseEntity(HttpStatus.BAD_REQUEST)
                is QueryExceptionHTTP -> ResponseEntity(HttpStatus.valueOf(ex.statusCode))
                else -> ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }

}
