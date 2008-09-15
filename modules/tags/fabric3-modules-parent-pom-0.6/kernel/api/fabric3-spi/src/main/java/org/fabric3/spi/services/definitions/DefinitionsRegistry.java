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
package org.fabric3.spi.services.definitions;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.fabric3.scdl.definitions.AbstractDefinition;

/**
 * Registry of binding types, implementation types, intents and policy sets 
 * within an SCA domain.
 * 
 * @version $Revision$ $Date$
 */
public interface DefinitionsRegistry {
    
    /**
     * Returns all the definitions of a given type.
     * 
     * @param <D> Definition type.
     * @param definitionClass Definition class.
     * @return All definitions of the given type.
     */
    <D extends AbstractDefinition> Collection<D> getAllDefinitions(Class<D> definitionClass);
    
    /**
     * Returns the definition of specified type and qualified name.
     * 
     * @param <D> Definition type.
     * @param name Qualified name of the definition object.
     * @param definitionClass Definition class.
     * @return Requested definition object if available, otherwise null.
     */
    <D extends AbstractDefinition> D getDefinition(QName name, Class<D> definitionClass);
    
    /**
     * Activates all the definitions in the specified contributions.
     * 
     * @param contributionUris The URIs for the contribution.
     * @throws DefinitionActivationException If unable to find definition.
     */
    void activateDefinitions(List<URI> contributionUris) throws DefinitionActivationException;

}
