/*
 *
====================================================================================================================
 * Name        : DataSourceFactoryImpl.java
 * Part of     : Comptel Policy Control
 * Description : PCRF business logic node functionality.
 *
 * Copyright (c) 2012 Comptel Corporation. All rights reserved.
 *
 * This material, including documentation and any related computer programs, is protected by copyright controlled by
 * Comptel Corporation. All rights are reserved. Copying, including reproducing, storing, adapting or translating, any
 * or all of this material requires the prior written consent of Comptel Corporation. This material also contains
 * confidential information which may not be disclosed to others without the prior written consent of
 * Comptel Corporation.
 *
====================================================================================================================
*/

package com.comptel.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Data source factory implementation class.
 * The underlying connection pool implementation is based on the C3P0 framework.
 *
 * @see {@link ComboPooledDataSource}
 * @author cpt2m3p
 */
public class DataSourceFactoryImpl extends DataSourceFactory {
    private static final Logger logger = Logger.getLogger(DataSourceFactoryImpl.class.getName());

    private ConcurrentHashMap<String, ComboPooledDataSource> dataSources;

    public DataSourceFactoryImpl() {
        dataSources = new ConcurrentHashMap<String, ComboPooledDataSource>();
    }

    
    @Override
    public DataSource getDataSource(String configurationName){
        return dataSources.get(configurationName);
    }

    @Override
    public void close() {
        if(logger.isLoggable(Level.FINEST)) {
            logger.finest("Closing data source factory, amount of data sources to be closed: " + String.valueOf(dataSources.size()));
        }
        
        for (ComboPooledDataSource pooledDataSource : dataSources.values()) {
            pooledDataSource.close();
        }
        
        dataSources.clear();
    }

    public synchronized DataSource createNamedDataSource(String namedConfigurationKey) {
        if (dataSources.containsKey(namedConfigurationKey)) {
            if(logger.isLoggable(Level.FINEST)) {
                logger.finest("A pooled datasource already exists for configuration " + namedConfigurationKey + " returning it.");
            }
            return dataSources.get(namedConfigurationKey);
        }
        
        checkDataSourceConfiguration();
        ComboPooledDataSource pooledDataSource = new ComboPooledDataSource(namedConfigurationKey);
        dataSources.put(namedConfigurationKey,pooledDataSource);
        return pooledDataSource;
    }
    
    @Override
    public synchronized DataSource createDataSource(String id, String username, String password, String jdbcURL, String driverClassName) {
        if(id == null) {
            // Generate id
            id = getDataSourceID(username, password, jdbcURL, driverClassName);
        }
        if (dataSources.containsKey(id)) {
            logger.finest("A pooled datasource already exists for configuration " + id + " returning it.");
            return dataSources.get(id);
        }
        
        try {
            if(logger.isLoggable(Level.FINEST)) {
                logger.finest("Creating new pooled datasources for database configuration: " + id);
            }
            ComboPooledDataSource pooledDataSource = new ComboPooledDataSource();
            pooledDataSource.setDriverClass(driverClassName);
            pooledDataSource.setJdbcUrl(jdbcURL);
            pooledDataSource.setUser(username);
            pooledDataSource.setPassword(password);
            
            int maxThreadCount = 2;
            pooledDataSource.setMaxPoolSize(maxThreadCount);
            int preparedStatementCount = 15;
            pooledDataSource.setMaxStatementsPerConnection(preparedStatementCount);
            
            dataSources.put(id, pooledDataSource);
            return pooledDataSource;
        } catch (PropertyVetoException ex){
            throw new RuntimeException(ex);
        }
    }

    private String getDataSourceID(String username, String password, String url, String driver) {
        StringBuilder builder = new StringBuilder();
        builder.append(username);
        builder.append("/");
        builder.append(url);
        builder.append("/");
        builder.append(driver);
        return builder.toString();
    }
    
    
    /**
     * Method to check if the datasource XML file was configured in system property.
     * @throws Exception if an error occurs
     */
    private void checkDataSourceConfiguration() {
        String config = System.getProperty("com.mchange.v2.c3p0.cfg.xml");
        if(config == null || config.equals("")){
            throw new RuntimeException("System property com.mchange.v2.c3p0.cfg.xml is not defined.");
        }
        
        File file = new File(config);
        if(!file.exists()){
            throw new RuntimeException("DataSource configuration file not found: " + config);
        }
    }
}
