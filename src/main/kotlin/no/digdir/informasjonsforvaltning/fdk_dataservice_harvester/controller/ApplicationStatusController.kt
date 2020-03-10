package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.controller

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester.DataServiceHarvester
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.DataServiceService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ApplicationStatusController(private val dataServiceService: DataServiceService, private val dataServiceHarvester: DataServiceHarvester) {

    @GetMapping("/ping")
    fun ping(): ResponseEntity<Void> {
        dataServiceHarvester.harvestDataServiceCatalog("https://raw.githubusercontent.com/Informasjonsforvaltning/dataservice-publisher/master/tests/catalog_1.ttl")
        dataServiceHarvester.harvestDataServiceCatalog("https://raw.githubusercontent.com/Informasjonsforvaltning/dataservice-publisher/master/tests/catalog_2.ttl")
        return ResponseEntity.ok().build()
    }

    @GetMapping("/ready")
    fun ready(): ResponseEntity<Void> {
        try {
            dataServiceService.countDataServiceCatalogs()
            return ResponseEntity.ok().build()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
    }

    @GetMapping("/count")
    fun count(): ResponseEntity<Int> {
        try {
            return ResponseEntity.ok(dataServiceService.countDataServiceCatalogs())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
    }
}