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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

import junit.framework.TestCase;

import org.fabric3.jaxb.provision.JAXBConstants;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;

/**
 * @version $Revision$ $Date$
 */
public class JAXBTypeIntrospectorTestCase extends TestCase {
    private JAXBTypeIntrospector introspector;

    public void testJAXBIntrospection() throws Exception {
        Operation<Type> jaxbOperation = createOperation("jaxbMethod", Param.class);
        introspector.introspect(jaxbOperation, Contract.class.getMethod("jaxbMethod", Param.class), null);
        assertTrue(jaxbOperation.getIntents().contains(JAXBConstants.DATABINDING_INTENT));
    }

    public void testNoJAXBIntrospection() throws Exception {
        Operation<Type> nonJaxbOperation = createOperation("nonJaxbMethod", String.class);
        introspector.introspect(nonJaxbOperation, Contract.class.getMethod("nonJaxbMethod", String.class), null);
        assertFalse(nonJaxbOperation.getIntents().contains(JAXBConstants.DATABINDING_INTENT));
    }

    protected void setUp() throws Exception {
        super.setUp();
        introspector = new JAXBTypeIntrospector();
    }

    private Operation<Type> createOperation(String name, Class<?> paramType) {
        DataType<Type> type = new DataType<Type>(paramType, paramType);
        List<DataType<Type>> in = new ArrayList<DataType<Type>>();
        in.add(type);
        DataType<List<DataType<Type>>> inParams = new DataType<List<DataType<Type>>>(paramType, in);
        return new Operation<Type>(name, inParams, null, null);
    }

    private class Contract {
        public void jaxbMethod(Param param) {

        }

        public void nonJaxbMethod(String param) {

        }
    }

    @XmlRootElement
    private class Param {

    }

}
