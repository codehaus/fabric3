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
package org.fabric3.binding.ws.axis2.common;

import javax.xml.namespace.QName;

/**
 * 
 * @version $Revision$ $Date$
 */
public interface Constant {
    public static final QName AXIS2_JAXWS_QNAME = new QName("urn:org.fabric3:binding:axis2", "jaxws");
    public static final String SOAP_ACTION = "soapAction";
    public static final String VALUE_TRUE = "true";
    
    public static final String CONFIG_ENABLE_MTOM = "enableMTOM";    
}
