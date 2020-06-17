package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils

import org.slf4j.LoggerFactory
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.Testcontainers
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration

abstract class ApiTestContext {

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of(
                "fuseki.dataserviceUri=http://localhost:${fusekiContainer.getMappedPort(API_PORT)}/fuseki/dataservice-harvest",
                "fuseki.metaUri=http://localhost:${fusekiContainer.getMappedPort(API_PORT)}/fuseki/dataservice-meta"
            ).applyTo(configurableApplicationContext.environment)
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(ApiTestContext::class.java)
        var fusekiContainer: KGenericContainer

        init {

            startMockServer()

            Testcontainers.exposeHostPorts(LOCAL_SERVER_PORT)

            fusekiContainer = KGenericContainer("eu.gcr.io/digdir-fdk-infra/fdk-fuseki-service:latest")
                .withExposedPorts(API_PORT)
                .waitingFor(HttpWaitStrategy()
                    .forPort(API_PORT)
                    .forPath("/fuseki/dataservice-meta")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(1)))

            fusekiContainer.start()

            addTestDataToFuseki(HARVESTED, "dataservice-harvest?graph=$TEST_HARVEST_SOURCE_ID", fusekiContainer.getMappedPort(API_PORT))
            addTestDataToFuseki(DATASERVICE_META_0, "dataservice-meta?graph=$DATASERVICE_ID_0", fusekiContainer.getMappedPort(API_PORT))
            addTestDataToFuseki(DATASERVICE_META_1, "dataservice-meta?graph=$DATASERVICE_ID_1", fusekiContainer.getMappedPort(API_PORT))
            addTestDataToFuseki(CATALOG_META_0, "dataservice-meta?graph=$CATALOG_ID_0", fusekiContainer.getMappedPort(API_PORT))
            addTestDataToFuseki(CATALOG_META_1, "dataservice-meta?graph=$CATALOG_ID_1", fusekiContainer.getMappedPort(API_PORT))

            try {
                val con = URL("http://localhost:5000/ping").openConnection() as HttpURLConnection
                con.connect()
                if (con.responseCode != 200) {
                    logger.debug("Ping to mock server failed")
                    stopMockServer()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
    }

}

// Hack needed because testcontainers use of generics confuses Kotlin
class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)
