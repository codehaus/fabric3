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

import org.fabric3.idl.wsdl.WsdlContract;
import org.fabric3.idl.wsdl.version.WsdlVersionChecker.WsdlVersion;

/**
 * WSDL 1.1 processor implementation.
 * 
 * @version $Revsion$ $Date$
 */
public class Wsdl11Processor implements WsdlProcessor {
    
    /**
     * @param wsdlProcessorRegistry Injected default processor.
     */
    public Wsdl11Processor(WsdlProcessorRegistry wsdlProcessorRegistry) {
        wsdlProcessorRegistry.registerProcessor(WsdlVersion.VERSION_1_1, this);
    }

    /**
     * @see org.fabric3.idl.wsdl.processor.WsdlProcessor#processWsdl(org.fabric3.idl.wsdl.WsdlContract, java.io.InputStream)
     */
    public void processWsdl(WsdlContract wsdlContract, InputStream wsdl) {
        // Implement using wsdl4j
        throw new UnsupportedOperationException("Not supported yet");
    }

}
