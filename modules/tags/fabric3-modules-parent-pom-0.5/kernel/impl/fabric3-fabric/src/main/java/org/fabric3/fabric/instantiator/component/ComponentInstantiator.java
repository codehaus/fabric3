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
package org.fabric3.fabric.instantiator.component;

import java.util.Map;

import org.w3c.dom.Document;

import org.fabric3.fabric.instantiator.LogicalInstantiationException;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Implementation;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * @version $Revision$ $Date$
 */
public interface ComponentInstantiator {

    /**
     * Instantiates a logical component from a component definition
     *
     * @param parent     the parent logical component
     * @param properties the collection of properties associated with the component
     * @param definition the component definition to instantiate from @return the instantiated logical component
     * @return an instantiated logical component
     * @throws org.fabric3.fabric.instantiator.LogicalInstantiationException
     *          if an error occurs during instantiation
     */
    <I extends Implementation<?>> LogicalComponent<I> instantiate(LogicalCompositeComponent parent,
                                                                  Map<String, Document> properties,
                                                                  ComponentDefinition<I> definition) throws LogicalInstantiationException;

}