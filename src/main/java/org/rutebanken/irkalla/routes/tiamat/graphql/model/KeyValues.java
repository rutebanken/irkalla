package org.rutebanken.irkalla.routes.tiamat.graphql.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyValues {

    public String key;

    public List<String> values;

    @Override
    public String toString() {
        return "KeyValues{" +
                       "key='" + key + '\'' +
                       ", values=" + values +
                       '}';
    }
}
