package org.rutebanken.irkalla.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.wololo.geojson.Geometry;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrudEvent {

    public enum EntityType {StopPlace}

    public enum Action {
        CREATE, UPDATE, REMOVE
    }

    public String correlationId;

    public Instant eventTime;

    public EntityType entityType;

    public Action action;

    public String changeType;

    public String externalId;

    public Long version;

    public String name;

    public String oldValue;

    public String newValue;

    public Geometry geometry;

    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            StringWriter writer = new StringWriter();
            mapper.writeValue(writer, this);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {

        protected CrudEvent event = new CrudEvent();

        private Builder() {
        }

        public Builder action(CrudEvent.Action action) {
            event.action = action;
            return this;
        }

        public Builder type(CrudEvent.EntityType type) {
            event.entityType = type;
            return this;
        }

        public Builder time(Instant time) {
            event.eventTime = time;
            return this;
        }

        public Builder name(String name) {
            event.name = name;
            return this;
        }

        public Builder geometry(Geometry geometry) {
            event.geometry = geometry;
            return this;
        }

        public Builder newValue(String newValue) {
            event.newValue = newValue;
            return this;
        }

        public Builder oldValue(String oldValue) {
            event.oldValue = oldValue;
            return this;
        }

        public Builder externalId(String externalId) {
            event.externalId = externalId;
            return this;
        }

        public Builder version(Long version) {
            event.version = version;
            return this;
        }

        public Builder changeType(String changeType) {
            event.changeType = changeType;
            return this;
        }

        public Builder correlationId(String correlationId) {
            event.correlationId = correlationId;
            return this;
        }


        public CrudEvent build() {

            if (event.action == null) {
                throw new IllegalArgumentException("No action");
            }
            if (event.entityType == null) {
                throw new IllegalArgumentException("No entityType");
            }
            if (event.version == null) {
                throw new IllegalArgumentException("No version");
            }
            if (event.externalId == null) {
                throw new IllegalArgumentException("No externalId");
            }
            if (event.eventTime == null) {
                throw new IllegalArgumentException("No eventTime");
            }
            return event;
        }
    }


}
