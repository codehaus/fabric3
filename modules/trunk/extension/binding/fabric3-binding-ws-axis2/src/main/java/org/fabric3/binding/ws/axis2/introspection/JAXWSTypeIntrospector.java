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
package org.fabric3.binding.ws.axis2.introspection;

import java.lang.reflect.Method;

import javax.jws.WebMethod;

import org.fabric3.binding.ws.axis2.common.Constant;
import org.fabric3.introspection.contract.OperationIntrospector;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ValidationContext;

/**
 * Introspects operations for the presence of JAX-WS annotations. JAX-WS annotations are used to configure the Axis2 engine.
 * 
 * @version $Revision$ $Date$
 */
public class JAXWSTypeIntrospector implements OperationIntrospector {

    public <T> void introspect(Operation<T> operation, Method method, ValidationContext context) {
        WebMethod webMethod = method.getAnnotation(WebMethod.class);
        if (webMethod != null) {
            String soapAction = webMethod.action();
            if (soapAction != null) {
                operation.addInfo(Constant.AXIS2_JAXWS_QNAME, Constant.SOAP_ACTION, soapAction);
            }
        }

    }
}
