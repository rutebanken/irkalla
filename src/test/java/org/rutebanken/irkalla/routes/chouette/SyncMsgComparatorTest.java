package org.rutebanken.irkalla.routes.chouette;

import org.apache.activemq.command.ActiveMQMessage;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.rutebanken.irkalla.Constants.*;

public class SyncMsgComparatorTest {


    @Test
    public void testComparator() throws Exception {
        List<ActiveMQMessage> sortedList = Arrays.asList(msg(SYNC_OPERATION_FULL_WITH_DELETE_UNUSED_FIRST, null),
                msg(SYNC_OPERATION_FULL, "url"), msg(SYNC_OPERATION_FULL, null),
                msg(SYNC_OPERATION_DELTA, "url"), msg(SYNC_OPERATION_DELTA, null));


        List<ActiveMQMessage> toBeSortedList = new ArrayList<>(sortedList);
        Collections.sort(toBeSortedList, new SyncMsgComparator());

        Assert.assertEquals(sortedList, toBeSortedList);


        List<ActiveMQMessage> toBeSortedListReversed = new ArrayList<>(sortedList);
        Collections.reverse(toBeSortedListReversed);
        Collections.sort(toBeSortedListReversed, new SyncMsgComparator());
        Assert.assertEquals(sortedList, toBeSortedListReversed);
    }


    private ActiveMQMessage msg(String syncOperation, String nextBatchUrl) throws Exception {
        ActiveMQMessage msg = new ActiveMQMessage();
        msg.setProperty(HEADER_NEXT_BATCH_URL, nextBatchUrl);
        msg.setProperty(HEADER_SYNC_OPERATION, syncOperation);
        return msg;
    }
}
