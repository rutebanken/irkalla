package org.rutebanken.irkalla.routes.tiamat.graphql;

import org.rutebanken.irkalla.domain.ChangeType;
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

    @Value("${tiamat.graphql.path:/jersey/graphql}")
    private String tiamatGraphQLPath;

    @Override
    public StopPlaceChange getStopPlaceChange(ChangeType changeType, String id, Long version) {
        RestTemplate restTemplate = new RestTemplate();
        StopPlaceResponse rsp =
                restTemplate.exchange(tiamatUrl + tiamatGraphQLPath, HttpMethod.POST, createQueryHttpEntity(id, version), StopPlaceResponse.class).getBody();

        return toStopPlaceChange(changeType, rsp);
    }

    private StopPlaceChange toStopPlaceChange(ChangeType changeType, StopPlaceResponse rsp) {
        StopPlace current = rsp.getCurrent();
        StopPlace previous = rsp.getPreviousVersion();

        if (current == null) {
            return null;
        }

        return new StopPlaceChange(changeType, current, previous);
    }

    private HttpEntity<String> createQueryHttpEntity(String id, Long version) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");

        return new HttpEntity<>(new StopPlaceQuery(id, version).toString(), headers);
    }


}
