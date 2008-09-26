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

import java.util.Map;
import javax.xml.namespace.QName;

/**
 * Implementations select a BindingProvider based on some criteria such as a weighting algorithm.
 *
 * @version $Revision$ $Date$
 */
public interface BindingSelectionStrategy {

    /**
     * Chooses a binding provider from a collection of candidates.
     *
     * @param providers the candidate providers
     * @return the selected  provider
     */
    BindingProvider select(Map<QName, BindingProvider> providers);
}
