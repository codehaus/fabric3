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
package org.fabric3.spi.model.instance;

import java.net.URI;

import javax.xml.namespace.QName;

import org.fabric3.scdl.ServiceDefinition;

import org.osoa.sca.Constants;

/**
 * Represents a resolved service
 *
 * @version $Rev$ $Date$
 */
public class LogicalService extends Bindable {
    
    private static final QName TYPE = new QName(Constants.SCA_NS, "service");
    
    private final ServiceDefinition definition;
    private URI targetUri;

    /**
     * @param uri
     * @param definition
     * @param parent
     */
    public LogicalService(URI uri, ServiceDefinition definition, LogicalComponent<?> parent) {
        super(uri, parent, TYPE);
        this.definition = definition;
    }

    /**
     * @return
     */
    public ServiceDefinition getDefinition() {
        return definition;
    }

    /**
     * @return
     */
    public URI getTargetUri() {
        return targetUri;
    }

    /**
     * @param targetUri
     */
    public void setTargetUri(URI targetUri) {
        this.targetUri = targetUri;
    }
    
}
