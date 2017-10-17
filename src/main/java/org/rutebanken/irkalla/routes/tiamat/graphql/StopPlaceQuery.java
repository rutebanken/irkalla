package org.rutebanken.irkalla.routes.tiamat.graphql;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StopPlaceQuery {


    // Query size 1 should be sufficient when querying for fixed id and version
    private static final int DEFAULT_QUERY_SIZE = 1;

    public String operationName = "findStop";

    public Map<String, Object> variables = new HashMap<>();

    public StopPlaceQuery(String stopPlaceId, Long version) {
        variables.put("id", stopPlaceId);
        variables.put("currentVersion", version);
        variables.put("previousVersion", version - 1);
        setQuerySize(DEFAULT_QUERY_SIZE);
    }

    public void setQuerySize(int querySize) {
        variables.put("size", querySize);
    }

    public String query = "query stopPlace($id: String, $size: Int, $currentVersion: Int, $previousVersion: Int) { " +
                                  "current: stopPlace(id: $id, size: $size, version: $currentVersion) " + RESULT_DEFINITION +
                                  " previous: stopPlace(id: $id, size: $size, version: $previousVersion) " + RESULT_DEFINITION +
                                  "}";


    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            StringWriter writer = new StringWriter();
            mapper.writeValue(writer, this);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String RESULT_DEFINITION = "{" +
                                                            "id " +
                                                            "version " +
                                                            "validBetween {fromDate toDate}" +
                                                            "name {" +
                                                            "      value" +
                                                            "    }" +
                                                            "    geometry {" +
                                                            "      type" +
                                                            "      coordinates" +
                                                            "    }" +
                                                            "    keyValues {" +
                                                            "      key" +
                                                            "      values" +
                                                            "    }" +
                                                            "       topographicPlace {" +
                                                            "      topographicPlaceType" +
                                                            "      name {" +
                                                            "        value" +
                                                            "      }" +
                                                            "      parentTopographicPlace {" +
                                                            "        topographicPlaceType" +
                                                            "        name {" +
                                                            "          value" +
                                                            "        }" +
                                                            "      }" +
                                                            "    }" +
                                                            "    versionComment "+
                                                            "    changedBy "+
                                                            "    ... on StopPlace {" +
                                                            "    stopPlaceType " +
                                                            "    quays {" +
                                                            "      id" +
                                                            "       name {" +
                                                            "          value" +
                                                            "        }" +
                                                            "      geometry {" +
                                                            "        type" +
                                                            "        coordinates" +
                                                            "      }" +
                                                            "    }" +
                                                            "    }" +
                                                            "  }";
}