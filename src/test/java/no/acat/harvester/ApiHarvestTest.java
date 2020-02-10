package no.acat.harvester;

import no.acat.configuration.AppProperties;
import no.acat.model.ApiDocument;
import no.acat.repository.ApiDocumentRepository;
import no.acat.service.ApiDocumentBuilderService;
import no.acat.service.RegistrationApiClient;
import no.acat.common.model.ApiRegistrationPublic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
public class ApiHarvestTest {

    @Mock
    private ApiDocumentBuilderService apiDocumentBuilderServiceMock;
    @Mock
    private ApiDocumentRepository apiDocumentRepositoryMock;
    @Mock
    private RegistrationApiClient registrationApiClientMock;
    @Mock
    private AppProperties appProperties;
    @InjectMocks
    private ApiHarvester harvester;

    private ApiDocument apiDocument = new ApiDocument();

    @Test
    public void harvestAllOK() throws Throwable {

        List<ApiRegistrationPublic> publishedApis = new ArrayList<>();
        publishedApis.add(new ApiRegistrationPublic());

        when(apiDocumentBuilderServiceMock.createFromApiRegistration(any(), any(), any())).thenReturn(apiDocument);
        when(registrationApiClientMock.getPublished()).thenReturn(publishedApis);
        when(appProperties.getApiRegistrationsFile()).thenReturn("test-apis.csv");

        harvester.harvestAll();

        verify(apiDocumentRepositoryMock, times(4)).createOrReplaceApiDocument(apiDocument);
    }


    @Test
    public void harvestAllShouldWorkWithEmtpyRegistration() throws Throwable {
        when(registrationApiClientMock.getPublished()).thenReturn(new ArrayList<>());
        when(appProperties.getApiRegistrationsFile()).thenReturn("empty-test-apis.csv");

        harvester.harvestAll();

        verify(apiDocumentRepositoryMock, times(0)).createOrReplaceApiDocument(apiDocument);
    }

    @Test
    public void harvestAllShouldWorkWithEmtpyPublished() throws Throwable {
        when(registrationApiClientMock.getPublished()).thenReturn(new ArrayList<>());
        when(apiDocumentBuilderServiceMock.createFromApiRegistration(any(), any(), any())).thenReturn(apiDocument);
        when(appProperties.getApiRegistrationsFile()).thenReturn("test-apis.csv");

        harvester.harvestAll();

        verify(apiDocumentRepositoryMock, times(3)).createOrReplaceApiDocument(any());
    }

    @Test
    public void shouldGetExceptionWhenHarvestApiFails() {
        when(registrationApiClientMock.getPublished()).thenReturn(null);
        when(appProperties.getApiRegistrationsFile()).thenReturn("test-apis.csv");

        harvester.RETRY_COUNT_API_RETRIEVAL = 5;
        assertThrows(RuntimeException.class, () -> harvester.harvestAll());//Throws exception.
    }
}
