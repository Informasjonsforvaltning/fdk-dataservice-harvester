package no.acat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("unit")
public class ElasticsearchServiceTest {

    @Test
    public void constructorTest() {
        new ElasticsearchService(new ObjectMapper(), "localhost:9300", "elasticsearch");
    }

}
