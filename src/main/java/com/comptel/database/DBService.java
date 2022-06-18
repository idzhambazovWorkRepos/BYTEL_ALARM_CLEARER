package com.comptel.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author cpt2vot
 */
public class DBService {

    private static final Logger LOGGER = Logger.getLogger(DBService.class.getCanonicalName());
    private DataSource dataSource;

    private static final String SELECT_FROM_EL =
            "select ACKUSER from el_events where EVENTID = ?";

    private static final String SELECT_ALL_FROM_EL = "SELECT ackuser FROM el_events";
    public DBService(DBConnectionParams dbConnectionParams) {
        Locale.setDefault(Locale.ENGLISH);
        //As datasourcefactory will generate an id from the params we passed, we are sending
        //simply "null" as the id.
        dataSource = DataSourceFactory.getInstance().createDataSource(null, 
                        dbConnectionParams.getUserName(), dbConnectionParams.getPassword(), 
                        dbConnectionParams.getUrl(), dbConnectionParams.getDriver());
    }


    public List<ELEvent> selectAllFromEL() throws SQLException {
        LOGGER.fine(String.format("%s", SELECT_ALL_FROM_EL));
        long startTime = System.nanoTime();
        ELEvent elEvent = new ELEvent();
        List<ELEvent> eventsList = new ArrayList<>();

        ResultSet rs = null;
        Connection connection = null;
        Statement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(SELECT_ALL_FROM_EL);
            if (rs.next() == false) {
                LOGGER.info("FETCH SIZE 0 ");
                elEvent.setAckuser("alarmDeleted");
            } else {
                while (rs.next()) {
                    LOGGER.info("READ");
                    elEvent = new ELEvent();
                    if (rs.getString("ACKUSER") != null) {
                        LOGGER.info("ACKUSER " + rs.getString("ACKUSER"));
                        elEvent.setAckuser(rs.getString("ACKUSER"));
                    } else {
                        LOGGER.info("NO ACKUSER");
                        elEvent.setAckuser("");
                    }
                    eventsList.add(elEvent);
                    LOGGER.info("CURRENT LIST SIZE:" + eventsList.size());
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error occurred for '" + SELECT_ALL_FROM_EL +"' query", e);
            throw e;
        } finally {
            closeConnectionResources(connection, statement, rs);
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            long endTime = System.nanoTime();
            double queryDuration = (endTime - startTime)/1e6;
            LOGGER.fine("QueryDuration(SELECT_ALL_FROM_EL): " + queryDuration+"ms");
        }

        return eventsList;
    }

    public ELEvent selectByEventidFromEL(long eventId) throws SQLException {
        LOGGER.fine(String.format("%s with EVENT_ID=%d", SELECT_FROM_EL, eventId));
        long startTime = System.nanoTime();
        ELEvent elEvent = new ELEvent();

        ResultSet rs = null;
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(SELECT_FROM_EL);
            statement.setLong(1, eventId);
            rs = statement.executeQuery();
            if (rs.getFetchSize() == 0) {
                elEvent.setAckuser("alarmDeleted");
            } else if (rs.next()) {
                elEvent.setAdditionalId(String.valueOf(eventId));
                if (rs.getString("ACKUSER") != null) {
                    elEvent.setAckuser(rs.getString("ACKUSER"));
                } else {
                    elEvent.setAckuser("");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error occurred for '"+SELECT_FROM_EL+"' query", e);
            throw e;
        } finally {
            closeConnectionResources(connection, statement, rs);
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            long endTime = System.nanoTime();
            double queryDuration = (endTime - startTime)/1e6;
            LOGGER.fine("QueryDuration(SELECT_FROM_EL): " + queryDuration+"ms");
        }

        return elEvent;
    }
    private void closeConnectionResources(Connection connection, Statement statement, ResultSet resultSet) {

        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "", e);
            }
        }

        if (statement != null) {
            try {
                statement.close();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "", e);
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "", e);
            }
        }
    }
}
