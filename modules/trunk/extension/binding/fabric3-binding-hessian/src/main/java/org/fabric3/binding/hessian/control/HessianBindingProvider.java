/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
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
package org.fabric3.binding.hessian.control;

import java.net.URI;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.hessian.scdl.HessianBindingDefinition;
import org.fabric3.spi.binding.BindingProvider;
import org.fabric3.spi.binding.BindingSelectionException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.topology.DomainManager;

/**
 * Allows Hessian to be used for sca.binding in a domain.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class HessianBindingProvider implements BindingProvider {
    private static final QName BINDING_QNAME = new QName("http://www.fabric3.org/binding/hessian/0.2", "binding.hessian");
    private DomainManager domainManager;

    /**
     * Injects the domain manager. This reference is optional so the extension can be loaded on non-controller instances.
     *
     * @param domainManager the domain manager
     */
    @Reference(required = false)
    public void setDomainManager(DomainManager domainManager) {
        this.domainManager = domainManager;
    }

    public MatchType canBind(LogicalReference source, LogicalService target) {
        // TODO handle must provide intents
        return MatchType.REQUIRED_INTENTS;
    }

    public void bind(LogicalReference source, LogicalService target) throws BindingSelectionException {
        if (domainManager == null) {
            throw new BindingSelectionException("Domain manager not configured");
        }
        LogicalComponent<?> component = target.getParent();
        String zone = component.getZone();
        if (zone == null) {
            // programming error
            throw new AssertionError("Component not allocated: " + component.getUri());
        }
        String baseUrl = domainManager.getTransportMetaData(zone, String.class, "http");
        if (baseUrl == null) {
            throw new BindingSelectionException("HTTP information not found");
        }
        baseUrl = "http://" + baseUrl;
        // determing whether to configure both sides of the wire or just the reference
        if (target.getBindings().isEmpty()) {
            // configure both sides
            configureService(target);
            configureReference(source, target, baseUrl);
        } else {
            configureReference(source, target, baseUrl);
        }
    }

    private void configureReference(LogicalReference source, LogicalService target, String baseUrl) throws BindingSelectionException {
        LogicalBinding<HessianBindingDefinition> binding = null;
        for (LogicalBinding<?> entry : target.getBindings()) {
            if (entry.getBinding().getType().equals(BINDING_QNAME)) {
                //noinspection unchecked
                binding = (LogicalBinding<HessianBindingDefinition>) entry;
                break;
            }
        }
        if (binding == null) {
            throw new BindingSelectionException("Hessian binding on service not found: " + target.getUri());
        }
        URI targetUri = URI.create(baseUrl + binding.getBinding().getTargetUri().toString());
        constructLogicalReference(source, targetUri);
    }

    private void constructLogicalReference(LogicalReference source, URI targetUri) {
        HessianBindingDefinition referenceDefinition = new HessianBindingDefinition(targetUri, null);
        LogicalBinding<HessianBindingDefinition> referenceBinding = new LogicalBinding<HessianBindingDefinition>(referenceDefinition, source);
        source.addBinding(referenceBinding);
    }

    private void configureService(LogicalService target) {
        String endpointName = target.getUri().getPath() + "/" + target.getUri().getFragment();
        URI endpointUri = URI.create(endpointName);
        HessianBindingDefinition serviceDefinition = new HessianBindingDefinition(endpointUri, null);
        LogicalBinding<HessianBindingDefinition> serviceBinding = new LogicalBinding<HessianBindingDefinition>(serviceDefinition, target);
        target.addBinding(serviceBinding);
    }
}
