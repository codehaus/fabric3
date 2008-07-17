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
package org.fabric3.jpa.hibernate;

import java.util.Collections;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import org.fabric3.jpa.spi.delegate.EmfBuilderDelegate;
import org.fabric3.jpa.spi.EmfBuilderException;
import org.fabric3.spi.resource.DataSourceRegistry;

import org.hibernate.ejb.Ejb3Configuration;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class HibernateDelegate implements EmfBuilderDelegate {

    private DataSourceRegistry dataSourceRegistry;

    @Reference
    public void setDataSourceRegistry(DataSourceRegistry dataSourceRegistry) {
        this.dataSourceRegistry = dataSourceRegistry;
    }

    public EntityManagerFactory build(PersistenceUnitInfo info, ClassLoader classLoader, String dataSourceName) throws EmfBuilderException {

        Ejb3Configuration cfg = new Ejb3Configuration();

        if (dataSourceName != null) {
            DataSource dataSource = dataSourceRegistry.getDataSource(dataSourceName);
            if (dataSource == null) {
                throw new DataSourceNotConfiguredException("Datasource not configured: " + dataSourceName);
            }
            cfg.setDataSource(dataSource);
        }
        cfg.configure(info, Collections.emptyMap());

        return cfg.buildEntityManagerFactory();
    }

}
