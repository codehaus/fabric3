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

import org.fabric3.spi.binding.serializer.SerializationException;
import org.fabric3.spi.binding.serializer.Serializer;
import org.fabric3.spi.binding.serializer.SerializerFactory;
import org.fabric3.spi.binding.serializer.UnsupportedTypesException;
import org.fabric3.spi.util.Base64;

/**
 * Serializer that uses JDK serialization.
 *
 * @version $Revision$ $Date$
 */
@Service(SerializerFactory.class)
@EagerInit
public class JDKSerializerFactory implements SerializerFactory, Serializer {

    public Serializer getInstance(Set<Class<?>> types, Set<Class<?>> faultTypes) throws SerializationException {
        return this;
    }

    public <T> T serialize(Class<T> clazz, Object o) throws SerializationException {
        try {
            boolean isString = String.class.equals(clazz);
            if (!Byte.TYPE.equals(clazz.getComponentType()) && !isString) {
                throw new UnsupportedTypesException("This implementation only supports serialization to bytes or Strings");
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(bos);
            stream.writeObject(o);
            byte[] bytes = bos.toByteArray();
            if (isString) {
                return clazz.cast(Base64.encode(bytes));
            } else {
                return clazz.cast(bytes);
            }
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }

    public <T> T serializeFault(Class<T> clazz, Throwable exception) throws SerializationException {
        return serialize(clazz, exception);
    }

    public <T> T deserialize(Class<T> clazz, Object object) throws SerializationException {
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
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream stream = new ObjectInputStream(bis);
            return clazz.cast(stream.readObject());
        } catch (IOException e) {
            throw new SerializationException(e);
        } catch (ClassNotFoundException e) {
            throw new SerializationException(e);
        }
    }

    public Throwable deserializeFault(Object serialized) throws SerializationException {
        return deserialize(Throwable.class, serialized);
    }


}
