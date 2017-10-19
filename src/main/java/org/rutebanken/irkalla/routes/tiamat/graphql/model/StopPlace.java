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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StopPlace {

    public String id;
    public Long version;
    public Name name;
    public String versionComment;
    public String changedBy;
    public String stopPlaceType;
    public GraphqlGeometry geometry;

    public List<Quay> quays;

    public TopographicPlace topographicPlace;

    public ValidBetween validBetween;

    public String __typename;


    @JsonIgnore
    public String getNameAsString() {
        return name != null ? name.value : null;
    }

    @JsonIgnore
    public List<Quay> safeGetQuays() {
        if (quays == null) {
            quays = new ArrayList<>();
        }
        return quays;
    }

    @Override
    public String toString() {
        return "StopPlace{" +
                       "id='" + id + '\'' +
                       ", version=" + version +
                       ", name=" + name +
                       ", versionComment='" + versionComment + '\'' +
                       ", changedBy='" + changedBy + '\'' +
                       ", stopPlaceType='" + stopPlaceType + '\'' +
                       ", quays=" + quays +
                       ", validBetween=" + validBetween +
                       '}';
    }
}