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
 * Represents a component reference.
 *
 * @version $Rev$ $Date$
 */
public class ReferenceDefinition extends AbstractPolicyAware {
    private static final long serialVersionUID = 4641581818938572132L;
    private final String name;
    private ServiceContract<?> serviceContract;
    private Multiplicity multiplicity;
    private final List<BindingDefinition> bindings = new ArrayList<BindingDefinition>();
    private final List<BindingDefinition> callbackBindings = new ArrayList<BindingDefinition>();
    private final List<OperationDefinition> operations = new ArrayList<OperationDefinition>();

    /**
     * Constructor.
     *
     * @param name            the refeence name
     * @param serviceContract the service contract required by this reference
     */
    public ReferenceDefinition(String name, ServiceContract<?> serviceContract) {
        this(name, serviceContract, Multiplicity.ONE_ONE);
    }

    /**
     * Constructor.
     *
     * @param name            the refeence name
     * @param serviceContract the service contract required by this reference
     * @param multiplicity    the reference multiplicity
     */
    public ReferenceDefinition(String name, ServiceContract<?> serviceContract, Multiplicity multiplicity) {
        this.name = name;
        this.serviceContract = serviceContract;
        this.multiplicity = multiplicity;
    }

    /**
     * Returns the reference name.
     *
     * @return the reference name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the service contract required by this reference.
     *
     * @return the service contract required by this reference
     */
    public ServiceContract<?> getServiceContract() {
        return serviceContract;
    }

    /**
     * Sets the service contract required by this reference.
     *
     * @param serviceContract the service contract required by this reference
     */
    public void setServiceContract(ServiceContract<?> serviceContract) {
        this.serviceContract = serviceContract;
    }

    /**
     * Returns the reference multiplicity.
     *
     * @return the reference multiplicity
     */
    public Multiplicity getMultiplicity() {
        return multiplicity;
    }

    /**
     * Sets the reference multiplicity.
     *
     * @param multiplicity the reference multiplicity
     */
    public void setMultiplicity(Multiplicity multiplicity) {
        this.multiplicity = multiplicity;
    }

    /**
     * Returns true if the reference is required
     *
     * @return true if the reference is required
     */
    public boolean isRequired() {
        return multiplicity == Multiplicity.ONE_ONE || multiplicity == Multiplicity.ONE_N;
    }

    /**
     * @return List of bindings defined against the reference.
     */
    public List<BindingDefinition> getBindings() {
        return bindings;
    }

    /**
     * @param binding Binding to be added.
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
