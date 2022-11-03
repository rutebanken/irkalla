/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package org.rutebanken.irkalla.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.geojson.Geometry;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrudEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrudEvent.class);
    private static final int MAX_STRING_LENGTH = 255;

    public enum EntityType {StopPlace}

    public enum Action {
        CREATE, UPDATE, REMOVE, DELETE
    }

    public String correlationId;

    public Instant eventTime;

    public Instant registeredTime;

    public EntityType entityType;

    public String entityClassifier;

    public Action action;

    public String changeType;

    public String externalId;

    public Long version;

    public String name;

    public String oldValue;

    public String newValue;

    public String comment;

    public String username;

    public Geometry geometry;

    public String location;

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

        public Builder entityClassifier(String entityClassifier) {
            event.entityClassifier = entityClassifier;
            return this;
        }

        public Builder registeredTime(Instant time) {
            event.registeredTime = time;
            return this;
        }

        public Builder eventTime(Instant time) {
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

        public Builder location(String location) {
            event.location = location;
            return this;
        }


        public Builder newValue(String newValue) {
            if (newValue !=null && newValue.length() > MAX_STRING_LENGTH) {
                LOGGER.warn("Trimming newValue since its greater than max string length: {}, {}",MAX_STRING_LENGTH,  newValue);
            }
            event.newValue = StringUtils.substring(newValue,0, MAX_STRING_LENGTH);
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

        public Builder comment(String comment) {
            if (comment !=null && comment.length() > MAX_STRING_LENGTH) {
                LOGGER.warn("Trimming comment since its greater than max string length:{}, {}",MAX_STRING_LENGTH, comment);
            }
            event.comment = StringUtils.substring(comment,0, MAX_STRING_LENGTH);
            return this;
        }

        public Builder username(String username) {
            event.username = username;
            return this;
        }

        public Builder changeType(Enum changeType) {
            if (changeType != null) {
                event.changeType = changeType.toString();
            }
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
