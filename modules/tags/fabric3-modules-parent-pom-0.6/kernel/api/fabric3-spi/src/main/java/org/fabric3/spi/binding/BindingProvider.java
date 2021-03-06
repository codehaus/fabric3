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
package org.fabric3.spi.binding;

import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Implementations are responsible for configuring a binding for a reference targeted to a remote service, that is, one hosted on a different runtime.
 * Binding configuration must be performed in two situations: when the reference targets a service with an explicit binding; and when a service
 * binding is not declared.
 * <p/>
 * In the first case, the binding provider will construct a binding configuration for the reference side of the wire based on the explicitly declared
 * service binding information.
 * <p/>
 * In the second case, when no binding is specified, the reference is said to be wired to a service. In SCA, inter-VM wires use the binding.sca. This
 * binding is abstract. In other words, it represents a remote protocol the particular runtime implementation chooses to effect communication. Fabric3
 * implements binding.sca by delegating to a binding provider, which is responsible for configuring binding information for both sides (reference and
 * serivce) of a wire.
 * <p/>
 * For a given wire, a variety of transport protocols may potentially be used. Which provider is selected depends on the algorithm inforce in a
 * particular domain. For example, a domain may use a weighted algorithm where a particular provider is preferred.
 *
 * @version $Revision$ $Date$
 */
public interface BindingProvider {
    enum MatchType {
        NO_MATCH,
        REQUIRED_INTENTS,
        ALL_INTENTS
    }

    /**
     * Determines if this binding provider can be used as a remote transport for the wire from the source reference to the target service.
     * Implementations must take into account required intents.
     *
     * @param source the source reference
     * @param target the target service
     * @return if the binding provider can wire from the source to target. {@link MatchType#NO_MATCH} indicates the binding provider cannot be used
     *         for the wire; {@link MatchType#REQUIRED_INTENTS} if the provider can be used but not all the mayProvides intents will be supported; and
     *         {@link MatchType#ALL_INTENTS} if all required and mayProvides intents are supported by the provider.
     */
    MatchType canBind(LogicalReference source, LogicalService target);

    /**
     * Configures binding information for the source reference and target service.
     *
     * @param source the source reference
     * @param target the target service
     * @throws BindingSelectionException if some error is encountered that inhibits binding configuration from being generated
     */
    void bind(LogicalReference source, LogicalService target) throws BindingSelectionException;
}
