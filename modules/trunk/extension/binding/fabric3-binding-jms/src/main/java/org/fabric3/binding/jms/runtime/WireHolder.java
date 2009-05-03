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
package org.fabric3.binding.jms.runtime;

import java.util.List;

import org.fabric3.binding.jms.common.CorrelationScheme;
import org.fabric3.binding.jms.common.TransactionType;

/**
 * Holder for Wires and required metadata for performing an invocation.
 *
 * @version $Revision$ $Date$
 */
public class WireHolder {
    private List<InvocationChainHolder> chains;
    private String callbackUri;
    private CorrelationScheme correlationScheme;
    private TransactionType transactionType;

    /**
     * Constructor.
     *
     * @param chains            InvocationChains contained by the wire
     * @param callbackUri       the callback URI or null if the wire is unidirectional
     * @param correlationScheme the correlation scheme if the wire uses request-response, otherwise null
     * @param transactionType   the transaction type if the wire uses request-response, otherwise null
     */
    public WireHolder(List<InvocationChainHolder> chains, String callbackUri, CorrelationScheme correlationScheme, TransactionType transactionType) {
        this.chains = chains;
        this.callbackUri = callbackUri;
        this.correlationScheme = correlationScheme;
        this.transactionType = transactionType;
    }

    public String getCallbackUri() {
        return callbackUri;
    }

    public CorrelationScheme getCorrelationScheme() {
        return correlationScheme;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public List<InvocationChainHolder> getInvocationChains() {
        return chains;
    }
}