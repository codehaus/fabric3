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

package org.fabric3.idl.wsdl.processor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.idl.wsdl.WsdlContract;
import org.fabric3.idl.wsdl.version.WsdlVersionChecker.WsdlVersion;

/**
 * Default WSDL processor implementation.
 * 
 * @version $Revsion$ $Date$
 */
public class DefaultWsdlProcessor implements WsdlProcessor {
    
    /**
     * WSDL processors.
     */
    private Map<WsdlVersion, WsdlProcessor> wsdlProcessors = new HashMap<WsdlVersion, WsdlProcessor>();

    /**
     * @see org.fabric3.idl.wsdl.processor.WsdlProcessor#processWsdl(org.fabric3.idl.wsdl.WsdlContract, java.io.InputStream)
     */
    public void processWsdl(WsdlContract wsdlContract, InputStream wsdl) {

        WsdlVersion wsdlVersion = wsdlContract.getWsdlVersion();
        if(!wsdlProcessors.containsKey(wsdlVersion)) {
            throw new WsdlProcessorException("No processor registered for version " + wsdlVersion);
        }
        wsdlProcessors.get(wsdlVersion).processWsdl(wsdlContract, wsdl);
        
    }
    
    /**
     * Registers a processor.
     * 
     * @param wsdlVersion WSDL version.
     * @param processor WSDL processor.
     */
    public void registerProcessor(WsdlVersion wsdlVersion, WsdlProcessor processor) {
        wsdlProcessors.put(wsdlVersion, processor);
    }

}
