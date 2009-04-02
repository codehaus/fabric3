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
package org.fabric3.policy.xpath;

import java.util.Collections;
import java.util.List;

import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Implements the URIRef function defined by the SCA Policy Specificaton.
 *
 * @version $Revision$ $Date$
 */
public class UriRefFunction implements Function {

    @SuppressWarnings({"unchecked"})
    public Object call(Context context, List args) throws FunctionCallException {
        if (args.size() != 1) {
            throw new FunctionCallException("Invalid number of arguments for URIRef(): " + args.size());
        }
        String uri = args.get(0).toString();
        List<LogicalComponent<?>> nodeSet = context.getNodeSet();
        for (LogicalComponent<?> component : nodeSet) {
            if (component.getUri().getSchemeSpecificPart().equals(uri)) {
                return component;
            } else if (component instanceof LogicalCompositeComponent) {
                LogicalComponent ret = find(uri, (LogicalCompositeComponent) component);
                if (ret != null) {
                    return ret;
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Recurses the composite hierarchy for the component with the given URI
     *
     * @param uri       the uri
     * @param composite the composite to recurse
     * @return the component or null if not found
     */
    private LogicalComponent find(String uri, LogicalCompositeComponent composite) {
        for (LogicalComponent child : composite.getComponents()) {
            if (child.getUri().getSchemeSpecificPart().equals(uri)) {
                return child;
            }
            if (child instanceof LogicalCompositeComponent) {
                LogicalComponent component = find(uri, (LogicalCompositeComponent) child);
                if (component != null) {
                    return component;
                }
            }
        }
        return null;

    }
}
