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
package org.fabric3.jaxb.introspection;

import java.lang.reflect.Method;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.fabric3.introspection.contract.OperationIntrospector;
import org.fabric3.jaxb.provision.JAXBConstants;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ValidationContext;

/**
 * Introspects operations for the presence of JAXB types. If a parameter is a JAXB type, the JAXB intent is added to the operation.
 *
 * @version $Revision$ $Date$
 */
public class JAXBTypeIntrospector implements OperationIntrospector {

    public <T> void introspect(Operation<T> operation, Method method, ValidationContext context) {
        // TODO perform error checking, e.g. mixing of databindings
        DataType<List<DataType<T>>> inputType = operation.getInputType();
        for (DataType<?> type : inputType.getLogical()) {
            if (isJAXB(type)) {
                operation.addIntent(JAXBConstants.DATABINDING_INTENT);
                return;
            }
        }
    }

    private boolean isJAXB(DataType<?> dataType) {
        if (dataType.getLogical() instanceof Class) {
            Class clazz = (Class) dataType.getLogical();
            if (clazz.isAnnotationPresent(XmlRootElement.class) || JAXBElement.class.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

}
