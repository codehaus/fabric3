/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.resource.jndi.proxy.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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
    private List<String> dataSourceKeys;

    @Property
    public void setDataSourceKeys(List<String> dataSourceKeys) {
        this.dataSourceKeys = dataSourceKeys;
    }

    @Reference
    public void setDataSourceRegistry(DataSourceRegistry dataSourceRegistry) {
        this.dataSourceRegistry = dataSourceRegistry;
    }
    
    @Override
    @Init
    public void init() throws NamingException {
        super.init();
        for (String dataSourceKey : dataSourceKeys) {
            dataSourceRegistry.registerDataSource(dataSourceKey, this);
        }
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
