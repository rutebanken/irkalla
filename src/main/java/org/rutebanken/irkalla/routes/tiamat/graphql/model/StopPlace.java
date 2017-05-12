package org.rutebanken.irkalla.routes.tiamat.graphql.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StopPlace {

    public String id;
    public Long version;
    public Name name;
    public GraphqlGeometry geometry;

    public List<Quay> quays;

    public List<ValidBetween> validBetweens;


    @JsonIgnore
    public String getNameAsString() {
        return name != null ? name.value : null;
    }

    @JsonIgnore
    public List<Quay> safeGetQuays() {
        if (quays == null) {
            quays = new ArrayList<>();
        }
        return quays;
    }

}