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
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.model.type.binding.SCABinding;

/**
 * Super class for logical services and references.
 *
 * @version $Rev$ $Date$
 */
public abstract class Bindable extends LogicalAttachPoint {
    private static final long serialVersionUID = 570403036597601956L;
    private List<LogicalBinding<?>> bindings;
    private List<LogicalBinding<?>> callbackBindings;
    private ServiceContract serviceContract;

    /**
     * Initializes the URI and parent for the service or the reference.
     *
     * @param uri      URI of the service or the reference.
     * @param contract the service contract
     * @param parent   Parent of the service or the reference.
     */
    protected Bindable(URI uri, ServiceContract contract, LogicalComponent<?> parent) {
        super(uri, contract, parent);
        bindings = new ArrayList<LogicalBinding<?>>();
        callbackBindings = new ArrayList<LogicalBinding<?>>();
        serviceContract = contract;
    }

    /**
     * Overrides all the current bindings for the service or reference.
     *
     * @param bindings New set of bindings.
     */
    public final void overrideBindings(List<LogicalBinding<?>> bindings) {
        this.bindings.clear();
        this.bindings.addAll(bindings);
    }

    /**
     * Overrides all the current callback bindings for the service or reference.
     *
     * @param bindings New set of bindings.
     */
    public final void overrideCallbackBindings(List<LogicalBinding<?>> bindings) {
        this.callbackBindings.clear();
        for (LogicalBinding<?> binding : bindings) {
            binding.setCallback(true);
        }
        this.callbackBindings.addAll(bindings);
    }

    /**
     * Returns all the bindings on the service or the reference.
     *
     * @return The bindings available on the service or the reference.
     */
    public final List<LogicalBinding<?>> getBindings() {
        return bindings;
    }

    /**
     * Returns true if this bindable has been configured with a concrete binding as opposed to using binding.sca.
     *
     * @return true if this bindable has been configured with a concrete binding as opposed to using binding.sca
     */
    public boolean isConcreteBound() {
        if (bindings.isEmpty()) {
            return false;
        }
        for (LogicalBinding<?> binding : bindings) {
            if (!(binding.getDefinition() instanceof SCABinding)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all the callback bindings on the service or the reference.
     *
     * @return The bindings available on the service or the reference.
     */
    public final List<LogicalBinding<?>> getCallbackBindings() {
        return callbackBindings;
    }

    /**
     * Adds a binding to the service or the reference.
     *
     * @param binding Binding to be added to the service or the reference.
     */
    public final void addBinding(LogicalBinding<?> binding) {
        bindings.add(binding);
    }

    /**
     * Adds a collection of bindings.
     *
     * @param bindings the bindings
     */
    public void addBindings(List<LogicalBinding<?>> bindings) {
        bindings.addAll(bindings);
    }

    /**
     * Adds a callback binding to the service or the reference.
     *
     * @param binding Binding to be added to the service or the reference.
     */
    public final void addCallbackBinding(LogicalBinding<?> binding) {
        binding.setCallback(true);
        callbackBindings.add(binding);
    }


    /**
     * The effective service contract for this bindable. The effective contract may be set through promotion.
     *
     * @return the effective service contract for this bindable
     */
    public ServiceContract getServiceContract() {
        return serviceContract;
    }

    /**
     * Sets the effective service contract for this bindable.
     *
     * @param serviceContract the contract
     */
    public void setServiceContract(ServiceContract serviceContract) {
        this.serviceContract = serviceContract;
        createOperations(serviceContract);
    }

}
