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
package org.fabric3.scdl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Represents an operation that is part of a service contract. The type paramter of this operation identifies the
 * logical type system for all data types.
 *
 * @version $Rev$ $Date$
 */
public class Operation<T> extends AbstractPolicyAware {
    public static final int NO_CONVERSATION = -1;
    public static final int CONVERSATION_CONTINUE = 1;
    public static final int CONVERSATION_END = 2;

    private final String name;
    private DataType<T> outputType;
    private DataType<List<DataType<T>>> inputType;
    private List<DataType<T>> faultTypes;
    private String dataBinding;
    private boolean wrapperStyle;
    private boolean callback;
    private boolean nonBlocking;
    private int conversationSequence = NO_CONVERSATION;
    private Map<String, Object> metaData;

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
        this(name, inputType, outputType, faultTypes, false, false, null, NO_CONVERSATION);
    }

    /**
     * Construct an operation
     *
     * @param name        the name of the operation
     * @param inputType   the data types of parameters passed to the operation
     * @param outputType  the data type returned by the operation
     * @param faultTypes  the data type of faults raised by the operation
     * @param nonBlocking if the operation is non-blocking
     * @param callback    if the operation is a callback operation
     * @param dataBinding the data-binding type required by the operation
     * @param sequence    the conversational attributes of the operation, {@link #NO_CONVERSATION}, {@link
     *                    #CONVERSATION_CONTINUE}, {@link #CONVERSATION_CONTINUE}
     */
    public Operation(final String name,
                     final DataType<List<DataType<T>>> inputType,
                     final DataType<T> outputType,
                     final List<DataType<T>> faultTypes,
                     boolean nonBlocking,
                     boolean callback,
                     String dataBinding,
                     int sequence) {
        super();
        this.name = name;
        List<DataType<T>> types = Collections.emptyList();
        this.inputType = inputType;
        this.outputType = outputType;
        this.faultTypes = (faultTypes == null) ? types : faultTypes;
        this.nonBlocking = nonBlocking;
        this.callback = callback;
        this.dataBinding = dataBinding;
        this.conversationSequence = sequence;
    }

    /**
     * Returns true if the operation is part of the callback contract.
     *
     * @return true if the operation is part of the callback contract.
     */
    public boolean isCallback() {
        return callback;
    }

    /**
     * Sets whether the operation is part of the callback contract.
     *
     * @param callback whether the operation is part of the callback contract.
     */
    public void setCallback(boolean callback) {
        this.callback = callback;
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
     * Returns true if the operation is non-blocking. A non-blocking operation may not have completed execution at the
     * time an invocation of the operation returns.
     *
     * @return true if the operation is non-blocking
     */
    public boolean isNonBlocking() {
        return nonBlocking;
    }

    /**
     * Sets if the operation is non-blocking
     *
     * @param nonBlocking true f the operation is non-blocking
     */
    public void setNonBlocking(boolean nonBlocking) {
        this.nonBlocking = nonBlocking;
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
     * Returns true if the operation is wrapper style
     *
     * @return the wrapperStyle
     */
    public boolean isWrapperStyle() {
        return wrapperStyle;
    }

    /**
     * @param wrapperStyle the wrapperStyle to set
     */
    public void setWrapperStyle(boolean wrapperStyle) {
        this.wrapperStyle = wrapperStyle;
    }

    /**
     * Returns the data binding type specified for the operation or null.
     *
     * @return the data binding type specified for the operation or null.
     */
    public String getDataBinding() {
        return dataBinding;
    }

    /**
     * Set the databinding for this operation
     *
     * @param dataBinding The databinding
     */
    public void setDataBinding(String dataBinding) {
        this.dataBinding = dataBinding;
    }

    /**
     * Returns a map of metadata key to value mappings for the operation.
     *
     * @return a map of metadata key to value mappings for the operation.
     */
    public Map<String, Object> getMetaData() {
        if (metaData == null) {
            return Collections.emptyMap();
        }
        return metaData;
    }

    /**
     * Adds metadata associated with the operation.
     *
     * @param key the metadata key
     * @param val the metadata value
     */
    public void setMetaData(String key, Object val) {
        if (metaData == null) {
            metaData = new HashMap<String, Object>();
        }
        metaData.put(key, val);
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
