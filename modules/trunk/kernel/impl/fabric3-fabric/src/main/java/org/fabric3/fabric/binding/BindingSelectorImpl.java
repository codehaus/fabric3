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
package org.fabric3.fabric.binding;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.binding.BindingProvider;
import org.fabric3.spi.binding.BindingSelectionException;
import org.fabric3.spi.binding.BindingSelectionStrategy;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Selects a binding provider by delegating to a BindingSelectionStrategy configured for the domain.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class BindingSelectorImpl implements BindingSelector {
    private Map<QName, BindingProvider> providers = new HashMap<QName, BindingProvider>();
    // the default binding strategy if none explicitly configured
    private BindingSelectionStrategy strategy = new DefaultBindingSelectionStrategy();

    /**
     * Lazily injects SCAServiceProviders as they become available from runtime extensions.
     *
     * @param providers the set of providers
     */
    @Reference(required = false)
    public void setProviders(Map<QName, BindingProvider> providers) {
        this.providers = providers;
    }

    @Reference(required = false)
    public void setStrategy(BindingSelectionStrategy strategy) {
        if (strategy == null) {
            // FABRICTHREE-288 workaround
            return;
        }
        this.strategy = strategy;
    }

    public void selectBinding(LogicalReference source, LogicalService target) throws BindingSelectionException {
        Map<QName, BindingProvider> requiredMatches = new HashMap<QName, BindingProvider>();
        Map<QName, BindingProvider> allMatches = new HashMap<QName, BindingProvider>();

        for (Map.Entry<QName, BindingProvider> entry : providers.entrySet()) {
            BindingProvider.MatchType matchType = entry.getValue().canBind(source, target);
            switch (matchType) {
            case ALL_INTENTS:
                allMatches.put(entry.getKey(), entry.getValue());
                break;
            case REQUIRED_INTENTS:
                requiredMatches.put(entry.getKey(), entry.getValue());
                break;
            case NO_MATCH:
                break;
            }
        }
        if (!allMatches.isEmpty()) {
            strategy.select(allMatches).bind(source, target);
        } else if (!requiredMatches.isEmpty()) {
            strategy.select(requiredMatches).bind(source, target);
        } else {
            URI sourceUri = source.getUri();
            URI targetUri = target.getUri();
            throw new NoSCABindingProviderException("No SCA binding provider suitable for creating wire from " + sourceUri + " to " + targetUri);
        }
    }

}

