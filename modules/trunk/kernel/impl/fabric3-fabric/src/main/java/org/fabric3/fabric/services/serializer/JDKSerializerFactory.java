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
package org.fabric3.fabric.services.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Service;

import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.serializer.Serializer;
import org.fabric3.spi.binding.serializer.SerializerFactory;
import org.fabric3.spi.binding.serializer.UnsupportedTypesException;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.util.Base64;

/**
 * Serializer that uses JDK serialization.
 *
 * @version $Revision$ $Date$
 */
@Service(SerializerFactory.class)
@EagerInit
public class JDKSerializerFactory implements SerializerFactory, Serializer {

    public Serializer getInstance(Set<Class<?>> types, Set<Class<?>> faultTypes, ClassLoader classLoader) throws EncoderException {
        return this;
    }

    public <T> T serialize(Class<T> clazz, Object o) throws EncoderException {
        ObjectOutputStream stream = null;
        try {
            boolean isString = String.class.equals(clazz);
            if (!Byte.TYPE.equals(clazz.getComponentType()) && !isString) {
                throw new UnsupportedTypesException("This implementation only supports serialization to bytes or Strings");
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            stream = new ObjectOutputStream(bos);
            stream.writeObject(o);
            stream.flush();
            byte[] bytes = bos.toByteArray();
            if (isString) {
                return clazz.cast(Base64.encode(bytes));
            } else {
                return clazz.cast(bytes);
            }
        } catch (IOException e) {
            throw new EncoderException(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public <T> T serializeResponse(Class<T> clazz, Object message) throws EncoderException {
        return serialize(clazz, message);
    }

    public <T> T serializeFault(Class<T> clazz, Throwable exception) throws EncoderException {
        return serialize(clazz, exception);
    }

    public Message deserializeMessage(Object serialized) throws EncoderException {
        return deserialize(Message.class, serialized);
    }

    public <T> T deserialize(Class<T> clazz, Object object) throws EncoderException {
        ByteArrayInputStream bis = null;
        ObjectInputStream stream = null;
        try {
            boolean isString = String.class.equals(clazz);
            if (!Byte.TYPE.equals(object.getClass().getComponentType()) && isString) {
                throw new UnsupportedTypesException("This implementation only supports serialization from bytes or Strings");
            }
            byte[] bytes;
            if (isString) {
                bytes = Base64.decode((String) object);
            } else {
                bytes = (byte[]) object;
            }
            bis = new ByteArrayInputStream(bytes);
            stream = new ObjectInputStream(bis);
            return clazz.cast(stream.readObject());
        } catch (IOException e) {
            throw new EncoderException(e);
        } catch (ClassNotFoundException e) {
            throw new EncoderException(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public <T> T deserializeResponse(Class<T> clazz, Object object) throws EncoderException {
        return deserialize(clazz, object);
    }

    public Throwable deserializeFault(Object serialized) throws EncoderException {
        return deserialize(Throwable.class, serialized);
    }


}
