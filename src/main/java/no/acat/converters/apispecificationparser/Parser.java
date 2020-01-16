package no.acat.converters.apispecificationparser;

import no.acat.common.model.apispecification.ApiSpecification;

public interface Parser {

    boolean canParse(String spec);

    ApiSpecification parse(String spec) throws ParseException;
}
