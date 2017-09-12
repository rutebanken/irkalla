package org.rutebanken.irkalla.routes.tiamat.graphql.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidBetween {
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXXX";
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern=DATE_TIME_PATTERN)
    public Instant fromDate;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern=DATE_TIME_PATTERN)
    public Instant toDate;

    @Override
    public String toString() {
        return "ValidBetween{" +
                       "fromDate=" + fromDate +
                       ", toDate=" + toDate +
                       '}';
    }
}
