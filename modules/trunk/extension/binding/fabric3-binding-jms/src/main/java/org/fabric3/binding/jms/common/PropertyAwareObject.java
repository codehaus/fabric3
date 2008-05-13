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
package org.fabric3.binding.jms.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.scdl.ModelObject;

public abstract class PropertyAwareObject extends ModelObject{
    /**
     * Properties.
     */
    private Map<String,String> properties = null;
    /**
     * @param Properties used to create the administered object.
     */
    public void setProperties( Map<String,String> properties) {
        ensurePropertiesNotNull();
        this.properties.putAll(properties);
    }

    /**
     * Add a Property
     * @param name Name of the property.
     * @param value Value of the property.
     */
    public void addProperty(String name, String value) {
        ensurePropertiesNotNull();
        properties.put(name, value);
    }

    private void ensurePropertiesNotNull() {
        if (properties == null) {
            properties = new HashMap<String, String>();
        }
    }

    /**
     * @return Properties used to create the administered object.
     */
    public  Map<String,String> getProperties() {
        if(this.properties!=null){
            return Collections.unmodifiableMap(properties);
        }else{
            return Collections.emptyMap();
        }
    }
}
