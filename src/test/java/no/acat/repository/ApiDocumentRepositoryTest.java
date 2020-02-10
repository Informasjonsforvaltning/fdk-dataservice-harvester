package no.acat.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.acat.model.ApiDocument;
import no.acat.service.ElasticsearchService;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
public class ApiDocumentRepositoryTest {
    @Mock
    private ElasticsearchService mockElasticsearchService;
    @Mock
    private ObjectMapper mapper;
    @Mock
    private SearchResponse searchResponse;
    @Mock
    private Client client;
    @Mock
    private SearchRequestBuilder searchRequestBuilder;
    @Mock
    private BulkRequestBuilder bulkRequestBuilder;
    @Mock
    private BulkResponse bulkResponse;
    @Mock
    private ListenableActionFuture listenableActionFuture;

    @InjectMocks
    private ApiDocumentRepository spyApiDocumentRepository;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(
            mockElasticsearchService,
            mapper,
            searchResponse,
            client,
            searchRequestBuilder,
            bulkRequestBuilder,
            bulkResponse,
            listenableActionFuture
        );
    }

    @Test
    public void checkIf_Count_ReturnOK() {

        when(mockElasticsearchService.getClient()).thenReturn(client);
        when(client.prepareSearch("acat")).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.setTypes(anyString())).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.setQuery(QueryBuilders.matchAllQuery())).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.setSize(anyInt())).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.get()).thenReturn(searchResponse);

        SearchHits hits = mock(SearchHits.class);
        when(searchResponse.getHits()).thenReturn(hits);
        when(hits.getTotalHits()).thenReturn(10L);

        long count = spyApiDocumentRepository.getCount();

        Assert.assertEquals(10, count);

    }


    @Test
    public void checkIf_apiDocumentByHarvestSourceUri_hitsLengthIsOne_returnApiDocument() {

        String id = "1002";
        ApiDocument apiDocument = new ApiDocument();
        apiDocument.setId(id);

        when(mockElasticsearchService.getClient()).thenReturn(client);
        when(client.prepareSearch("acat")).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.setTypes(anyString())).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.setQuery(QueryBuilders.termQuery("harvestSourceUri", "harvestSourceUri"))).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.get()).thenReturn(searchResponse);

        SearchHits hits = mock(SearchHits.class);
        SearchHit hit = mock(SearchHit.class);
        SearchHit[] shits = {hit};

        when(searchResponse.getHits()).thenReturn(hits);
        when(hits.getHits()).thenReturn(shits);
        when(shits[0].getSourceAsString()).thenReturn("{\"id\":\"1002\"}");

        Optional<ApiDocument> expected = spyApiDocumentRepository.getApiDocumentByHarvestSourceUri("harvestSourceUri");

        Assert.assertThat(expected.get().getId(), is(id));

    }


    @Test
    public void checkIf_createOrReplaceApiDocument_hasFailures() throws IOException {

        String id = "1002";
        ApiDocument apiDocument = new ApiDocument();

        when(mockElasticsearchService.getClient()).thenReturn(client);
        when(client.prepareBulk()).thenReturn(bulkRequestBuilder);
        when(mapper.writeValueAsString(apiDocument)).thenReturn("{\"id\":\"1002\"}");
        when(bulkRequestBuilder.execute()).thenReturn(listenableActionFuture);
        when(listenableActionFuture.actionGet()).thenReturn(bulkResponse);
        when(bulkResponse.hasFailures()).thenReturn(true);

        assertThrows(RuntimeException.class, () -> spyApiDocumentRepository.createOrReplaceApiDocument(apiDocument));

    }

    @Test
    public void checkIf_createOrReplaceApiDocument_hasPassed() throws IOException {

        String id = "1002";
        ApiDocument apiDocument = new ApiDocument();

        when(mockElasticsearchService.getClient()).thenReturn(client);
        when(client.prepareBulk()).thenReturn(bulkRequestBuilder);
        when(mapper.writeValueAsString(apiDocument)).thenReturn("{\"id\":\"1002\"}");
        when(bulkRequestBuilder.execute()).thenReturn(listenableActionFuture);
        when(listenableActionFuture.actionGet()).thenReturn(bulkResponse);
        when(bulkResponse.hasFailures()).thenReturn(false);

        spyApiDocumentRepository.createOrReplaceApiDocument(apiDocument);

        verify(listenableActionFuture, times(1)).actionGet();
        verify(bulkResponse, times(1)).hasFailures();
    }

    @Test
    public void check_deleteApiDocumentByIds_IfIdsSize_IsZero() {

        List<String> ids = new ArrayList<>();

        spyApiDocumentRepository.deleteApiDocumentByIds(ids);

        Assert.assertTrue(ids.size() == 0);

    }

    @Test
    public void checkIf_deleteApiDocumentByIds_hasFailures() {

        List<String> ids = new ArrayList<>();
        ids.add("1");

        when(mockElasticsearchService.getClient()).thenReturn(client);
        when(client.prepareBulk()).thenReturn(bulkRequestBuilder);
        when(bulkRequestBuilder.execute()).thenReturn(listenableActionFuture);
        when(listenableActionFuture.actionGet()).thenReturn(bulkResponse);
        when(bulkResponse.hasFailures()).thenReturn(true);

        assertThrows(RuntimeException.class, () -> spyApiDocumentRepository.deleteApiDocumentByIds(ids));

    }

    @Test
    public void checkIf_deleteApiDocumentByIds_passed() {

        List<String> ids = new ArrayList<>();
        ids.add("1");

        when(mockElasticsearchService.getClient()).thenReturn(client);
        when(client.prepareBulk()).thenReturn(bulkRequestBuilder);
        when(bulkRequestBuilder.execute()).thenReturn(listenableActionFuture);
        when(listenableActionFuture.actionGet()).thenReturn(bulkResponse);
        when(bulkResponse.hasFailures()).thenReturn(false);

        spyApiDocumentRepository.deleteApiDocumentByIds(ids);

        verify(listenableActionFuture, times(1)).actionGet();
        verify(bulkResponse, times(1)).hasFailures();
    }

}
