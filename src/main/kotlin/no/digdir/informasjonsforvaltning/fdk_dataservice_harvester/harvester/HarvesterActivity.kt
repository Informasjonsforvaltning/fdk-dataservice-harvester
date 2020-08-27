package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.HarvestAdminAdapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rabbit.RabbitMQPublisher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import java.util.*
import javax.annotation.PostConstruct

private val LOGGER = LoggerFactory.getLogger(HarvesterActivity::class.java)
private const val HARVEST_ALL_ID = "all"

@Service
class HarvesterActivity(
    private val harvestAdminAdapter: HarvestAdminAdapter,
    private val harvester: DataServiceHarvester,
    private val publisher: RabbitMQPublisher
): CoroutineScope by CoroutineScope(Dispatchers.Default) {

    @PostConstruct
    private fun fullHarvestOnStartup() = initiateHarvest(null)

    fun initiateHarvest(params: MultiValueMap<String, String>?) {
        if (params == null) LOGGER.debug("starting harvest of all data services")
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

        launch {
            harvest.join()
            LOGGER.debug("completed harvest with parameters $params")

            publisher.send(HARVEST_ALL_ID)
        }
    }
}