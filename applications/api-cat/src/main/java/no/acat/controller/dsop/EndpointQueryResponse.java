package no.acat.controller.dsop;

import lombok.Builder;
import lombok.Data;
import org.elasticsearch.action.search.SearchResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Builder
public class EndpointQueryResponse {
    List<Endpoint> endpoints;
    long total;

    static EndpointQueryResponse fromElasticResponse(SearchResponse elasticResponse, EnvironmentEnum environment) {
        long total = elasticResponse.getHits().getTotalHits();
        List<Endpoint> endpoints = Arrays
            .stream(elasticResponse.getHits().getHits())
            .map(hit -> Endpoint.fromElasticHit(hit, environment))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return EndpointQueryResponse.builder()
            .total(total)
            .endpoints(endpoints)
            .build();
    }
}
