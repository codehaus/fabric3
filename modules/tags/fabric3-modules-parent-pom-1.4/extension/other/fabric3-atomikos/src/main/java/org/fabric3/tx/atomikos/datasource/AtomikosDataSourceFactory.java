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

package org.fabric3.tx.atomikos.datasource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamReader;

import com.atomikos.jdbc.AbstractDataSourceBean;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.atomikos.jdbc.nonxa.AtomikosNonXADataSourceBean;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.resource.DataSourceConfiguration;
import org.fabric3.spi.resource.DataSourceFactory;
import org.fabric3.spi.resource.DataSourceRegistry;
import org.fabric3.spi.resource.DataSourceType;

/**
 * Initializes configured data sources and provides facilities for creating datasources dynamically.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class AtomikosDataSourceFactory implements DataSourceFactory {
    private List<DataSourceConfiguration> configurations = Collections.emptyList();
    private Map<String, AbstractDataSourceBean> beans;
    private DataSourceRegistry registry;
    private DataSourceConfigParser parser = new DataSourceConfigParser();


    public AtomikosDataSourceFactory(@Reference DataSourceRegistry registry) {
        this.registry = registry;
    }

    @Property(required = false)
    public void setDataSources(XMLStreamReader reader) throws DataSourceParseException {
        configurations = parser.parse(reader);
    }

    @Init
    public void init() throws DuplicateDataSourceException {
        beans = new HashMap<String, AbstractDataSourceBean>();
        for (DataSourceConfiguration configuration : configurations) {
            create(configuration);
        }
    }

    @Destroy
    public void destroy() {
        for (Map.Entry<String, AbstractDataSourceBean> entry : beans.entrySet()) {
            registry.unregister(entry.getKey());
            entry.getValue().close();
        }
    }

    public void create(DataSourceConfiguration configuration) throws DuplicateDataSourceException {
        String name = configuration.getName();
        if (registry.getDataSource(name) != null) {
            throw new DuplicateDataSourceException("Datasource already registered with name: " + name);
        }
        if (DataSourceType.XA == configuration.getType()) {
            AtomikosDataSourceBean bean = new AtomikosDataSourceBean();
            bean.setUniqueResourceName(name);
            bean.setXaProperties(configuration.getProperties());
            bean.setXaDataSourceClassName(configuration.getDriverClass());
            // TODO set pool properties
            registerJMX(bean);
            beans.put(name, bean);
            registry.register(name, bean);
        } else {
            AtomikosNonXADataSourceBean bean = new AtomikosNonXADataSourceBean();
            bean.setUniqueResourceName(name);
            bean.setDriverClassName(configuration.getDriverClass());
            bean.setUrl(configuration.getUrl());
            bean.setUser(configuration.getUsername());
            bean.setPassword(configuration.getPassword());
            // TODO set pool properties
            registerJMX(bean);
            beans.put(name, bean);
            registry.register(name, bean);
        }
    }

    private void registerJMX(AtomikosDataSourceBean bean) {
        // TODO implement
    }

    private void registerJMX(AtomikosNonXADataSourceBean bean) {
        // TODO implement
    }

}
