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
package org.fabric3.json;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.wire.Interceptor;

/**
 * Transforms from Java types to a serialized JSON format. Placed on the reference side of the wire.
 *
 * @version $Revision$ $Date$
 */
public class JsonReferenceInterceptor implements Interceptor {
    private Interceptor next;
    private ObjectMapper mapper;
    private Class<?> returnType;
    private List<Class<?>> faultTypes;

    /**
     * Constructor.
     *
     * @param mapper     the JSON mapper
     * @param returnType the type of the return value
     * @param faultTypes types of declared faults
     */
    public JsonReferenceInterceptor(ObjectMapper mapper, Class<?> returnType, List<Class<?>> faultTypes) {
        this.mapper = mapper;
        this.returnType = returnType;
        this.faultTypes = faultTypes;
    }

    public Message invoke(Message msg) {
        try {
            write(msg);
            Message ret = next.invoke(msg);
            read(ret);
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
        Object[] body = (Object[]) msg.getBody();
        if (body == null) {
            return;
        }
        StringWriter writer = new StringWriter();
        if (body.length == 0) {
            throw new AssertionError("Empty array passed as message body");
        }
        if (body.length == 1) {
            mapper.writeValue(writer, body[0]);
        } else {
            // TODO support multiple params
            throw new UnsupportedOperationException();
        }
        msg.setBody(writer.toString());
    }

    private void read(Message msg) throws IOException {
        if (msg.getBody() == null) {
            return;
        }
        if (!(msg.getBody() instanceof String)) {
            throw new AssertionError("Return type must be encoded as a JSON string:" + msg.getBody());
        }
        String body = (String) msg.getBody();
        if (msg.isFault()) {
            // FIXME there has to be a better way to do this
            Object ret = null;
            // iterate through the fault types and deserialize
            for (Class<?> faultType : faultTypes) {
                try {
                    ret = mapper.readValue(body, faultType);
                } catch (JsonMappingException e) {
                    // ignore
                }
            }
            if (ret == null) {
                // undeclared runtime exception type, throw a generic one
                ret = new ServiceRuntimeException(body);
            }
            msg.setBodyWithFault(ret);
        } else {
            if (returnType.equals(Void.class)) {
                msg.setBody(null);
                return;
            }
            Object ret = mapper.readValue(body, returnType);
            msg.setBody(ret);
        }
    }
}
