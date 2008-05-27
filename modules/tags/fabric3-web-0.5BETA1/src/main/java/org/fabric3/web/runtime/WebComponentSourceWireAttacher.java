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
package org.fabric3.web.runtime;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;
import org.fabric3.web.provision.WebComponentWireSourceDefinition;

/**
 * Source WireAttacher for web components.
 *
 * @version $Rev: 955 $ $Date: 2007-08-31 23:10:21 +0100 (Fri, 31 Aug 2007) $
 */
@EagerInit
public class WebComponentSourceWireAttacher implements SourceWireAttacher<WebComponentWireSourceDefinition> {
    private ComponentManager manager;

    public WebComponentSourceWireAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attachToSource(WebComponentWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        URI sourceUri = UriHelper.getDefragmentedName(source.getUri());
        String referenceName = source.getUri().getFragment();
        InteractionType interactionType = source.getInteractionType();
        WebComponent component = (WebComponent) manager.getComponent(sourceUri);
        try {
            component.attachWire(referenceName, interactionType, wire);
        } catch (ObjectCreationException e) {
            throw new WiringException(e);
        }
    }

    public void detachFromSource(WebComponentWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        throw new AssertionError();
    }

    public void attachObjectFactory(WebComponentWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        URI sourceUri = UriHelper.getDefragmentedName(source.getUri());
        String referenceName = source.getUri().getFragment();
        WebComponent component = (WebComponent) manager.getComponent(sourceUri);
        try {
            component.attachWire(referenceName, objectFactory);
        } catch (ObjectCreationException e) {
            throw new WiringException(e);
        }
    }
}