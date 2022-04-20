package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.HarvestAdminAdapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.HarvestAdminParameters
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rabbit.RabbitMQPublisher
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.UpdateService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct

private val LOGGER = LoggerFactory.getLogger(HarvesterActivity::class.java)
private const val HARVEST_ALL_ID = "all"

@Service
class HarvesterActivity(
    private val harvestAdminAdapter: HarvestAdminAdapter,
    private val harvester: DataServiceHarvester,
    private val publisher: RabbitMQPublisher,
    private val updateService: UpdateService
): CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val activitySemaphore = Semaphore(1)

    @PostConstruct
    private fun fullHarvestOnStartup() = initiateHarvest(null)

    fun initiateHarvest(params: HarvestAdminParameters?) {
        if (params == null) LOGGER.debug("starting harvest of all data services")
        else LOGGER.debug("starting harvest with parameters $params")

        launch {
            activitySemaphore.withPermit {
                harvestAdminAdapter.getDataSources(params ?: HarvestAdminParameters())
                    .filter { it.dataType == "dataservice" }
                    .filter { it.url != null }
                    .map { async { harvester.harvestDataServiceCatalog(it, Calendar.getInstance()) } }
                    .awaitAll()
                    .filterNotNull()
                    .also { updateService.updateMetaData() }
                    .also {
                        if (params != null) LOGGER.debug("completed harvest with parameters $params")
                        else LOGGER.debug("completed harvest of all catalogs") }
                    .run { publisher.send(this) }
            }
        }
    }
}
