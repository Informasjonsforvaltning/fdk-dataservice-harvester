package no.acat.controller;

import no.acat.model.ApiDocument;
import no.acat.repository.ApiDocumentRepository;
import no.fdk.webutils.exceptions.NotFoundException;
import org.junit.Assert;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
public class ApiRestControllerTest {

    @Mock
    private ApiDocumentRepository apiDocumentRepository;
    @InjectMocks
    private ApiRestController controller;

    @Test
    public void getApiDocument_ShouldReturnApiDocument() throws Exception {
        String id = "a";

        ApiDocument testDocument = new ApiDocument();
        when(apiDocumentRepository.getById(id)).thenReturn(Optional.of(testDocument));

        ApiDocument apiDocument = controller.getApiDocument(id);

        Assert.assertSame(testDocument, apiDocument);
    }

    @Test
    public void getApiDocument_WhenInvalidId_ShouldFailWithNotFoundException() throws NotFoundException, IOException {
        String id = "b";

        when(apiDocumentRepository.getById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> controller.getApiDocument(id));
    }

    @Test
    public void getApiSpec_ShouldReturnApiSpec() throws Exception {
        String id = "testid";
        String testSpec = "testspec";

        ApiDocument testDocument = new ApiDocument();
        testDocument.setApiSpec(testSpec);

        when(apiDocumentRepository.getById(id)).thenReturn(Optional.of(testDocument));

        String spec = controller.getApiSpec(id);

        Assert.assertSame(testSpec, spec);
    }
}
