/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.binding.net.runtime.http;

import org.jboss.netty.handler.codec.http.HttpResponse;

import org.fabric3.spi.binding.format.ResponseEncodeCallback;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

/**
 * ResponseEncodeCallback for an HTTP response.
 *
 * @version $Rev$ $Date$
 */
public class HttpResponseCallback implements ResponseEncodeCallback {
    private HttpResponse response;
    // TODO add as callback
    private String contentType = "text/plain; charset=UTF-8";

    public HttpResponseCallback(HttpResponse response) {
        this.response = response;
    }

    public void encodeContentLengthHeader(long length) {
        response.setHeader(CONTENT_LENGTH, String.valueOf(length));
        // TODO FIXME make a callback event
        response.setHeader(CONTENT_TYPE, contentType);
    }

}
