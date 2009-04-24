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
package org.fabric3.hessian.provision;

import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;

/**
 * Base class of an interceptor definition for a wire that requires transformation of data types to and from Hessian.
 *
 * @version $Revision$ $Date$
 */
public class AbstractHessianInterceptorDefinition extends PhysicalInterceptorDefinition {
    private static final long serialVersionUID = -1123937584067507079L;
    private Encoding encoding;

    public AbstractHessianInterceptorDefinition(Encoding encoding) {
        this.encoding = encoding;
    }

    public Encoding getEncoding() {
        return encoding;
    }
}
