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
package org.fabric3.binding.jms.control;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;
import static org.fabric3.spi.Constants.FABRIC3_NS;

/**
 * Default implementation of the PayloadTypeIntrospector. Message types are determined as follows:
 * <pre>
 * <ul>
 * <li>If the operation has a JAXB databinding intent, a text type is returned
 * <li>If the parameters are Serializable, an object message is returned
 * <li>If the parameters are primitives, the specific primitive type is returned
 * <li>If the parameters are a stream, a stream message is returned
 * <ul>
 * </pre>
 *
 * @version $Revision$ $Date$
 */
public class PayloadTypeIntrospectorImpl implements PayloadTypeIntrospector {
    private static final QName DATABINDING_INTENT = new QName(FABRIC3_NS, "dataBinding.jaxb");

    public <T> PayloadType introspect(Operation<T> operation) throws JmsGenerationException {
        // TODO perform error checking, e.g. mixing of databindings
        if (operation.getIntents().contains(DATABINDING_INTENT)) {
            return PayloadType.TEXT;
        }
        DataType<List<DataType<T>>> inputType = operation.getInputType();
        if (inputType.getLogical().size() == 1) {
            DataType<?> param = inputType.getLogical().get(0);
            Type physical = param.getPhysical();
            if (physical instanceof Class) {
                Class<?> clazz = (Class<?>) physical;
                if (clazz.isPrimitive()) {
                    return calculatePrimitivePayloadType(clazz);
                } else if (InputStream.class.isAssignableFrom(clazz)) {
                    return PayloadType.STREAM;
                } else if (Serializable.class.isAssignableFrom(clazz)) {
                    return PayloadType.OBJECT;
                }
            } else {
                throw new UnsupportedOperationException("Non-class types not supported: " + physical);
            }
        }
        // more than one parameter, use an object type message
        return PayloadType.OBJECT;
    }

    private PayloadType calculatePrimitivePayloadType(Class<?> clazz) throws JmsGenerationException {
        if (Short.TYPE.equals(clazz)) {
            return PayloadType.SHORT;
        } else if (Integer.TYPE.equals(clazz)) {
            return PayloadType.INTEGER;
        } else if (Double.TYPE.equals(clazz)) {
            return PayloadType.DOUBLE;
        } else if (Float.TYPE.equals(clazz)) {
            return PayloadType.FLOAT;
        } else if (Long.TYPE.equals(clazz)) {
            return PayloadType.LONG;
        } else if (Character.TYPE.equals(clazz)) {
            return PayloadType.CHARACTER;
        } else if (Boolean.TYPE.equals(clazz)) {
            return PayloadType.BOOLEAN;
        } else if (Byte.TYPE.equals(clazz)) {
            return PayloadType.BYTE;
        } else {
            throw new JmsGenerationException("Parameter type not supported: " + clazz);
        }

    }

}
