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
package org.fabric3.mock;

import java.net.URI;
import java.util.Map;

import org.fabric3.scdl.PropertyValue;
import org.fabric3.spi.AbstractLifecycle;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.WorkContext;
import org.osoa.sca.ComponentContext;

/**
 * @version $Revision$ $Date$
 */
public class MockComponent<T> extends AbstractLifecycle implements AtomicComponent<T> {
    
    private final URI componentId;
    private final ObjectFactory<T> objectFactory;
    
    public MockComponent(URI componentId, ObjectFactory<T> objectFactory) {
        this.componentId = componentId;
        this.objectFactory = objectFactory;
    }

    public URI getUri() {
        return componentId;
    }

    @SuppressWarnings("unchecked")
    public ObjectFactory<T> createObjectFactory() {
        return objectFactory;
    }

    public InstanceWrapper<T> createInstanceWrapper(WorkContext workContext) throws ObjectCreationException {
        return null;
    }

    public URI getGroupId() {
        return null;
    }

    public int getInitLevel() {
        return 0;
    }

    public long getMaxAge() {
        return 0;
    }

    public long getMaxIdleTime() {
        return 0;
    }

    public boolean isEagerInit() {
        return false;
    }

    public ComponentContext getComponentContext() {
        return null;
    }

    public Map<String, PropertyValue> getDefaultPropertyValues() {
        return null;
    }

    public void setDefaultPropertyValues(Map<String, PropertyValue> propertyValues) {
    }

}
