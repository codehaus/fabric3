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
package org.fabric3.resource.ds;

import java.util.Map;

import javax.sql.DataSource;

import org.fabric3.spi.resource.DataSourceRegistry;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class DataSourceRegistryImpl implements DataSourceRegistry {
    
    private Map<String, DataSource> dataSources;
    
    /**
     * @param dataSources Injected daatsources.
     */
    @Reference
    public void setDataSources(Map<String, DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    /**
     * @see org.fabric3.spi.resource.DataSourceRegistry#getDataSource(java.lang.String)
     */
    public DataSource getDataSource(String name) {
        return dataSources.get(name);
    }

}
