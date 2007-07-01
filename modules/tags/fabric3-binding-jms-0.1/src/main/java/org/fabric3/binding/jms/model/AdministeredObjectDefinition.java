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
package org.fabric3.binding.jms.model;

import org.fabric3.spi.model.type.ModelObject;

/**
 * JMS administered object definition.
 * 
 * @version $Revision$ $Date$
 */
public class AdministeredObjectDefinition extends ModelObject {

    /**
     * Destination type.
     */
    private DestinationType type;

    /**
     * Destination name (can be JNDI name or simple name)
     */
    private String name;

    /**
     * Destination creation.
     */
    private CreateOption create = CreateOption.ifnotexist;

    /**
     * @return the create
     */
    public CreateOption getCreate() {
        return create;
    }

    /**
     * @param create the create to set
     */
    public void setCreate(CreateOption create) {
        this.create = create;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the type
     */
    public DestinationType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(DestinationType type) {
        this.type = type;
    }

}
