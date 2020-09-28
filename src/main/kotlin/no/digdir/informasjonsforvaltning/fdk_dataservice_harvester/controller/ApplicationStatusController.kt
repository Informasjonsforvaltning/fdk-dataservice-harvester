package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.controller

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.DataServiceService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ApplicationStatusController(private val dataServiceService: DataServiceService) {

    @GetMapping("/ping")
    fun ping(): ResponseEntity<Void> =
        ResponseEntity.ok().build()

    @GetMapping("/ready")
    fun ready(): ResponseEntity<Void> =
        ResponseEntity.ok().build()

    @GetMapping("/count")
    fun count(): ResponseEntity<Long> {
        try {
            return ResponseEntity.ok(dataServiceService.countMetaData())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
    }

}