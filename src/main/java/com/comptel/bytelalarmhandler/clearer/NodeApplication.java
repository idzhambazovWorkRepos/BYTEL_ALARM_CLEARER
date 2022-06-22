package com.comptel.bytelalarmhandler.clearer;

import com.comptel.database.DBConnectionParams;
import com.comptel.database.DBService;
import com.comptel.database.ELEvent;

import com.comptel.mc.node.*;
import com.nokia.calm.model.Alarm;
import com.nokia.calm.rest.CalmRestClient;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class NodeApplication implements BusinessLogic, Schedulable {

    private static final Logger logger = Logger.getLogger(NodeApplication.class.getCanonicalName());
    private DBService dbServiceEL;
    private long sleepPerAlarmInMs = 0;
    private AtomicBoolean isStillClearingAlarms = new AtomicBoolean(false);
    private ExecutorService executorService = Executors.newSingleThreadExecutor();


    /*
     * Calm changes
     */
    private String _calmHost;
    private int _calmRestApiPort;
    CalmRestClient _restClient;

    public void init(NodeContext nodeContext) throws Exception {
        initDBService(nodeContext);
        sleepPerAlarmInMs = Long.parseLong(nodeContext.getParameter("sleepPerAlarmInMs"));

        _calmHost = nodeContext.getParameter("CalmHost").trim();
        _calmRestApiPort = nodeContext.getParameterInt("CalmRestApiPort");
        _restClient = new CalmRestClient(_calmHost, _calmRestApiPort);
    }

    private void initDBService(NodeContext nodeContext) {
        String dbConnectionUrl = nodeContext.getParameter("ELJDBC.URL");
        String dbUser = nodeContext.getParameter("ELJDBC.User");
        String dbPassword = nodeContext.getParameter("ELJDBC.Password");
        String dbDriver = nodeContext.getParameter("ELJDBC.Driver");
        DBConnectionParams paramsEL
                           = new DBConnectionParams(dbConnectionUrl, dbUser, dbPassword, dbDriver);
        dbServiceEL = new DBService(paramsEL);

    }

    @Override
    public void process(EventRecord er) throws Exception {
        logger.info("Processing record");
    }

    @Override
    public void schedule() throws Exception {
        if (isStillClearingAlarms.compareAndSet(false, true)) {
            logger.info("Schedule starts sending clear alarms ");
            Callable callable = new Callable() {

                @Override
                public Object call() throws Exception {
                    organizeFetchAndClearAlarms();
                    return "this return is not used by any method";
                }
            };
            executorService.submit(callable);

        } else {
            logger.info("Schedule skipped as node is still busy sending clear alarms ");
        }
    }

    private void organizeFetchAndClearAlarms() throws Exception {
        logger.info("organizeFetchAndClearAlarms(): enter");
        try {
            List<Alarm> notClearedAlarmList = fetchAndClearAlarms();
            while (!notClearedAlarmList.isEmpty()) {
                notClearedAlarmList = fetchAndClearAlarms();
            }
        } catch (Exception e) {
            logger.severe(String.format("An exception caught in organizeFetchAndClearAlarms(). "
                                        + "Node will continue where it left off within next schedule time. "
                                        + "The exception message is:\n%s", e.getMessage()));
            throw e;
        } finally {
            isStillClearingAlarms.set(false);
        }
        logger.info("organizeFetchAndClearAlarms(): exit");
    }
    private List<Alarm> fetchAndClearAlarms() throws NumberFormatException, SQLException, InterruptedException, IOException {
        logger.info("fetchAndClearAlarms(): enter");
        int count = 0;
        long eventId = 0l;
        List<Alarm> notClearedAlarmEventsList = _restClient.getAllActiveAlarms();
        if (notClearedAlarmEventsList.isEmpty()) {
            logger.info("No more uncleared alarm in EM db for this schedule, nothing to do");
        } else {
            for (Alarm alarm : notClearedAlarmEventsList) {
                logger.info("alarm:" + alarm.alarmCode);
                ELEvent elEvent = dbServiceEL.selectByEventidFromEL(parseEventIdFromSourceObject(alarm.sourceObject));
                logger.info("ACKUSER:[" + elEvent.getAckuser() + "]");
                eventId = Long.parseLong(alarm.id);
                if (elEvent.getAckuser() == null){
                    logger.fine(String.format("ELEventId:%s has not been acknowledged yet", elEvent.getAdditionalId()));
                }else{
                    _restClient.clearAlarm(elEvent.getAckuser(), eventId);
                    count++;
                    logger.info(String.format("Clear is sent for ELEventId:%s EMEventId:%s",
                            elEvent.getAdditionalId(), eventId));
                    Thread.sleep(sleepPerAlarmInMs);
                }

            }

            if (count > 0) {
                logger.info(String.format("%d clear alarms sent to EM. %d alarms in EM checked "
                                          + "for being acknowledged", count, notClearedAlarmEventsList.size()));
            } else {
                logger.fine(String.format("0 clear alarms sent to EM. %d alarms in EM checked "
                                          + "for being acknowledged", notClearedAlarmEventsList.size()));
            }
        }
        logger.info("fetchAndClearAlarms(): exit");
        return notClearedAlarmEventsList;

    }

    public long parseEventIdFromSourceObject(String sourceObject) {

        logger.info("parseEventIdFromSourceObject(): start");
        String eventId = "";

        //process-StreamName:TEST_ALARM2-NodeName:ALARMGENERATOR_BLN3-UDPCOLLECTOR013
        if (sourceObject.isEmpty()) {
            return 0L;
        }

        String process = sourceObject.substring(sourceObject.lastIndexOf("/process-"));
        logger.info("parseEventIdFromSourceObject(): process = " + process);

        String[] splittedProcess = process.split("-");

        if (splittedProcess.length != 0) {
            String eventIdGroup = splittedProcess[splittedProcess.length - 1];
            eventId = eventIdGroup.substring(eventIdGroup.indexOf(":") + 1);
            logger.info("parseEventIdFromSourceObject(): EventId = " + eventId);
        }

        logger.info("parseEventIdFromSourceObject(): end");
        if (eventId.isEmpty()) {
            return 0L;
        }

        return Long.parseLong(eventId);
    }

    @Override
    public void setService(EventRecordService eventRecordService) {
    }

    @Override
    public void flush() throws Exception {
    }

    @Override
    public void pause(int reason) throws Exception {
    }

    @Override
    public void request(String requestString) throws Exception {
    }

    @Override
    public void resume(int reason) throws Exception {
    }

    @Override
    public void end() throws Exception {
        executorService.shutdownNow();
    }

}
