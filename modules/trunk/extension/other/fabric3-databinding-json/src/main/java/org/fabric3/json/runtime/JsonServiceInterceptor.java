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
package org.fabric3.json.runtime;

import java.io.IOException;
import java.io.StringWriter;

import org.codehaus.jackson.map.ObjectMapper;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.wire.Interceptor;

/**
 * Transforms from a JSON serialized form to a Java type. Placed on the service side of a wire.
 *
 * @version $Revision$ $Date$
 */
public class JsonServiceInterceptor implements Interceptor {
    private Interceptor next;
    private ObjectMapper mapper;
    private Class<?> toType;

    public JsonServiceInterceptor(ObjectMapper mapper, Class<?> toType) {
        this.mapper = mapper;
        this.toType = toType;
    }

    public Message invoke(Message msg) {
        try {
            read(msg);
            Message ret = next.invoke(msg);
            write(ret);
            return ret;
        } catch (IOException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Interceptor getNext() {
        return next;
    }

    private void write(Message msg) throws IOException {
        Object body = msg.getBody();
        if (body == null) {
            return;
        }
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, body);
        if (msg.isFault()) {
            msg.setBodyWithFault(writer.toString());
        } else {
            msg.setBody(writer.toString());
        }
    }

    private void read(Message msg) throws IOException {
        if (msg.getBody() == null) {
            return;
        }
        if (!(msg.getBody() instanceof String)) {
            throw new AssertionError("Type must be encoded as a JSON string:" + msg.getBody());
        }
        String body = (String) msg.getBody();
        if (body.length() == 0) {
            msg.setBody(null);
            return;
        }
        Object to = mapper.readValue(body, toType);
        msg.setBody(new Object[]{to});
    }
}