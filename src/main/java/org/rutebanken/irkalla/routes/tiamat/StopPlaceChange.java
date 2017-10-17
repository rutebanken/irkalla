package org.rutebanken.irkalla.routes.tiamat;

import com.google.common.base.Joiner;
import org.rutebanken.irkalla.domain.CrudAction;
import org.rutebanken.irkalla.routes.tiamat.graphql.model.GraphqlGeometry;
import org.rutebanken.irkalla.routes.tiamat.graphql.model.StopPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Representation of a change for a stop place.
 */
public class StopPlaceChange {
    public enum StopPlaceUpdateType {NAME, COORDINATES, TYPE, NEW_QUAY, REMOVED_QUAY, MINOR, MAJOR}

    private static final Logger logger = LoggerFactory.getLogger(StopPlaceChange.class);


    private CrudAction crudAction;

    private StopPlaceUpdateType updateType;

    private StopPlace current;

    private StopPlace previousVersion;

    private List<String> oldValue = new ArrayList<>();

    private List<String> newValue = new ArrayList<>();

    public StopPlaceChange(CrudAction crudAction, StopPlace current, StopPlace previousVersion) {
        this.crudAction = crudAction;
        this.current = current;
        this.previousVersion = previousVersion;
        detectUpdateType();
    }


    public Instant getChangeTime() {
        Instant changeTime = null;
        if (current.validBetween != null) {
            if (CrudAction.REMOVE.equals(crudAction)) {
                changeTime = current.validBetween.toDate;
            } else {
                changeTime = current.validBetween.fromDate;
            }
        }

        if (changeTime == null) {
            return Instant.now();
        }
        return changeTime;
    }


    public CrudAction getCrudAction() {
        return crudAction;
    }

    public StopPlace getCurrent() {
        return current;
    }

    public StopPlace getPreviousVersion() {
        return previousVersion;
    }

    public String getEntityClassifier() {
        if (current.stopPlaceType != null) {
            return current.stopPlaceType;
        }
        if (current.keyValues != null) {
            return current.keyValues.stream().filter(kv -> kv != null && kv.values != null).anyMatch(kv -> "IS_PARENT_STOP_PLACE".equals(kv.key) && kv.values.stream().anyMatch(v -> "false".equalsIgnoreCase(v))) ? "multiModal" : null;
        }
        return null;
    }

    public StopPlaceUpdateType getUpdateType() {
        return updateType;
    }

    public String getOldValue() {
        return oldValue.isEmpty() ? null : Joiner.on("\n").join(oldValue);
    }

    public String getNewValue() {
        return newValue.isEmpty() ? null : Joiner.on("\n").join(newValue);
    }

    private void detectUpdateType() {
        if (!CrudAction.UPDATE.equals(crudAction)) {
            return;
        }

        // Defaulting to minor if no substantial changes are found
        updateType = StopPlaceUpdateType.MINOR;

        if (current == null || previousVersion == null) {
            logger.warn("Unable to detect update type for unknown current and/or previous version of stops. Current: " + current + ", previous: " + previousVersion);
            return;
        }


        checkForChanges(current.getNameAsString(), previousVersion.getNameAsString(), StopPlaceUpdateType.NAME);
        checkForChanges(current.stopPlaceType, previousVersion.stopPlaceType, StopPlaceUpdateType.TYPE);

        // TODO do we need to verify magnitude of coord change? Seems to be small changes due to rounding.
        checkForChanges(current.geometry, previousVersion.geometry, StopPlaceUpdateType.COORDINATES);

        List<String> currentQuayIds = current.safeGetQuays().stream().map(q -> q.id).collect(Collectors.toList());
        List<String> previousVersionQuayIds = previousVersion.safeGetQuays().stream().map(q -> q.id).collect(Collectors.toList());


        List<String> newQuays = currentQuayIds.stream().filter(q -> !previousVersionQuayIds.contains(q)).collect(Collectors.toList());
        List<String> removedQuays = previousVersionQuayIds.stream().filter(q -> !currentQuayIds.contains(q)).collect(Collectors.toList());

        if (!newQuays.isEmpty()) {
            registerUpdate(StopPlaceUpdateType.NEW_QUAY);
            newValue.add(newQuays.toString());
            oldValue.add(removedQuays.toString());
        } else if (!removedQuays.isEmpty()) {
            registerUpdate(StopPlaceUpdateType.REMOVED_QUAY);
            oldValue.add(removedQuays.toString());
        }

    }

    private void checkForChanges(Object curr, Object pv, StopPlaceUpdateType updateType) {
        if (!Objects.equals(curr, pv)) {
            registerUpdate(updateType);
            oldValue.add(formatValue(pv));
            newValue.add(formatValue(curr));
        }
    }


    private String formatValue(Object value) {
        if (value instanceof GraphqlGeometry) {
            return formatGeometry((GraphqlGeometry) value);
        }
        return ObjectUtils.nullSafeToString(value);
    }

    private String formatGeometry(GraphqlGeometry geometry) {
        if ("Point".equals(geometry.type)) {
            if (!CollectionUtils.isEmpty(geometry.coordinates)) {
                List<Double> coordinates = geometry.coordinates.get(0);
                if (coordinates != null && coordinates.size() > 1) {
                    Double x = coordinates.get(0);
                    Double y = coordinates.get(1);
                    return "(" + x + "," + y + ")";
                }
            }
        }
        return null;
    }

    private void registerUpdate(StopPlaceUpdateType updateType) {
        if (StopPlaceUpdateType.MINOR.equals(this.updateType)) {
            this.updateType = updateType;
        } else {
            // Set as MAJOR if multiple substantial changes are detected
            this.updateType = StopPlaceUpdateType.MAJOR;
        }
    }

}
