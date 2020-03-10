package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.controller

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester.DataServiceHarvester
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.CatalogService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ApplicationStatusController(private val catalogService: CatalogService, private val dataServiceHarvester: DataServiceHarvester) {

    @GetMapping("/ping")
    fun ping(): ResponseEntity<Void> {
        return ResponseEntity.ok().build()
    }

    @GetMapping("/ready")
    fun ready(): ResponseEntity<Void> {
        try {
            catalogService.countDataServiceCatalogs()
            return ResponseEntity.ok().build()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
    }

    @GetMapping("/count")
    fun count(): ResponseEntity<Int> {
        try {
            return ResponseEntity.ok(catalogService.countDataServiceCatalogs())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
    }

    @GetMapping("/harvest")
    fun harvest(): ResponseEntity<Void> {
        dataServiceHarvester.harvestDataServiceCatalog("https://dataservice-publisher.staging.fellesdatakatalog.digdir.no/catalogs/1")
        dataServiceHarvester.harvestDataServiceCatalog("https://dataservice-publisher.staging.fellesdatakatalog.digdir.no/catalogs/2")
        return ResponseEntity.ok().build()
    }

}