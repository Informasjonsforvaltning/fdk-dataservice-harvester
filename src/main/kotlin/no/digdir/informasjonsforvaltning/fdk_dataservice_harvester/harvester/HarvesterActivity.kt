package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.HarvestAdminAdapter
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

    @PostConstruct
    private fun fullHarvestOnStartup() = initiateHarvest(null)

    fun initiateHarvest(params: Map<String, String>?) {
        if (params == null || params.isEmpty()) LOGGER.debug("starting harvest of all data services")
        else LOGGER.debug("starting harvest with parameters $params")

        val harvest = launch {
            harvestAdminAdapter.getDataSources(params)
                .filter { it.dataType == "dataservice" }
                .forEach {
                    if (it.url != null) {
                        try {
                            harvester.harvestDataServiceCatalog(it, Calendar.getInstance())
                        } catch (exception: Exception) {
                            LOGGER.error("Harvest of ${it.url} failed", exception)
                        }
                    }
                }
        }

        val onHarvestCompletion = launch {
            harvest.join()
            updateService.updateMetaData()

            if (params == null || params.isEmpty()) LOGGER.debug("completed harvest of all data services")
            else LOGGER.debug("completed harvest with parameters $params")

            publisher.send(HARVEST_ALL_ID)

            harvest.cancelChildren()
            harvest.cancel()
        }

        onHarvestCompletion.invokeOnCompletion { onHarvestCompletion.cancel() }
    }
}
