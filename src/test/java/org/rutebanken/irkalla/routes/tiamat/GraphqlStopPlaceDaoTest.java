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
