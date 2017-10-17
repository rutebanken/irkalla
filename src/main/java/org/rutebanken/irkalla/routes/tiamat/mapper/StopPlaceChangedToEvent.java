package org.rutebanken.irkalla.routes.tiamat.mapper;

import org.rutebanken.irkalla.domain.CrudEvent;
import org.rutebanken.irkalla.routes.tiamat.StopPlaceChange;
import org.rutebanken.irkalla.routes.tiamat.graphql.model.GraphqlGeometry;
import org.rutebanken.irkalla.routes.tiamat.graphql.model.StopPlace;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.wololo.geojson.Geometry;
import org.wololo.geojson.Point;

import java.util.ArrayList;
import java.util.List;

@Component
public class StopPlaceChangedToEvent {

    public CrudEvent toEvent(StopPlaceChange stopPlaceChange) {
        StopPlace currentVersion = stopPlaceChange.getCurrent();

        CrudEvent.Builder event = CrudEvent.builder()
                                          .type(CrudEvent.EntityType.StopPlace)
                                          .entityClassifier(stopPlaceChange.getEntityClassifier())
                                          .action(CrudEvent.Action.valueOf(stopPlaceChange.getCrudAction().name()))
                                          .changeType(stopPlaceChange.getUpdateType())
                                          .oldValue(stopPlaceChange.getOldValue())
                                          .newValue(stopPlaceChange.getNewValue())
                                          .externalId(currentVersion.id)
                                          .version(currentVersion.version)
                                          .name(currentVersion.getNameAsString())
                                          .comment(currentVersion.versionComment)
                                          .username(currentVersion.changedBy)
                                          .geometry(toGeometry(currentVersion.geometry))
                                          .location(stopPlaceChange.getLocation())
                                          .eventTime(stopPlaceChange.getChangeTime());

        return event.build();
    }


    private Geometry toGeometry(GraphqlGeometry graphqlGeometry) {

        if (graphqlGeometry != null && "Point".equals(graphqlGeometry.type)) {
            List<Double> coordinates = new ArrayList<>();
            graphqlGeometry.coordinates.forEach(coordList -> coordinates.addAll(coordList));

            return new Point(coordinates.stream().mapToDouble(c -> c).toArray());
        }
        return null;
    }

}
