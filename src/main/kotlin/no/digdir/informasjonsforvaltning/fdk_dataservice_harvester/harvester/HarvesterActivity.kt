package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.HarvestAdminAdapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.HarvestAdminParameters
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rabbit.RabbitMQPublisher
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.UpdateService
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.Calendar

private val LOGGER = LoggerFactory.getLogger(HarvesterActivity::class.java)

@Service
class HarvesterActivity(
    private val harvestAdminAdapter: HarvestAdminAdapter,
    private val harvester: DataServiceHarvester,
    private val publisher: RabbitMQPublisher,
    private val updateService: UpdateService
): CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val activitySemaphore = Semaphore(1)

    @EventListener
    private fun fullHarvestOnStartup(event: ApplicationReadyEvent) =
        initiateHarvest(HarvestAdminParameters(null, null, null), false)

    fun initiateHarvest(params: HarvestAdminParameters, forceUpdate: Boolean) {
        if (params.harvestAllDataServices()) LOGGER.debug("starting harvest of all data services, force update: $forceUpdate")
        else LOGGER.debug("starting harvest with parameters $params, force update: $forceUpdate")

        launch {
            activitySemaphore.withPermit {
                try {
                    harvestAdminAdapter.getDataSources(params)
                        .filter { it.dataType == "dataservice" }
                        .filter { it.url != null }
                        .map { async { harvester.harvestDataServiceCatalog(it, Calendar.getInstance(), forceUpdate) } }
                        .awaitAll()
                        .filterNotNull()
                        .also { updateService.updateMetaData() }
                        .also {
                            if (params.harvestAllDataServices()) LOGGER.debug("completed harvest with parameters $params, force update: $forceUpdate")
                            else LOGGER.debug("completed harvest of all catalogs, force update: $forceUpdate") }
                        .run { publisher.send(this) }
                } catch (ex: Exception) {
                    LOGGER.error("harvest failure", ex)
                }
            }
        }
    }

}
