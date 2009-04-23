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
package org.fabric3.binding.net.generator;

import javax.xml.namespace.QName;

import org.osoa.sca.Constants;

import org.fabric3.spi.binding.BindingMatchResult;
import org.fabric3.spi.binding.BindingProvider;
import org.fabric3.spi.binding.BindingSelectionException;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * @version $Revision$ $Date$
 */
public class HttpBindingProvider implements BindingProvider {
    public static final QName HTTP_BINDING = new QName(Constants.SCA_NS, "binding.http");

    public QName getType() {
        return HTTP_BINDING;
    }

    public BindingMatchResult canBind(LogicalReference source, LogicalService target) {
        // disable for now
        return new BindingMatchResult(false, HTTP_BINDING);
    }

    public void bind(LogicalReference source, LogicalService target) throws BindingSelectionException {
        throw new UnsupportedOperationException();
    }


}
