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

import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.binding.jms.provision.MessageType;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;
import static org.fabric3.spi.Constants.FABRIC3_NS;

/**
 * Default implementation of the MessageTypeIntrospector. Message types are determined as follows:
 * <pre>
 * <ul>
 * <li>If the operation has a JAXB databinding intent, a text message is used
 * <li>If the parameters are Serializable, an object message is used
 * <li>If the parameters are primitives, a bytes message is used
 * <li>If the parameters are a stream, a stream message is used
 * <ul>
 * </pre>
 *
 * @version $Revision$ $Date$
 */
public class MessageTypeIntrospectorImpl implements MessageTypeIntrospector {
    private static final QName DATABINDING_INTENT = new QName(FABRIC3_NS, "dataBinding.jaxb");

    public <T> MessageType introspect(Operation<T> operation) throws JmsGenerationException {
        // TODO perform error checking, e.g. mixing of databindings
        if (operation.getIntents().contains(DATABINDING_INTENT)) {
            return MessageType.TEXT;
        }
        DataType<List<DataType<T>>> inputType = operation.getInputType();
        MessageType messageType = null;
        for (DataType<?> type : inputType.getLogical()) {
            MessageType val = introspect(type);
            if (messageType == null) {
                messageType = val;
            } else {
                if (messageType != val) {
                    throw new JmsGenerationException("Mixed parameter databinding types not supported on operation: " + operation.getName());
                }
            }
        }
        if (introspect(operation.getOutputType()) != messageType) {
            throw new JmsGenerationException("Mixed parameter and return databinding types not supported on operation: " + operation.getName());
        }
        return messageType;
    }

    private MessageType introspect(DataType<?> dataType) {
        // TODO finish
        return MessageType.OBJECT;
    }

}
