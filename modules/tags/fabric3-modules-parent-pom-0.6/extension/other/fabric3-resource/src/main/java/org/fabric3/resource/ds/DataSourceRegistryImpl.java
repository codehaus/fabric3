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

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.fabric3.spi.resource.DataSourceRegistry;

/**
 * @version $Revision$ $Date$
 */
public class DataSourceRegistryImpl implements DataSourceRegistry {
    
    private Map<String, DataSource> dataSources = new HashMap<String, DataSource>();

    public DataSource getDataSource(String name) {
        return dataSources.get(name);
    }
    
    public void registerDataSource(String name, DataSource dataSource) {
        // TODO why not map of references?
        dataSources.put(name, dataSource);
    }

}
