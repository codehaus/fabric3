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
package org.fabric3.hessian.runtime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.util.Base64;
import org.fabric3.spi.wire.Interceptor;

/**
 * Transforms from a Hessian serialized form to a Java type. Placed on the service side of a wire.
 *
 * @version $Revision$ $Date$
 */
public class HessianServiceInterceptor implements Interceptor {
    private Interceptor next;
    private ClassLoader classLoader;
    private SerializerFactory serializerFactory;

    public HessianServiceInterceptor(SerializerFactory serializerFactory, ClassLoader classLoader) {
        this.serializerFactory = serializerFactory;
        this.classLoader = classLoader;
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
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(bos);
        out.startReply();
        if (msg.isFault()) {
            out.writeFault("400", "Exception", msg.getBody());
            out.completeReply();
            out.close();
            byte[] data = bos.toByteArray();
            msg.setBodyWithFault(Base64.encode(data));
        } else {
            out.writeObject(msg.getBody());
            out.completeReply();
            out.close();
            byte[] data = bos.toByteArray();
            msg.setBody(Base64.encode(data));
        }
    }

    private void read(Message msg) throws IOException {
        if (msg.getBody() == null) {
            return;
        }
        if (!(msg.getBody() instanceof String)) {
            throw new AssertionError("Type must be encoded as a Hessian string:" + msg.getBody());
        }
        String body = (String) msg.getBody();
        if (body.length() == 0) {
            msg.setBody(null);
            return;
        }


        ByteArrayInputStream bin = new ByteArrayInputStream(Base64.decode(body));
        Hessian2Input in = new Hessian2Input(bin);
        in.setSerializerFactory(serializerFactory);

        in.startMessage();

        Object[] params;
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            // Hessian uses the TCCL to deserialize parameters
            Thread.currentThread().setContextClassLoader(classLoader);
            params = (Object[]) in.readObject();
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }

        in.completeMessage();
        in.close();
        bin.close();
        msg.setBody(params);
    }
}