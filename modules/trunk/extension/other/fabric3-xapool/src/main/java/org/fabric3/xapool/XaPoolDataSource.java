/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.xapool;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.enhydra.jdbc.standard.StandardXADataSource;
import org.fabric3.spi.resource.DataSourceRegistry;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class XaPoolDataSource implements DataSource {
    
    private String user;
    private String password;
    private String url;
    private String driver;
    private String dataSourceKey;
    
    private StandardXADataSource delegate;
    private TransactionManager transactionManager;
    private DataSourceRegistry dataSourceRegistry;

    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return delegate.getConnection(username, password);
    }

    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }
    
    @Property
    public void setDataSourceKey(String dataSourceKey) {
        this.dataSourceKey = dataSourceKey;
    }

    @Property
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Property
    public void setUser(String user) {
        this.user = user;
    }

    @Property
    public void setPassword(String password) {
        this.password = password;
    }

    @Property
    public void setUrl(String url) {
        this.url = url;
    }

    @Property
    public void setDriver(String driver) {
        this.driver = driver;
    }

    @Reference
    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Reference
    public void setDataSourceRegistry(DataSourceRegistry dataSourceRegistry) {
        this.dataSourceRegistry = dataSourceRegistry;
    }
    
    @Init
    public void start() throws SQLException {
        
        delegate = new StandardXADataSource();
        delegate.setTransactionManager(transactionManager);
        delegate.setUrl(url);
        delegate.setDriverName(driver);
        delegate.setPassword(password);
        delegate.setUser(user);
        
        dataSourceRegistry.registerDataSource(dataSourceKey, this);
        
    }

}
