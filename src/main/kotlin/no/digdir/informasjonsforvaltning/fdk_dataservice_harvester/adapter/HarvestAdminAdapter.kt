package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.ApplicationProperties
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.HarvestDataSource
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

private val logger = LoggerFactory.getLogger(HarvestAdminAdapter::class.java)

@Service
class HarvestAdminAdapter(private val applicationProperties: ApplicationProperties) {

    private val defaultHeaders: HttpHeaders = HttpHeaders().apply {
        accept = listOf(MediaType.APPLICATION_JSON)
        contentType = MediaType.APPLICATION_JSON
    }

    fun getDataSources(queryParams: MultiValueMap<String, String>?): List<HarvestDataSource> {
        val url = "${applicationProperties.harvestAdminRootUrl}/datasources"
        val uriBuilder = UriComponentsBuilder.fromHttpUrl(url).queryParams(queryParams)
        try {
            val response: ResponseEntity<List<HarvestDataSource>> = RestTemplate().exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                HttpEntity<Any>(defaultHeaders),
                object : ParameterizedTypeReference<List<HarvestDataSource>>() {})

            return response.body ?: emptyList()
        } catch (e: HttpClientErrorException) {
            logger.error("Error fetching harvest urls from GET / ${uriBuilder.toUriString()}. ${e.statusText} (${e.statusCode.value()})")
        } catch (e: RestClientException) {
            logger.error(e.message)
        }
        return emptyList()
    }

}