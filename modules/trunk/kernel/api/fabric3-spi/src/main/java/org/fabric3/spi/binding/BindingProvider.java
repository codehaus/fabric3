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
 */
package org.fabric3.spi.binding;

import javax.xml.namespace.QName;

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

    QName getType();

    /**
     * Determines if this binding provider can be used as a remote transport for the wire from the source reference to the target service.
     * Implementations must take into account required intents.
     *
     * @param source the source reference
     * @param target the target service
     * @return if the binding provider can wire from the source to target.
     */
    BindingMatchResult canBind(LogicalReference source, LogicalService target);

    /**
     * Configures binding information for the source reference and target service.
     *
     * @param source the source reference
     * @param target the target service
     * @throws BindingSelectionException if some error is encountered that inhibits binding configuration from being generated
     */
    void bind(LogicalReference source, LogicalService target) throws BindingSelectionException;
}
