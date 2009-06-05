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
package org.fabric3.binding.net.runtime;

import java.util.List;

import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.wire.InvocationChain;

/**
 * Holder for Wires and required metadata for performing an invocation.
 *
 * @version $Revision$ $Date$
 */
public class WireHolder {
    private ParameterEncoder parameterEncoder;
    private String callbackUri;
    private List<InvocationChain> chains;

    /**
     * Constructor.
     *
     * @param chains           InvocationChains contained by the wire
     * @param parameterEncoder the ParameterEncoder for encoding and decoding parameters
     * @param callbackUri      the callback URI or null if the wire is unidirectional
     */
    public WireHolder(List<InvocationChain> chains, ParameterEncoder parameterEncoder, String callbackUri) {
        this.chains = chains;
        this.parameterEncoder = parameterEncoder;
        this.callbackUri = callbackUri;
    }

    public String getCallbackUri() {
        return callbackUri;
    }

    public ParameterEncoder getParameterEncoder() {
        return parameterEncoder;
    }

    public List<InvocationChain> getInvocationChains() {
        return chains;
    }
}
