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
package org.fabric3.hessian.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.serializer.Serializer;
import org.fabric3.spi.binding.serializer.UnsupportedTypesException;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.util.Base64;
import org.fabric3.hessian.format.QNameSerializerFactory;

/**
 * Serializer that uses Hessian for reading and writing data.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class HessianSerializer implements Serializer {
    private SerializerFactory factory;
    private ClassLoader loader;

    /**
     * Constructor.
     *
     * @param loader the classloader for deserializing parameter and fault types. Hessian requires this classloader to be set as the TCCL.
     */
    public HessianSerializer(ClassLoader loader) {
        this.loader = loader;
        factory = new SerializerFactory();
        // add custom serializers
        factory.addFactory(new QNameSerializerFactory());
    }

    public <T> T serialize(Class<T> clazz, Object o) throws EncoderException {

        try {
            boolean isString = String.class.equals(clazz);
            if (!isString && !Byte.TYPE.equals(clazz.getComponentType())) {
                throw new UnsupportedTypesException("This implementation only supports serialization to bytes and strings");
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Hessian2Output out = new Hessian2Output(bos);
            out.setSerializerFactory(factory);
            out.startMessage();
            out.writeObject(o);
            out.completeMessage();
            out.close();
            if (isString) {
                return clazz.cast(Base64.encode(bos.toByteArray()));
            } else {
                return clazz.cast(bos.toByteArray());
            }
        } catch (IOException e) {
            throw new EncoderException(e);
        }
    }

    public <T> T serializeResponse(Class<T> clazz, Object message) throws EncoderException {
        return serialize(clazz, message);
    }

    public <T> T serializeFault(Class<T> clazz, Throwable exception) throws EncoderException {
        return serialize(clazz, exception);
    }

    public Message deserializeMessage(Object serialized) throws EncoderException {
        return deserialize(Message.class, serialized, Message.class.getClassLoader());
    }

    public <T> T deserialize(Class<T> clazz, Object object) throws EncoderException {
        return deserialize(clazz, object, loader);
    }

    public <T> T deserializeResponse(Class<T> clazz, Object object) throws EncoderException {
        return deserialize(clazz, object, loader);
    }

    private <T> T deserialize(Class<T> clazz, Object object, ClassLoader classLoader) throws EncoderException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            boolean isString = String.class.equals(object.getClass());
            if (!isString && !Byte.TYPE.equals(object.getClass().getComponentType())) {
                throw new UnsupportedTypesException("This implementation only supports serialization from bytes or strings");
            }
            InputStream is;
            if (isString) {
                is = new ByteArrayInputStream(Base64.decode((String) object));
            } else {
                byte[] bytes = (byte[]) object;
                is = new ByteArrayInputStream(bytes);
            }
            Thread.currentThread().setContextClassLoader(classLoader);
            Hessian2Input in = new Hessian2Input(is);
            in.setSerializerFactory(factory);
            in.startMessage();
            Object ret = in.readObject(clazz);
            in.completeMessage();
            in.close();
            return clazz.cast(ret);
        } catch (IOException e) {
            throw new EncoderException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }


    }

    public Throwable deserializeFault(Object serialized) throws EncoderException {
        return deserialize(Throwable.class, serialized);
    }

}