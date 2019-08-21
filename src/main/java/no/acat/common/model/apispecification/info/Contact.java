package no.fdk.acat.common.model.apispecification.info;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Contact {
    private String name;
    private String url;
    private String email;
}
