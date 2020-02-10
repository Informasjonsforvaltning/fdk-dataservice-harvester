package no.acat.service;

import no.acat.model.ApiDocument;
import no.acat.repository.ApiDocumentRepository;
import no.acat.utils.Utils;
import no.acat.converters.apispecificationparser.ParseException;
import no.acat.common.model.ApiRegistrationPublic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
public class ApiDocumentBuilderServiceTest {
    @Mock
    private ApiDocumentRepository apiDocumentRepositoryMock;
    @Mock
    private PublisherCatClient publisherCatClientMock;
    @Mock
    private DatasetCatClient datasetCatClientMock;
    @InjectMocks
    private ApiDocumentBuilderService apiDocumentBuilderService;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(
            apiDocumentRepositoryMock,
            publisherCatClientMock,
            datasetCatClientMock
        );
    }

    @Test
    public void checkIfApiDocumentIsCreated() throws IOException, ParseException {
        String spec = Utils.getStringFromResource("enhetsregisteret-openapi3.json");
        ApiRegistrationPublic apiRegistrationPublic = new ApiRegistrationPublic();
        apiRegistrationPublic.setApiSpec(spec);
        String harvestSourceUri = "x";
        Date harvestDate = new Date();

        ApiDocument apiDocument =
            apiDocumentBuilderService.createFromApiRegistration(apiRegistrationPublic, harvestSourceUri, harvestDate);
        assertEquals(apiDocument.getHarvestSourceUri(), harvestSourceUri);
        assertEquals(apiDocument.getTitle(), "Åpne Data fra Enhetsregisteret - API Dokumentasjon");
    }

    @Test
    public void checkIfExistingApiDocumentIsUpdated() throws IOException, ParseException {
        String spec = Utils.getStringFromResource("enhetsregisteret-openapi3.json");
        ApiRegistrationPublic apiRegistrationPublic = new ApiRegistrationPublic();
        apiRegistrationPublic.setApiSpec(spec);
        String harvestSourceUri = "x";
        Date harvestDate = new Date();
        ApiDocument oldApiDocument =
            apiDocumentBuilderService.createFromApiRegistration(apiRegistrationPublic, harvestSourceUri, harvestDate);

        when(apiDocumentRepositoryMock.getApiDocumentByHarvestSourceUri(harvestSourceUri)).thenReturn(Optional.of(oldApiDocument));

        Date secondHarvestDate = new Date();
        secondHarvestDate.setTime(harvestDate.getTime() + 1000);
        apiRegistrationPublic.setCost("+1");

        ApiDocument apiDocument =
            apiDocumentBuilderService.createFromApiRegistration(apiRegistrationPublic, harvestSourceUri, secondHarvestDate);

        assertEquals(apiDocument.getId(), oldApiDocument.getId());
        assertEquals(apiDocument.getHarvestSourceUri(), harvestSourceUri);
        assertEquals(apiDocument.getCost(), apiRegistrationPublic.getCost());
        assertEquals(apiDocument.getHarvest().getChanged().get(0), secondHarvestDate);
        assertEquals(apiDocument.getHarvest().getFirstHarvested(), harvestDate);
        assertEquals(apiDocument.getHarvest().getLastChanged(), secondHarvestDate);
        assertEquals(apiDocument.getHarvest().getLastHarvested(), secondHarvestDate);
    }

}
