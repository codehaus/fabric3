/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.model.type.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.fabric3.model.type.AbstractPolicyAware;


/**
 * Represents an operation that is part of a service contract. The type paramter of this operation identifies the logical type system for all data
 * types.
 *
 * @version $Rev: 5481 $ $Date: 2008-09-26 02:36:30 -0700 (Fri, 26 Sep 2008) $
 */
public class Operation<T> extends AbstractPolicyAware {
    public static final int NO_CONVERSATION = -1;
    public static final int CONVERSATION_CONTINUE = 1;
    public static final int CONVERSATION_END = 2;

    private static final long serialVersionUID = 5279880534105654066L;
    private final String name;
    private DataType<T> outputType;
    private DataType<List<DataType<T>>> inputType;
    private List<DataType<T>> faultTypes;
    private int conversationSequence = NO_CONVERSATION;
    private String databinding;
    private Map<QName, Map<String, String>> info;

    /**
     * Construct a minimally-specified operation
     *
     * @param name       the name of the operation
     * @param inputType  the data types of parameters passed to the operation
     * @param outputType the data type returned by the operation
     * @param faultTypes the data type of faults raised by the operation
     */
    public Operation(String name,
                     DataType<List<DataType<T>>> inputType,
                     DataType<T> outputType,
                     List<DataType<T>> faultTypes) {
        this(name, inputType, outputType, faultTypes, NO_CONVERSATION);
    }

    /**
     * Construct an operation
     *
     * @param name       the name of the operation
     * @param inputType  the data types of parameters passed to the operation
     * @param outputType the data type returned by the operation
     * @param faultTypes the data type of faults raised by the operation
     * @param sequence   the conversational attributes of the operation, {@link #NO_CONVERSATION}, {@link #CONVERSATION_CONTINUE}, {@link
     *                   #CONVERSATION_CONTINUE}
     */
    public Operation(final String name,
                     final DataType<List<DataType<T>>> inputType,
                     final DataType<T> outputType,
                     final List<DataType<T>> faultTypes,
                     int sequence) {
        super();
        this.name = name;
        List<DataType<T>> types = Collections.emptyList();
        this.inputType = inputType;
        this.outputType = outputType;
        this.faultTypes = (faultTypes == null) ? types : faultTypes;
        this.conversationSequence = sequence;
    }

    /**
     * Returns the name of the operation.
     *
     * @return the name of the operation
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the data type returned by the operation.
     *
     * @return the data type returned by the operation
     */
    public DataType<T> getOutputType() {
        return outputType;
    }

    /**
     * Returns the data types of the parameters passed to the operation.
     * <p/>
     * The inputType's logical type is a list of DataTypes which describes the parameter types
     *
     * @return the data types of the parameters passed to the operation
     */
    public DataType<List<DataType<T>>> getInputType() {
        return inputType;
    }

    /**
     * Returns the data types of the faults raised by the operation.
     *
     * @return the data types of the faults raised by the operation
     */
    public List<DataType<T>> getFaultTypes() {
        if (faultTypes == null) {
            return Collections.emptyList();
        }
        return faultTypes;
    }

    /**
     * Returns the sequence the operation is called in a conversation
     *
     * @return the sequence the operation is called in a conversation
     */
    public int getConversationSequence() {
        return conversationSequence;
    }

    /**
     * Sets the sequence the operation is called in a conversation
     *
     * @param conversationSequence true the sequence the operation is called in a conversation
     */
    public void setConversationSequence(int conversationSequence) {
        this.conversationSequence = conversationSequence;
    }

    /**
     * Add additional info related to Operation
     *
     * @param qName QName info need to be keyed on.
     * @param key   Name of the parameter
     * @param value Value of the parameter
     */
    public void addInfo(QName qName, String key, String value) {
        if (info == null) {//Lazy loading
            info = new HashMap<QName, Map<String, String>>();
            info.put(qName, new HashMap<String, String>());
        }

        info.get(qName).put(key, value);
    }

    /**
     * Retrieve info set on the Operation
     *
     * @param qName QName as key to retrieve the info
     * @return Map containing key-value info
     */
    public Map<String, String> getInfo(QName qName) {
        if (info != null) {
            return info.get(qName);
        }
        return null;
    }

    /**
     * Returns the required databinding type or null if none is specified.
     *
     * @return the required databinding type or null if none is specified
     */
    public String getDatabinding() {
        return databinding;
    }

    /**
     * Sets the required databinding for the operation.
     *
     * @param databinding the databinding
     */
    public void setDatabinding(String databinding) {
        this.databinding = databinding;
    }

    public String toString() {
        return new StringBuilder().append("Operation [").append(name).append("]").toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Operation operation = (Operation) o;

        if (name != null ? !name.equals(operation.name) : operation.name != null) {
            return false;
        }

        if (faultTypes == null && operation.faultTypes != null) {
            return false;
        } else if (faultTypes != null
                && operation.faultTypes != null
                && faultTypes.size() != 0
                && operation.faultTypes.size() != 0) {
            if (faultTypes.size() < operation.faultTypes.size()) {
                return false;
            } else {
                //noinspection ForLoopReplaceableByForEach
                for (int i = 0; i < operation.faultTypes.size(); i++) {
                    if (!faultTypes.get(i).equals(operation.faultTypes.get(i))) {
                        return false;
                    }
                }
            }
        }

        //noinspection SimplifiableIfStatement
        if (inputType != null ? !inputType.equals(operation.inputType) : operation.inputType != null) {
            return false;
        }
        return !(outputType != null ? !outputType.equals(operation.outputType) : operation.outputType != null);
    }

    public int hashCode() {
        int result;
        result = name != null ? name.hashCode() : 0;

        result = 29 * result + (outputType != null ? outputType.hashCode() : 0);
        result = 29 * result + (inputType != null ? inputType.hashCode() : 0);
        result = 29 * result + (faultTypes != null ? faultTypes.hashCode() : 0);
        return result;
    }

}
