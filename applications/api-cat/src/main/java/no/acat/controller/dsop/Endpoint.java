package no.acat.controller.dsop;

import com.google.gson.Gson;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import no.acat.model.ApiDocument;
import no.dcat.shared.Publisher;
import no.fdk.acat.common.model.apispecification.ApiSpecification;
import no.fdk.acat.common.model.apispecification.servers.Server;
import org.elasticsearch.search.SearchHit;

import java.util.Date;
import java.util.Optional;

@Data
@Builder
public class Endpoint {
    static final String[] SOURCE_FIELDS = {"id", "publisher.id", "serviceType", "apiSpecification.servers.url", "apiSpecification.servers.description", "deprecationInfoExpirationDate"};
    @ApiModelProperty("url to the the api-description in API-catalogue")
    String apiRef;

    @ApiModelProperty("The organization number to the publisher of the API")
    String orgNo;

    @ApiModelProperty("The standard (\"type of service\") the API conforms to")
    String serviceType;

    @ApiModelProperty("The Server Object url of the API")
    String url;

    // TODO expected in DSOP, but not implemented in api document.
//    @ApiModelProperty("The date-time from which the endpoint is active")
//    Date activationDate

    @ApiModelProperty("The date-time to which the endpoint is active")
    Date expirationDate;

    @ApiModelProperty("Transport method")
    String transportProfile;

    @ApiModelProperty("The environment the url points to")
    String environment;

    static Endpoint fromElasticHit(SearchHit hit, EnvironmentEnum environment) {
        ApiDocument apiDocument = new Gson().fromJson(hit.getSourceAsString(), ApiDocument.class);

        String orgNo = Optional.ofNullable(apiDocument.getPublisher()).map(Publisher::getId).orElse(null);
        Server server = Optional.ofNullable(apiDocument.getApiSpecification())
            .map(ApiSpecification::getServers)
            .flatMap(servers ->
                servers
                    .stream()
                    .filter(item -> filterByEnvironment(item, environment))
                    .findAny())
            .orElse(null);

        if(server == null) {
            return null;
        }

        return Endpoint.builder()
            .apiRef("https://fellesdatakatalog.brreg.no/apis/" + apiDocument.getId())
            .orgNo(orgNo)
            .serviceType(apiDocument.getServiceType())
            .url(server.getUrl())
            .expirationDate(apiDocument.getDeprecationInfoExpirationDate())
            .transportProfile("eOppslag")
            .environment(environment.value)
            .build();
    }

    private static boolean filterByEnvironment(Server server, EnvironmentEnum environment) {
        if (environment == EnvironmentEnum.PRODUCTION) {
            return server.getDescription().toLowerCase().contains("production");
        } else if (environment == EnvironmentEnum.TEST) {
            return server.getDescription().toLowerCase().contains("test");
        } else {
            return false;
        }
    }
}
