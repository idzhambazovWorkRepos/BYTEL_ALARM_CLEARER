package com.comptel.bytelalarmhandler.clearer;

import com.comptel.database.DBConnectionParams;
import com.comptel.database.DBService;
import com.comptel.database.ELEvent;

import com.comptel.eventlink.core.Nodebase;
import com.comptel.mc.node.*;
import com.nokia.calm.model.Alarm;
import com.nokia.calm.rest.CalmRestClient;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static com.comptel.eventlink.core.Nodebase.*;

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
            Callable callable = () -> {
                //organizeFetchAndClearAlarms();
                fetchAndClearAlarms();
                return "this return is not used by any method";
            };

            final Future future = executorService.submit(callable);

            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.severe("InterruptedException: " + e.getCause());
                nb_abort();
            } catch (ExecutionException e) {
                e.printStackTrace();
                logger.severe("ExecutionException: " + e.getMessage());
                nb_abort();
            }catch (CancellationException e){
                e.printStackTrace();
                logger.severe("CancellationException: " + e.getMessage());
                nb_abort();
            }

        } else {
            logger.info("Schedule skipped as node is still busy sending clear alarms ");
        }

    }

//    private void organizeFetchAndClearAlarms() throws Exception {
//        logger.info("organizeFetchAndClearAlarms(): enter");
//        try {
//            List<Alarm> notClearedAlarmList = fetchAndClearAlarms();
//            while (!notClearedAlarmList.isEmpty()) {
//                notClearedAlarmList = fetchAndClearAlarms();
//            }
//        } catch (Exception e) {
//            logger.severe(String.format("An exception caught in organizeFetchAndClearAlarms(). "
//                                        + "Node will continue where it left off within next schedule time. "
//                                        + "The exception message is:\n%s", e.getMessage()));
//            throw e;
//        } finally {
//            isStillClearingAlarms.set(false);
//        }
//        logger.info("organizeFetchAndClearAlarms(): exit");
//    }
    private void fetchAndClearAlarms() throws NumberFormatException, SQLException, InterruptedException, IOException {
        logger.info("fetchAndClearAlarms(): enter");
        int count = 0;
        long alarmId = 0l;
        long evetnId = 0l;
        List<Alarm> notClearedAlarmEventsList = _restClient.getAllActiveAlarms();

        if (notClearedAlarmEventsList.isEmpty()) {
            logger.info("No more uncleared alarm in EM db for this schedule, nothing to do");
        } else {
            for (Alarm alarm : notClearedAlarmEventsList) {
                logger.info("alarm id:" + alarm.getId());
                evetnId = parseEventIdFromSourceObject(alarm.sourceObject);
                if (evetnId != 0) {
                    ELEvent elEvent = dbServiceEL.selectByEventidFromEL(evetnId);

                logger.info("AcknowledgedBy:[" + elEvent.getAckuser() + "]");
                alarmId = Long.parseLong(alarm.id);
                if (elEvent.getAckuser() == null) {
                    logger.fine(String.format("ELEventId:%s has not been acknowledged yet", elEvent.getAdditionalId()));
                } else {
                    _restClient.clearAlarm(elEvent.getAckuser(), alarmId);
                    count++;
                    logger.info(String.format("Clear is sent for ELEventId:%s EMEventId:%s",
                            elEvent.getAdditionalId(), alarmId));
                    Thread.sleep(sleepPerAlarmInMs);
                }
            }else{
                    logger.fine(String.format("Alarm cannot be sent to EM. Missing EventId"));
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
        isStillClearingAlarms.set(false);
        logger.info("fetchAndClearAlarms(): exit");

    }

    public long parseEventIdFromSourceObject(String sourceObject) {

        logger.info("parseEventIdFromSourceObject(): start");
        String eventId = null;

        if (sourceObject.isEmpty()) {
            logger.info("SourceObject not present");
            return 0L;
        }

        if (sourceObject.contains("/process-") && sourceObject.matches("(.*)EventId:(\\d+)")){
            String process = sourceObject.substring(sourceObject.lastIndexOf("/process-"));
            logger.info("parseEventIdFromSourceObject(): process = " + process);

            String[] splittedProcess = process.split("-");

            if (splittedProcess.length != 0) {
                String eventIdGroup = splittedProcess[splittedProcess.length - 1];
                eventId = eventIdGroup.substring(eventIdGroup.indexOf(":") + 1).trim();
                logger.info("parseEventIdFromSourceObject(): EventId = " + eventId);
            }
        }else{
            logger.info("Wrong SourceObject format provided: " + sourceObject + " Skip to next alarm.");
            return  0L;
        }

        if (eventId == null){
            return 0L;
        }

        logger.info("parseEventIdFromSourceObject(): end");

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
