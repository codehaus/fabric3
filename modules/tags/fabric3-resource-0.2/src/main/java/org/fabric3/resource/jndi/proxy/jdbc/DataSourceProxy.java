/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.fabric3.resource.jndi.proxy.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.fabric3.resource.jndi.proxy.AbstractProxy;
import org.fabric3.spi.resource.DataSourceRegistry;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/**
 * Proxy class for a JNDI-based datasource.
 * 
 * @version $Revision$ $Date$
 */
public class DataSourceProxy extends AbstractProxy<DataSource> implements DataSource {
    
    private DataSourceRegistry dataSourceRegistry;
    private String dataSourceKey;

    @Property
    public void setDataSourceKey(String dataSourceKey) {
        this.dataSourceKey = dataSourceKey;
    }

    @Reference
    public void setDataSourceRegistry(DataSourceRegistry dataSourceRegistry) {
        this.dataSourceRegistry = dataSourceRegistry;
    }
    
    @Override
    @Init
    public void init() throws NamingException {
        // TODO change this to map reference once re-evaluating references are done
        super.init();
        dataSourceRegistry.registerDataSource(dataSourceKey, this);
    }

    public Connection getConnection() throws SQLException {
        return getDelegate().getConnection();
    }

    public Connection getConnection(String userName, String password) throws SQLException {
        return getDelegate().getConnection(userName, password);
    }

    public PrintWriter getLogWriter() throws SQLException {
        return getDelegate().getLogWriter();
    }

    public int getLoginTimeout() throws SQLException {
        return getDelegate().getLoginTimeout();
    }

    public void setLogWriter(PrintWriter writer) throws SQLException {
        getDelegate().setLogWriter(writer);
    }

    public void setLoginTimeout(int timeout) throws SQLException {
        getDelegate().setLoginTimeout(timeout);
    }

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		//return getDelegate().isWrapperFor(iface);
		throw new UnsupportedOperationException("isWrapperFor not supported");
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		//return unwrap(iface);
		throw new UnsupportedOperationException("unwrap not supported");
	}

}
