package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester;

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.spring.CachableDispatcherServlet;
import org.apache.jena.riot.RIOT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

@SpringBootApplication
@EnableConfigurationProperties
public class Application {

    private static Logger LOGGER = LoggerFactory.getLogger(Application.class);

    @Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
    public DispatcherServlet dispatcherServlet() {
        return new CachableDispatcherServlet();
    }

    public static void main(String[] args) {
        RIOT.init();
        SpringApplication.run(Application.class, args);
    }
}