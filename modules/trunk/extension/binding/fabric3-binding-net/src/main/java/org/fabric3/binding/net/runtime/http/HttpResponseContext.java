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
package org.fabric3.binding.net.runtime.http;

import org.jboss.netty.handler.codec.http.HttpResponse;

import org.fabric3.spi.binding.format.HeaderContext;

/**
 * HeaderContext for an HTTP response.
 *
 * @version $Revision$ $Date$
 */
public class HttpResponseContext implements HeaderContext {
    private HttpResponse response;

    public HttpResponseContext(HttpResponse response) {
        this.response = response;
    }

    public long getContentLength() {
        return response.getContentLength();
    }

    public String getOperationName() {
        throw new UnsupportedOperationException();
    }

    public String getRoutingText() {
        throw new UnsupportedOperationException();
    }

    public byte[] getRoutingBytes() {
        throw new UnsupportedOperationException();
    }
}
