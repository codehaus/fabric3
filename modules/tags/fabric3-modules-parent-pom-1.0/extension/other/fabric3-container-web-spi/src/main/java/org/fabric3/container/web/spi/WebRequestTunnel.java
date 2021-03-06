/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.container.web.spi;

import javax.servlet.http.HttpServletRequest;

/**
 * Associates the current web request with the thread processing it so that contexts such as the HTTP session can be accessed by the web component
 * container .
 *
 * @version $Revision$ $Date$
 */
public final class WebRequestTunnel {
    private static final ThreadLocal<HttpServletRequest> REQUEST = new ThreadLocal<HttpServletRequest>();

    private WebRequestTunnel() {
    }

    public static void setRequest(HttpServletRequest request) {
        REQUEST.set(request);
    }

    public static HttpServletRequest getRequest() {
        return REQUEST.get();
    }

}
