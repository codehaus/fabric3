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
package org.fabric3.spi.component;

import java.net.URI;
import java.util.Map;

import org.osoa.sca.ComponentContext;

import org.fabric3.spi.Lifecycle;
import org.fabric3.scdl.PropertyValue;

/**
 * The runtime instantiation of an SCA component
 *
 * @version $$Rev$$ $$Date$$
 */
public interface Component extends Lifecycle {

    /**
     * Returns the component URI.
     *
     * @return the component URI
     */
    URI getUri();

    /**
     * Returns the SCA ComponentContext for this component.
     *
     * @return the SCA ComponentContext for this component
     */
    ComponentContext getComponentContext();

    /**
     * Returns the default property values associated with the component.
     *
     * @return default property values associated with the component.
     */
    Map<String, PropertyValue> getDefaultPropertyValues();

    /**
     * Sets the default property values associated with the component.
     *
     * @param defaultPropertyValues Default property values associated with the component.
     */
    void setDefaultPropertyValues(Map<String, PropertyValue> defaultPropertyValues);


}
