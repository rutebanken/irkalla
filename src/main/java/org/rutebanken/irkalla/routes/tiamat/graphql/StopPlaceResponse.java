package org.rutebanken.irkalla.routes.tiamat.graphql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.rutebanken.irkalla.routes.tiamat.graphql.model.StopPlace;
import org.springframework.util.CollectionUtils;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StopPlaceResponse {


    public Data data;

    public StopPlaceResponse() {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class Data {
        public List<StopPlace> current;


        public List<StopPlace> previous;

    }


    @JsonIgnore
    public StopPlace getCurrent() {
        if (data == null || CollectionUtils.isEmpty(data.current)) {
            return null;
        }
        return data.current.get(0);
    }

    @JsonIgnore
    public StopPlace getPreviousVersion() {
        if (data == null || CollectionUtils.isEmpty(data.previous)) {
            return null;
        }
        return data.previous.get(0);
    }
}

