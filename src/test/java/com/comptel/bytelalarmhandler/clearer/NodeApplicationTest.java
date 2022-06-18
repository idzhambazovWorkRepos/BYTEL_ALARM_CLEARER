package com.comptel.bytelalarmhandler.clearer;

import com.comptel.database.ELEvent;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Unit test node application
 */
//@RunWith(TxeEnvironment.class)
public class NodeApplicationTest {

    private NodeApplication app;

    @Before
    public void setUp() throws Exception {
        app = new NodeApplication();
    }
//
////    @Test
//    public void shouldAddGreetingFieldToRecord() throws Exception {
//        //this test case shows how to use provided mock classes
//        // Mock context. Parameter "Greeting" will have value "Hello"
//        NodeContext ctx = new MockNodeContext(Collections.singletonMap("Greeting", "Hello"));
//        // call init
//        app.init(ctx);
//
//        MockEventRecordService eventRecordService = new MockEventRecordService();
//        // inject the service
//        app.setService(eventRecordService);
//
//        EventRecord record = new MockEventRecord();
//        record.addField("Name", "Henri");
//
//        app.process(record);
//
//        // verify that output er was written and fields are ok
//        eventRecordService.assertRecordWrittenToLink(record, "OUT");
//        assertThat(record.getField("Message").getValue(), is("Hello Henri"));
//    }
//
////    @Test
//    public void shouldChangeGreetingBasedOnNodeContextInit() throws Exception {
//        //this test case shows how to use Mockito mocks
//        NodeContext ctx = mock(NodeContext.class);
//        //return Hi when asked what is the parameter value for "Greeting"
//        when(ctx.getParameter("Greeting")).thenReturn("Hi");
//        app.init(ctx);
//
//        EventRecordService service = mock(EventRecordService.class);
//        app.setService(service);
//
//        EventRecord record = mock(EventRecord.class);
//        Field arrivingField = mock(Field.class);
//        Field messageField = mock(Field.class);
//        //create relationships between Field and Record mocks
//        when(arrivingField.getValue()).thenReturn("Henri");
//        when(record.getField("Name")).thenReturn(arrivingField);
//        when(record.addField("Message")).thenReturn(messageField);
//
//        app.process(record);
//
//        //verification that calls to mocks were made with correct arguments
//        verify(service).write("OUT", record);
//        verify(record).addField("Message");
//        verify(messageField).setValue("Hi Henri");
//    }
//
//    @After
//    public void cleanUp() throws Exception {
//        // Clean up after test
//        app.end();
//    }

    @Test
    public void tmp(){

        long l = app.parseEventIdFromSourceObject("system-DR/host-el206test.comptel.com/service-NM/process-ErrorCode:NODEMANAGER03012-EventId:1021");
        System.out.println(l);

    }

}