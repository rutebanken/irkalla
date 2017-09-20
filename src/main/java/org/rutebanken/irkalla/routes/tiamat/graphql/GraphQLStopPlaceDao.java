package org.rutebanken.irkalla.routes.tiamat.graphql;

import org.rutebanken.irkalla.domain.CrudAction;
import org.rutebanken.irkalla.routes.tiamat.StopPlaceChange;
import org.rutebanken.irkalla.routes.tiamat.StopPlaceDao;
import org.rutebanken.irkalla.routes.tiamat.graphql.model.StopPlace;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("stopPlaceDao")
public class GraphQLStopPlaceDao implements StopPlaceDao {

    @Value("${tiamat.url}")
    private String tiamatUrl;

    @Value("${tiamat.graphql.path:/services/stop_places/graphql}")
    private String tiamatGraphQLPath;

    @Override
    public StopPlaceChange getStopPlaceChange(CrudAction crudAction, String id, Long version) {
        RestTemplate restTemplate = new RestTemplate();
        StopPlaceResponse rsp =
                restTemplate.exchange(tiamatUrl + tiamatGraphQLPath, HttpMethod.POST, createQueryHttpEntity(id, version), StopPlaceResponse.class).getBody();

        return toStopPlaceChange(crudAction, id, version, rsp);
    }

    private StopPlaceChange toStopPlaceChange(CrudAction crudAction, String id, Long version, StopPlaceResponse rsp) {
        StopPlace current = rsp.getCurrent();

        if (current == null || !id.equals(current.id)) {
            return null;
        }

        // Tiamat returns version 1 if queried for v 0. Verify that version is actually previous
        StopPlace previous = null;
        if (rsp.getPreviousVersion() != null && id.equals(rsp.getPreviousVersion().id) && rsp.getPreviousVersion().version == (version - 1)) {
            previous = rsp.getPreviousVersion();
        }

        return new StopPlaceChange(crudAction, current, previous);
    }

    private HttpEntity<String> createQueryHttpEntity(String id, Long version) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");

        return new HttpEntity<>(new StopPlaceQuery(id, version).toString(), headers);
    }


}
