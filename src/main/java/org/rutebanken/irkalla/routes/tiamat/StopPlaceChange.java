package org.rutebanken.irkalla.routes.tiamat;

import org.rutebanken.irkalla.domain.ChangeType;
import org.rutebanken.irkalla.routes.tiamat.graphql.model.StopPlace;
import org.rutebanken.irkalla.routes.tiamat.graphql.model.ValidBetween;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StopPlaceChange {
    public enum StopPlaceUpdateType {NAME, COORDINATES, NEW_QUAY, REMOVED_QUAY, MINOR, MAJOR}


    private ChangeType changeType;

    private StopPlaceUpdateType updateType;

    private StopPlace current;

    private StopPlace previousVersion;

    private String oldValue;

    private String newValue;

    public StopPlaceChange(ChangeType changeType, StopPlace current, StopPlace previousVersion) {
        this.changeType = changeType;
        this.current = current;
        this.previousVersion = previousVersion;
        detectUpdateType();
    }


    public Instant getChangeTime() {
        if (CollectionUtils.isEmpty(current.validBetweens)) {
            return null;
        }

        ValidBetween currentValidBetween = current.validBetweens.get(0);

        if (ChangeType.REMOVE.equals(changeType)) {
            return currentValidBetween.toDate;
        }
        return currentValidBetween.fromDate;
    }


    public ChangeType getChangeType() {
        return changeType;
    }

    public StopPlace getCurrent() {
        return current;
    }

    public StopPlace getPreviousVersion() {
        return previousVersion;
    }


    public StopPlaceUpdateType getUpdateType() {
        return updateType;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    private void detectUpdateType() {
        if (!ChangeType.UPDATE.equals(changeType)) {
            return;
        }

        // Defaulting to minor if no substantial changes are found
        updateType = StopPlaceUpdateType.MINOR;

        checkForChanges(current.getNameAsString(), previousVersion.getNameAsString(), StopPlaceUpdateType.NAME);

        // TODO do we need to verify magnitude of coord change? Seems to be small changes due to rounding.
        checkForChanges(current.geometry, previousVersion.geometry, StopPlaceUpdateType.COORDINATES);


        List<String> currentQuayIds = current.safeGetQuays().stream().map(q -> q.id).collect(Collectors.toList());
        List<String> previousVersionQuayIds = previousVersion.safeGetQuays().stream().map(q -> q.id).collect(Collectors.toList());


        List<String> newQuays = currentQuayIds.stream().filter(q -> !previousVersionQuayIds.contains(q)).collect(Collectors.toList());
        List<String> removedQuays = previousVersionQuayIds.stream().filter(q -> !currentQuayIds.contains(q)).collect(Collectors.toList());

        if (!newQuays.isEmpty()) {
            registerUpdate(StopPlaceUpdateType.NEW_QUAY);
            newValue = newQuays.toString();
            oldValue = removedQuays.toString();
        } else if (!removedQuays.isEmpty()) {
            registerUpdate(StopPlaceUpdateType.REMOVED_QUAY);
            oldValue = removedQuays.toString();
        }

    }

    private void checkForChanges(Object curr, Object pv, StopPlaceUpdateType updateType) {
        if (!Objects.equals(curr, pv)) {
            registerUpdate(updateType);
            oldValue = ObjectUtils.nullSafeToString(pv);
            newValue = ObjectUtils.nullSafeToString(curr);
        }
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
