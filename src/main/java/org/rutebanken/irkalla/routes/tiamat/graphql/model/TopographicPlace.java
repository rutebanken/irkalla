package org.rutebanken.irkalla.routes.tiamat.graphql.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopographicPlace {

    public TopographicPlace parentTopographicPlace;

    public Name name;

    public String topographicPlaceType;
}
