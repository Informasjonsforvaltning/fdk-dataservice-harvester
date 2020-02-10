package no.acat.controller;

import no.acat.utils.Utils;
import no.acat.bindings.ConvertRequest;
import no.acat.bindings.ConvertResponse;
import no.fdk.webutils.exceptions.BadRequestException;
import org.junit.Assert;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
public class ConvertControllerTest {

    @InjectMocks
    private ConvertController convertController;

    @Test
    public void testConvertWithoutSpecShouldFailed() throws Exception {

        ConvertRequest convertRequest = mock(ConvertRequest.class);

        when(convertRequest.getSpec()).thenReturn(null);
        assertThrows(BadRequestException.class, () -> convertController.convert(convertRequest));
    }

    @Test
    public void checkIfReturnResponsebodySuccess() throws IOException, BadRequestException {

        String spec = Utils.getStringFromResource("enhetsregisteret-openapi3.json");
        ConvertRequest convertRequest = ConvertRequest.builder().spec(spec).build();

        ConvertResponse convertResponse = convertController.convert(convertRequest);

        Assert.assertEquals(
            convertResponse.getApiSpecification().getInfo().getTitle(),
            "Åpne Data fra Enhetsregisteret - API Dokumentasjon");
    }

    @Test
    public void checkIfGettingSpecFromUrlSuccess() throws Exception {
        String url = Utils.getResourceUrl("enhetsregisteret-openapi3.json");
        ConvertRequest convertRequest = ConvertRequest.builder().url(url).build();

        ConvertResponse convertResponse = convertController.convert(convertRequest);

        Assert.assertEquals(
            convertResponse.getApiSpecification().getInfo().getTitle(),
            "Åpne Data fra Enhetsregisteret - API Dokumentasjon");
    }

    @Test
    public void checkIfDownloadingSpecFailed() throws Exception {
        String url = "https://fake.url";

        ConvertRequest convertRequest = ConvertRequest.builder().url(url).build();

        assertThrows(BadRequestException.class, () -> convertController.convert(convertRequest));
    }
}
