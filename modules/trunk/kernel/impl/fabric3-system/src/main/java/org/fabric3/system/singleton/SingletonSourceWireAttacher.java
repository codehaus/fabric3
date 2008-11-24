/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.system.singleton;

import java.net.URI;

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;

/**
 * A SourceWireAttacher used to reinject singleton components after the runtime bootsrap.
 *
 * @version $Rev: 4790 $ $Date: 2008-06-08 08:14:42 -0700 (Sun, 08 Jun 2008) $
 */
@EagerInit
public class SingletonSourceWireAttacher implements SourceWireAttacher<SingletonWireSourceDefinition> {
    private final ComponentManager manager;

    public SingletonSourceWireAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attachToSource(SingletonWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void detachFromSource(SingletonWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void detachObjectFactory(SingletonWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void attachObjectFactory(SingletonWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target)
            throws WiringException {
        URI sourceId = UriHelper.getDefragmentedName(source.getUri());
        SingletonComponent<?> sourceComponent = (SingletonComponent<?>) manager.getComponent(sourceId);
        InjectableAttribute attribute = source.getValueSource();
        // Add the object factory for the target to be reinjected.
        // The InjectableAttribute identifies the injection site (a field or method) on the singleton instance.
        sourceComponent.addObjectFactory(attribute, objectFactory);
    }
}