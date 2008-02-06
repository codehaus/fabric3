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

package org.fabric3.idl.wsdl;

import javax.xml.namespace.QName;

import org.fabric3.scdl.ServiceContract;

/**
 * WSDL Service contract.
 * 
 * @version $Revsion$ $Date$
 */
public class WsdlContract extends ServiceContract {
    
    /**
     * QName for the port type/interface.
     */
    private QName qname;
    
    /**
     * Callback qname.
     */
    private QName callbackQname;

    /**
     * @return QName for the port type/interface.
     */
    public QName getQname() {
        return qname;
    }

    /**
     * @param qname QName for the port type/interface.
     */
    public void setQname(QName qname) {
        this.qname = qname;
    }

    /**
     * @return Callback qname.
     */
    public QName getCallbackQname() {
        return callbackQname;
    }

    /**
     * @param callbackQname Callback qname.
     */
    public void setCallbackQname(QName callbackQname) {
        this.callbackQname = callbackQname;
    }

    public boolean isAssignableFrom(ServiceContract serviceContract) {
        throw new UnsupportedOperationException();
    }

    public String getQualifiedInterfaceName() {
        return qname.toString();
    }
}
