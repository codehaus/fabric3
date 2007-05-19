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
package org.fabric3.binding.jms.model.logical;

import org.fabric3.binding.jms.model.JmsBindingMetadata;
import org.fabric3.spi.model.type.BindingDefinition;

/**
 * Logical model object for JMS binding definition. TODO Support for overriding
 * request connection, response connection and operation properties from a
 * definition document as well as activation spec and resource adaptor.
 * 
 * @version $Revision$ $Date$
 */
public class JmsBindingDefinition extends BindingDefinition {

    /**
     * JMS binding metadata shared between logical and physical.
     */
    private JmsBindingMetadata metadata;

    /**
     * Default constructor.
     */
    public JmsBindingDefinition() {
    }

    /**
     * @param metadata Metadata to be initialized.
     */
    public JmsBindingDefinition(JmsBindingMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * @return the metadata
     */
    public JmsBindingMetadata getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(JmsBindingMetadata metadata) {
        this.metadata = metadata;
    }

}
