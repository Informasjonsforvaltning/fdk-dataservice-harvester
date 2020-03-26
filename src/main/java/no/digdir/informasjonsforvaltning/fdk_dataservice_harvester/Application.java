package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester;

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.ApplicationProperties;
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.FusekiProperties;
import org.apache.jena.riot.RIOT;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class, FusekiProperties.class})
public class Application {

    public static void main(String[] args) {
        RIOT.init();
        SpringApplication.run(Application.class, args);
    }
}