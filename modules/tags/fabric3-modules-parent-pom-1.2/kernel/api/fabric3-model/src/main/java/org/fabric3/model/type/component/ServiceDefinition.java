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
package org.fabric3.model.type.component;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.model.type.AbstractPolicyAware;
import org.fabric3.model.type.service.OperationDefinition;
import org.fabric3.model.type.service.ServiceContract;

/**
 * Represents a service offered by a component.
 *
 * @version $Rev$ $Date$
 */
public class ServiceDefinition extends AbstractPolicyAware {
    private static final long serialVersionUID = -3331868180749278028L;
    private String name;
    private ServiceContract<?> serviceContract;
    private boolean management;
    private final List<BindingDefinition> bindings = new ArrayList<BindingDefinition>();
    private final List<BindingDefinition> callbackBindings = new ArrayList<BindingDefinition>();
    private final List<OperationDefinition> operations = new ArrayList<OperationDefinition>();

    public ServiceDefinition(String name) {
        this(name, null);
    }

    public ServiceDefinition(String name, ServiceContract<?> serviceContract) {
        this.name = name;
        this.serviceContract = serviceContract;
    }

    /**
     * Return the name of this service definition.
     *
     * @return the name of this service definition
     */
    public String getName() {
        return name;
    }

    /**
     * Returns true if this is a management service.
     * <p/>
     * A management service is intended for administration of a component rather than normal interactions with it. As such, it is not considered a
     * valid target for wiring (including autowire).
     *
     * @return true if this is a management service
     */
    public boolean isManagement() {
        return management;
    }

    /**
     * Sets whether this is a management service
     *
     * @param management true if this is a management service
     */
    public void setManagement(boolean management) {
        this.management = management;
    }

    /**
     * Returns the service contract
     *
     * @return the service contract
     */
    public ServiceContract<?> getServiceContract() {
        return serviceContract;
    }

    /**
     * Sets the service contract
     *
     * @param contract the service contract
     */
    public void setServiceContract(ServiceContract<?> contract) {
        this.serviceContract = contract;
    }

    /**
     * Returns the bindings configured for the service
     *
     * @return the bindings configured for the service
     */
    public List<BindingDefinition> getBindings() {
        return bindings;
    }

    /**
     * Configures the service with a binding
     *
     * @param binding the binding
     */
    public void addBinding(BindingDefinition binding) {
        this.bindings.add(binding);
    }

    /**
     * @return List of callback bindings defined against the reference.
     */
    public List<BindingDefinition> getCallbackBindings() {
        return callbackBindings;
    }

    /**
     * @param binding callback binding to be added.
     */
    public void addCallbackBinding(BindingDefinition binding) {
        this.callbackBindings.add(binding);
    }


    /**
     * @return Get the list of operations defined against the reference.
     */
    public List<OperationDefinition> getOperations() {
        return operations;
    }

    /**
     * @param operation Operation definition to be added.
     */
    public void addOperation(OperationDefinition operation) {
        operations.add(operation);
    }

}
