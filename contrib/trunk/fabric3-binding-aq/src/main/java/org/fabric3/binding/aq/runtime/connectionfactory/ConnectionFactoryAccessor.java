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

package org.fabric3.binding.aq.runtime.connectionfactory;

import javax.jms.ConnectionFactory;

import org.fabric3.binding.aq.common.AQBindingMetadata;

/**
 * The Connection Factory Accessor is used to retrieve a {@link ConnectionFactory}
 * @version $Revsion$ $Date: 2008-02-26 09:26:36 +0000 (Tue, 26 Feb 2008) $
 */
public interface ConnectionFactoryAccessor<CF extends ConnectionFactory> {
   
    
    /**
     * Gets Connection Factory      
     * @param metadata is used to get a {@link ConnectionFactory}
     * @return ConnectionFactory 
     */
    CF getConnectionFactory(AQBindingMetadata metadata);
    

}
