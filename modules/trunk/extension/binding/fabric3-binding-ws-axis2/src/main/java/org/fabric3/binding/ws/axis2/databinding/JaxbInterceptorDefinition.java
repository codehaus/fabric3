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
package org.fabric3.binding.ws.axis2.databinding;

import java.net.URI;
import java.util.List;

import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;

/**
 * @version $Revision$ $Date$
 */
public class JaxbInterceptorDefinition extends PhysicalInterceptorDefinition {
    
    private final URI classLoaderId;
    private final List<String> inClassNames;
    private final String outClassName;
    private final boolean service;
    
    public JaxbInterceptorDefinition(URI classLoaderId, List<String> inClassNames, String outClassName, boolean service) {
        this.classLoaderId = classLoaderId;
        this.inClassNames = inClassNames;
        this.outClassName = outClassName;
        this.service = service;
    }

    public URI getClassLoaderId() {
        return classLoaderId;
    }

    public List<String> getInClassNames() {
        return inClassNames;
    }

    public String getOutClassName() {
        return outClassName;
    }

    public boolean isService() {
        return service;
    }

}
