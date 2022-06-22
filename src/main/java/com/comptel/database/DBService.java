package com.comptel.database;

import java.sql.*;
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

    private static final String SELECT_FROM_EL = "select ackuser from el_events where eventid = ?";

    public DBService(DBConnectionParams dbConnectionParams) {
        Locale.setDefault(Locale.ENGLISH);
        //As datasourcefactory will generate an id from the params we passed, we are sending
        //simply "null" as the id.
        dataSource = DataSourceFactory.getInstance().createDataSource(null, 
                        dbConnectionParams.getUserName(), dbConnectionParams.getPassword(), 
                        dbConnectionParams.getUrl(), dbConnectionParams.getDriver());
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
            while (rs.next()){
                elEvent.setAdditionalId(String.valueOf(eventId));
                    if (rs.getString("ackuser") != null) {
                        elEvent.setAckuser(rs.getString("ackuser"));
                    } else {
                        elEvent.setAckuser(null);
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
