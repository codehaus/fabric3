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
package org.fabric3.binding.ftp.control;

import org.fabric3.binding.ftp.provision.FtpWireSourceDefinition;
import org.fabric3.binding.ftp.provision.FtpWireTargetDefinition;
import org.fabric3.binding.ftp.scdl.FtpBindingDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;

/**
 *
 * @version $Revision$ $Date$
 * @param <HessianBindingDefinition>
 * @param <HessianWireSourceDefinition>
 */
public class FtpBindingGenerator implements BindingGenerator<FtpWireSourceDefinition, FtpWireTargetDefinition, FtpBindingDefinition> {

    public FtpWireSourceDefinition generateWireSource(LogicalBinding<FtpBindingDefinition> binding, 
                                                      Policy policy, 
                                                      ServiceDefinition serviceDefinition) throws GenerationException {
        return null;
    }

    public FtpWireTargetDefinition generateWireTarget(LogicalBinding<FtpBindingDefinition> binding, 
                                                      Policy policy,
                                                      ReferenceDefinition referenceDefinition) throws GenerationException {
        return null;
    }
    
}
