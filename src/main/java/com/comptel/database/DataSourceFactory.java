/*
 *
====================================================================================================================
 * Name        : DataSourceFactory.java
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

import javax.sql.DataSource;

/**
 * Factory class providing a convenient database persistence API.
 * 
 * Note: this can probably be replaced by the factory provided by platform.
 * 
 * @author cpt2m3p
 */
public abstract class DataSourceFactory {
    
    //singleton DataSourceFactory factory instance
    private volatile static DataSourceFactory factory;

    //object used as Lock to synchronize threads when setting the factory instance
    private static final Object LOCK = new Object();

    /**
     * Return a singleton instance of DataSourceFactory.
     * @return DataSourceFactory object.
     */
    public static DataSourceFactory getInstance() {
        if(factory == null) {
            // Initialise default factory
            setInstance(new DataSourceFactoryImpl());
        }
        return factory;
    }

    /**
     * Set the internal singleton DataSourceFactory object.
     * This method is called by the platform to inject a new DataSourceFactory to the application.
     * As a business logic developer, you do not need to use that method (except from unit tests).
     * If the DataSourceFactory instance was already set (by the platform), calling this method
     * will have no effects.
     * <br>
     * Usage: 
     * <pre>
     * {@code
     * DataSourceFactory.setInstance(new DataSourceFactoryImpl());
     * }
     * <pre>
     * @param dataSourceFactory the DataSourceFactory instance
     */
    public static void setInstance(DataSourceFactory dataSourceFactory) {
        synchronized (LOCK) {
            if (factory == null) {
                factory = dataSourceFactory;
            }
        }
    }
    
   
    
    /**
     * Generic method to retrieve a data source from any JDBC compliant database. The database and pool configuration
     * are set in a file called c3p0-config.xml and referred via a configuration key.
     * c3p0-config.xml should be configured via the com.mchange.v2.c3p0.cfg.xml property set in Transaction Engine JavaOptions. 
     * 
     * <br>
     * Example c3p0-config.xml:
     * <pre>
     * {@code
     * <c3p0-config>
     *   <default-config>
     *           <property name="driverClass">com.timesten.jdbc.TimesTenDriver</property>
     *           <property name="jdbcUrl">jdbc:timesten:ccacpDS</property>
     *           <property name="user">user</property>
     *           <property name="password"></property>
     *   </default-config>
     *
     *   <named-config name="MyDataSource">
     *           <property name="driverClass">com.timesten.jdbc.TimesTenDriver</property>
     *           <property name="jdbcUrl">jdbc:timesten:ccacpDS</property>
     *           <property name="user">user</property>
     *           <property name="password"></property>
     *           <property name="acquireIncrement">5</property>
     *           <property name="minPoolSize">20</property>
     *           <property name="maxPoolSize">100</property>
     *           <property name="maxIdleTime">3600</property>
     *           <property name="maxIdleTimeExcessConnections">300</property>
     *           <property name="numHelperThreads">6</property>
     *           <property name="unreturnedConnectionTimeout">3600</property>
     *   </named-config>
     *  </c3p0-config>
     *  }
     *  </pre>
     *  For more Information on the available properties, see C3P0 documentation.
     *  <br>
     *  
     *  Usage: 
     *  <pre>
     *  
     *  {@code
     *  DataSourceFactory factory = DataSourceFactory.getInstance();
     *  DataSource source = factory.getDataSource("MyDataSource");
     *  }
     *  </pre>
     * @param namedConfigurationKey key to the configuration in the c3p0-config.xml file
     * @return a {@link DataSource} Object
     * 
     */
    public abstract DataSource getDataSource(String namedConfigurationKey);
    
    /**
     * Creates a database connection datasource with a given key.
     * 
     * @param namedConfigurationKey name of the datasource. If null is given the key is generated.
     * @param username database usernname
     * @param password database password
     * @param jdbcURL database url
     * @param driverClassName JDBC driver name
     * @return created database source.
     */
    public abstract DataSource createDataSource(String namedConfigurationKey, String username, String password, String jdbcURL, String driverClassName);
    
    public abstract void close();
}
