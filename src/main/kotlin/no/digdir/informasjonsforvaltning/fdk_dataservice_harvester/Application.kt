package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.ApplicationProperties
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.FusekiProperties
import kotlin.jvm.JvmStatic
import org.springframework.boot.SpringApplication

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties::class, FusekiProperties::class)
open class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}