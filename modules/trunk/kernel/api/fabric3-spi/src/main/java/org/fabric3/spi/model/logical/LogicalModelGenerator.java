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
package org.fabric3.spi.model.logical;

import java.util.List;

import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.Implementation;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Interface that abstracts the concerns of instantiating and maintaining 
 * logical components within the assembly.
 * 
 * TODO Identify the contract required.
 * 
 * @version $Revision$ $Date$
 */
public interface LogicalModelGenerator {

    List<LogicalComponent<?>> include(LogicalComponent<CompositeImplementation> domain, Composite composite) throws ActivateException;
    
    <I extends Implementation<?>> LogicalComponent<I> instantiate(LogicalComponent<CompositeImplementation> parent,
            ComponentDefinition<I> definition) throws ActivateException;

}
