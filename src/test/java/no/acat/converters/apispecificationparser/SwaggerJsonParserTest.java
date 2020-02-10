package no.acat.converters.apispecificationparser;

import io.swagger.v3.oas.models.OpenAPI;
import no.acat.common.model.apispecification.ApiSpecification;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Tag("unit")
public class SwaggerJsonParserTest {

    private final SwaggerJsonParser swaggerJsonParser = new SwaggerJsonParser();

    private static String spec;
    private static String invalidSpec;

    @BeforeAll
    public static void setup() throws IOException {
        spec = IOUtils.toString(new ClassPathResource("fs-api-swagger.json").getInputStream(), StandardCharsets.UTF_8);
        invalidSpec = IOUtils.toString(new ClassPathResource("fs-api-swagger-invalid-missing-title.json").getInputStream(), StandardCharsets.UTF_8);
    }

    @Test
    public void CanParse_ShouldReturnTrue() {
        boolean result = swaggerJsonParser.canParse(spec);
        Assert.assertTrue(result);
    }

    @Test
    public void CanParse_ShouldReturnFalse() {
        boolean result = swaggerJsonParser.canParse(invalidSpec);
        Assert.assertFalse(result);
    }

    @Test
    public void ParseToOpenAPI_ShouldParse() throws Exception {
        OpenAPI parsed = swaggerJsonParser.parseToOpenAPI(spec);
        Assert.assertEquals(parsed.getInfo().getTitle(), "FS-API");
    }

    @Test
    public void Parse_ShouldParse() throws Exception {
        ApiSpecification parsed = swaggerJsonParser.parse(spec);
        Assert.assertEquals(parsed.getInfo().getTitle(), "FS-API");
    }
}
