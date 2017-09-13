package org.rutebanken.irkalla.routes.tiamat.graphql.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Name {
    public String value;

    public Name(String value) {
        this.value = value;
    }

    public Name() {
    }

    @Override
    public String toString() {
        return "Name{" +
                       "value='" + value + '\'' +
                       '}';
    }
}

