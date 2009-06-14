  /*
   * Fabric3
   * Copyright (c) 2009 Metaform Systems
   *
   * Fabric3 is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as
   * published by the Free Software Foundation, either version 3 of
   * the License, or (at your option) any later version, with the
   * following exception:
   *
   * Linking this software statically or dynamically with other
   * modules is making a combined work based on this software.
   * Thus, the terms and conditions of the GNU General Public
   * License cover the whole combination.
   *
   * As a special exception, the copyright holders of this software
   * give you permission to link this software with independent
   * modules to produce an executable, regardless of the license
   * terms of these independent modules, and to copy and distribute
   * the resulting executable under terms of your choice, provided
   * that you also meet, for each linked independent module, the
   * terms and conditions of the license of that module. An
   * independent module is a module which is not derived from or
   * based on this software. If you modify this software, you may
   * extend this exception to your version of the software, but
   * you are not obligated to do so. If you do not wish to do so,
   * delete this exception statement from your version.
   *
   * Fabric3 is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty
   * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   * See the GNU General Public License for more details.
   *
   * You should have received a copy of the
   * GNU General Public License along with Fabric3.
   * If not, see <http://www.gnu.org/licenses/>.
   */
package org.fabric3.xapool;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.enhydra.jdbc.standard.StandardXADataSource;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.resource.DataSourceRegistry;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class XaPoolDataSource implements DataSource {

    private String user;
    private String password;
    private String url;
    private String driver;
    private List<String> dataSourceKeys = new ArrayList<String>();
    private int minSize = 10;
    private int maxSize = 10;

    private StandardXADataSource delegate;
    private TransactionManager transactionManager;
    private DataSourceRegistry dataSourceRegistry;
    private Map<Transaction, TransactedConnection> connectionCache = new ConcurrentHashMap<Transaction, TransactedConnection>();

    public Connection getConnection() throws SQLException {
		
        try {
            
            final Transaction transaction = transactionManager.getTransaction();
            
            if (transaction == null) {
                return delegate.getConnection();
            }
            
            TransactedConnection connection = connectionCache.get(transaction);
            if (connection == null) {
                final XAConnection xaConnection = delegate.getXAConnection();
                connection = new TransactedConnection(xaConnection);
                connectionCache.put(transaction, connection);
                transaction.registerSynchronization(new Synchronization() {
                    public void afterCompletion(int status) {
                        TransactedConnection connection = connectionCache.get(transaction);
                        connection.closeForReal();
                        connectionCache.remove(transaction);
                    }
                    public void beforeCompletion() {
                    }
                });
            }
            
            return connection;
            
        } catch (SystemException e) {
            throw new SQLException(e.getMessage());
        } catch (RollbackException e) {
            throw new SQLException(e.getMessage());
        }
        
    }

    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException();
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
    public void setDataSourceKeys(List<String> dataSourceKeys) {
        this.dataSourceKeys = dataSourceKeys;
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

    @Property
    public void setMinSize(int minSize) {
        this.minSize = minSize;
    }

    @Property
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
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
        delegate.setMinCon(minSize);
        delegate.setMaxCon(maxSize);

        for (String dataSourceKey : dataSourceKeys) {
            dataSourceRegistry.registerDataSource(dataSourceKey, this);
        }

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
