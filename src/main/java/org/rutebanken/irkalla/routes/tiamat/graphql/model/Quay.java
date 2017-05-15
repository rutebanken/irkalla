package org.rutebanken.irkalla.routes.tiamat.graphql.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Quay {
    public String id;
    public Name name;
    public GraphqlGeometry geometry;

    public Quay(String id, Name name, GraphqlGeometry geometry) {
        this.id = id;
        this.name = name;
        this.geometry = geometry;
    }

    public Quay() {
    }
}
