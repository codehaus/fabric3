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
package org.fabric3.rs.runtime.rs;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * @version $Rev$ $Date$
 */
public class RsServlet extends ServletContainer {

    Fabric3ComponentProvider componentProvider;

    public RsServlet(Fabric3ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    @Override
    protected void initiate(ResourceConfig rc, WebApplication wa) {
        if (rc instanceof Fabric3ResourceConfig) {
            Fabric3ResourceConfig f3rc = (Fabric3ResourceConfig) rc;
            f3rc.setProvider(componentProvider);
            wa.initiate(rc, componentProvider);
        } else {
            wa.initiate(rc);
        }

    }
}
