/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.model.physical;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Represents an operation.
 *
 * @version $Revision$ $Date$
 */
public class PhysicalOperationDefinition implements Serializable {
    private static final long serialVersionUID = -4270990709748460450L;

    private List<String> parameterTypes = new LinkedList<String>();
    private List<String> faultTypes = new LinkedList<String>();
    private String returnType;
    private String name;
    private boolean callback;
    private boolean oneWay;
    private boolean endsConversation;
    private String databinding;

    // Interceptors defined against the operation
    private Set<PhysicalInterceptorDefinition> interceptors = new HashSet<PhysicalInterceptorDefinition>();

    /**
     * Returns the fully qualified parameter types for this operation.
     *
     * @return Parameter types.
     */
    public List<String> getParameters() {
        return parameterTypes;
    }

    /**
     * Add the fully qualified parameter type to the operation.
     *
     * @param parameter Parameter type to be added.
     */
    public void addParameter(String parameter) {
        parameterTypes.add(parameter);
    }

    /**
     * Gets the fuly qualified return type for this operation.
     *
     * @return Return type for this operation.
     */
    public String getReturnType() {
        return returnType;
    }

    /**
     * Sets the fully qualified return type for this operation.
     *
     * @param returnType Return type for this operation.
     */
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    /**
     * Returns the interceptor definitions available for this operation.
     *
     * @return Inteceptor definitions for this operation.
     */
    public Set<PhysicalInterceptorDefinition> getInterceptors() {
        return interceptors;
    }

    /**
     * Sets the interceptor definitions available for this operations.
     *
     * @param interceptors the interceptor definitions available for this operations
     */
    public void setInterceptors(Set<PhysicalInterceptorDefinition> interceptors) {
        this.interceptors = interceptors;
    }

    /**
     * Adds an interceptor definition to the operation.
     *
     * @param interceptor Interceptor definition to be added.
     */
    public void addInterceptor(PhysicalInterceptorDefinition interceptor) {
        interceptors.add(interceptor);
    }

    /**
     * Gets the name of the operation.
     *
     * @return Operation name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the operation.
     *
     * @param name Operation name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Checks whether the operation is a callback.
     *
     * @return True if this is a callback.
     */
    public boolean isCallback() {
        return callback;
    }

    /**
     * Sets whether this is a callback operation or not.
     *
     * @param callback True if this is a callback.
     */
    public void setCallback(boolean callback) {
        this.callback = callback;
    }

    /**
     * Returns true if the operation ends a conversation.
     *
     * @return true if the operation ends a conversation
     */
    public boolean isEndsConversation() {
        return endsConversation;
    }

    /**
     * Sets if the operation ends a conversation.
     *
     * @param endsConversation true if the operation ends a conversation
     */
    public void setEndsConversation(boolean endsConversation) {
        this.endsConversation = endsConversation;
    }

    /**
     * Returns true if the operation is non-blocking.
     *
     * @return true if the operation is non-blocking
     */
    public boolean isOneWay() {
        return oneWay;
    }

    /**
     * Sets if the operation is non-blocking.
     *
     * @param oneWay true if the operation is non-blocking
     */
    public void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }

    /**
     * Returns the fault types.
     *
     * @return the fault types
     */
    public List<String> getFaultTypes() {
        return faultTypes;
    }

    /**
     * Adds a fault type.
     *
     * @param name the type
     */
    public void addFaultType(String name) {
        faultTypes.add(name);
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
}
