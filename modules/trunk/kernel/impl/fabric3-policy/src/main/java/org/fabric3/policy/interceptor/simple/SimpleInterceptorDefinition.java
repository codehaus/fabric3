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
package org.fabric3.policy.interceptor.simple;

import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;

/**
 * Interceptor definition that encapsulates the interceptor class.
 *
 * @version $Revision$ $Date$
 */
public class SimpleInterceptorDefinition extends PhysicalInterceptorDefinition {
    private static final long serialVersionUID = 880405443267716015L;
    private String interceptorClass;

    /**
     * Constructor.
     *
     * @param interceptorClass the interceptor class
     */
    public SimpleInterceptorDefinition(String interceptorClass) {
        this.interceptorClass = interceptorClass;
    }

    /**
     * Returns the interceptor class.
     *
     * @return the interceptor class
     */
    public String getInterceptorClass() {
        return interceptorClass;
    }

}
