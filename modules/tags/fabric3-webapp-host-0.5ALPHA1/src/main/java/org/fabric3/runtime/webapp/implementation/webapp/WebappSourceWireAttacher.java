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
package org.fabric3.runtime.webapp.implementation.webapp;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;

/**
 * Source WireAttacher for webapp components.
 *
 * @version $Rev: 955 $ $Date: 2007-08-31 23:10:21 +0100 (Fri, 31 Aug 2007) $
 */
@EagerInit
public class WebappSourceWireAttacher implements SourceWireAttacher<WebappWireSourceDefinition> {

    private ComponentManager manager;

    public WebappSourceWireAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attachToSource(WebappWireSourceDefinition sourceDefinition,
                               PhysicalWireTargetDefinition targetDefinition,
                               Wire wire) {
        URI sourceUri = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        String referenceName = sourceDefinition.getUri().getFragment();
        WebappComponent source = (WebappComponent) manager.getComponent(sourceUri);
        source.attachWire(referenceName, wire);
    }

    public void attachObjectFactory(WebappWireSourceDefinition sourceDefinition, ObjectFactory<?> objectFactory) throws WiringException {
        URI sourceUri = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        String referenceName = sourceDefinition.getUri().getFragment();
        WebappComponent source = (WebappComponent) manager.getComponent(sourceUri);
        source.attachWire(referenceName, objectFactory);
    }
}