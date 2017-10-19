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

package org.rutebanken.irkalla.routes.tiamat;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rutebanken.irkalla.domain.CrudAction;
import org.rutebanken.irkalla.routes.tiamat.graphql.GraphQLStopPlaceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GraphQLStopPlaceDao.class)
public class GraphqlStopPlaceDaoTest {

    @Autowired
    private GraphQLStopPlaceDao stopPlaceDao;


    @Test
    @Ignore // Test against running Tiamat
    public void testGetStopPlace() {
        StopPlaceChange stopPlaceChange = stopPlaceDao.getStopPlaceChange(CrudAction.CREATE, "NSR:StopPlace:3512", 3L);

        Assert.assertNotNull(stopPlaceChange);
        Assert.assertNotNull(stopPlaceChange.getCurrent());
        Assert.assertNotNull(stopPlaceChange.getPreviousVersion());
    }

}
