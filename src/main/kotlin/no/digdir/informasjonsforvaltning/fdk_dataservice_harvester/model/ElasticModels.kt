package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "dataservices", type = "dataservice")
data class DataserviceDB (
    @Id val id: String,
    val harvestSourceUri: String,
    val apiSpecUrl: String,
    val title: String,
    val description: String,
    val titleFormatted: String
)
/*
*
  @JsonProperty("id")
  private String id;

  @JsonProperty("harvestSourceUri")
  private String harvestSourceUri;

  @JsonProperty("apiSpecUrl")
  private String apiSpecUrl;

  @JsonProperty("apiSpec")
  private Object apiSpec;

  @JsonProperty("apiSpecification")
  private ApiSpecification apiSpecification;

  @JsonProperty("harvest")
  private Object harvest;

  @JsonProperty("title")
  private String title;

  @JsonProperty("description")
  private String description;

  @JsonProperty("formats")
  private List<String> formats = new ArrayList<>();

  @JsonProperty("publisher")
  private Publisher publisher;

  @JsonProperty("contactPoint")
  private List<Contact> contactPoint = new ArrayList<>();

  @JsonProperty("datasetReference")
  private List<DatasetReference> datasetReference = new ArrayList<>();

  @JsonProperty("titleFormatted")
  private String titleFormatted;

  @JsonProperty("descriptionFormatted")
  private String descriptionFormatted;

  @JsonProperty("nationalComponent")
  private Boolean nationalComponent;

  @JsonProperty("isOpenAccess")
  private Boolean isOpenAccess;

  @JsonProperty("isOpenLicense")
  private Boolean isOpenLicense;

  @JsonProperty("isFree")
  private Boolean isFree;

  @JsonProperty("statusCode")
  private String statusCode;

  @JsonProperty("deprecationInfoExpirationDate")
  private String deprecationInfoExpirationDate;

  @JsonProperty("deprecationInfoReplacedWithUrl")
  private String deprecationInfoReplacedWithUrl;
* */