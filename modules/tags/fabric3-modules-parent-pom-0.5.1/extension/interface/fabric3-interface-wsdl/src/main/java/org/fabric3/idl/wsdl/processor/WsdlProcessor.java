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

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchemaType;
import org.fabric3.scdl.Operation;

/**
 * Abstraction for processing WSDL.
 *
 * @version $Revison$ $Date$
 */
public interface WsdlProcessor {

    /**
     * Get the list of operations from a WSDL 1.1 port type or WSDL 2.0 interface.
     * 
     * @param portTypeOrInterfaceName Qualified name of the WSDL 1.1 port type or WSDL 2.0 interface.
     * @param wsdlUrl The URL to the WSDL.
     * @return List of operations.
     */
    List<Operation<XmlSchemaType>> getOperations(QName portTypeOrInterfaceName, URL wsdlUrl);

}
