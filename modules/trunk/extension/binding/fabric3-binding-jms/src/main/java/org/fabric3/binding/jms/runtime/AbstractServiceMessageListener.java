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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for SourceMessageListener implementations.
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractServiceMessageListener implements ServiceMessageListener {
    protected WireHolder wireHolder;
    protected Map<String, InvocationChainHolder> invocationChainMap;
    protected InvocationChainHolder onMessageHolder;

    public AbstractServiceMessageListener(WireHolder wireHolder) {
        this.wireHolder = wireHolder;
        invocationChainMap = new HashMap<String, InvocationChainHolder>();
        for (InvocationChainHolder chainHolder : wireHolder.getInvocationChains()) {
            String name = chainHolder.getChain().getPhysicalOperation().getName();
            if ("onMessage".equals(name)) {
                onMessageHolder = chainHolder;
            }
            invocationChainMap.put(name, chainHolder);
        }
    }

    protected InvocationChainHolder getInvocationChainHolder(String opName) throws JmsBadMessageException {
        List<InvocationChainHolder> chainHolders = wireHolder.getInvocationChains();
        if (chainHolders.size() == 1) {
            return chainHolders.get(0);
        } else if (opName != null) {
            InvocationChainHolder chainHolder = invocationChainMap.get(opName);
            if (chainHolder == null) {
                throw new JmsBadMessageException("Unable to match operation on the service contract: " + opName);
            }
            return chainHolder;
        } else if (onMessageHolder != null) {
            return onMessageHolder;
        } else {
            throw new JmsBadMessageException("Unable to match operation on the service contract");
        }

    }


}
