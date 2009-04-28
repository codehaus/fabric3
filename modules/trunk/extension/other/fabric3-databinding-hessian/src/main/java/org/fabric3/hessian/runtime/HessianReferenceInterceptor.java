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

import org.fabric3.hessian.provision.Encoding;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.util.Base64;
import org.fabric3.spi.wire.Interceptor;

/**
 * Transforms from Java types to a serialized Hessian format. Placed on the reference side of the wire.
 *
 * @version $Revision$ $Date$
 */
public class HessianReferenceInterceptor implements Interceptor {
    private Interceptor next;
    private Encoding encoding;
    private SerializerFactory serializerFactory;
    private ClassLoader classLoader;

    public HessianReferenceInterceptor(Encoding encoding, SerializerFactory serializerFactory, ClassLoader classLoader) {
        this.encoding = encoding;
        this.serializerFactory = serializerFactory;
        this.classLoader = classLoader;
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
        if (body.length == 0) {
            throw new AssertionError("Empty array passed as message body");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(bos);
        out.startMessage();
        out.writeObject(body);
        out.completeMessage();
        out.close();
        byte[] data = bos.toByteArray();
        if (encoding == Encoding.ASCII) {
            msg.setBody(Base64.encode(data));
        } else {
            msg.setBody(data);
        }
    }

    private void read(Message msg) throws IOException {
        if (msg.getBody() == null) {
            return;
        }
        if (encoding == Encoding.ASCII && !(msg.getBody() instanceof String)) {
            throw new AssertionError("Return type must be encoded as a Hessian string:" + msg.getBody());
        }
        ByteArrayInputStream bin;
        if (encoding == Encoding.ASCII) {
            String body = (String) msg.getBody();
            bin = new ByteArrayInputStream(Base64.decode(body));
        } else {
            byte[] body = (byte[]) msg.getBody();
            bin = new ByteArrayInputStream(body);
        }

        Hessian2Input in = new Hessian2Input(bin);
        in.setSerializerFactory(serializerFactory);

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            Object retValue = in.readReply(null);
            msg.setBody(retValue);
        } catch (Throwable throwable) {
            msg.setBodyWithFault(throwable);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }

        in.close();
        bin.close();
    }


}
