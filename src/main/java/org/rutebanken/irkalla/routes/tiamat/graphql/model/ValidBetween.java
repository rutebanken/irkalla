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
