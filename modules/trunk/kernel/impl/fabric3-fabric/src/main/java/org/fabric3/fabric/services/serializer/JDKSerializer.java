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

import org.fabric3.spi.services.serializer.SerializationException;
import org.fabric3.spi.services.serializer.Serializer;

/**
 * Serializer that uses JDK serialization.
 *
 * @version $Revision$ $Date$
 */
public class JDKSerializer implements Serializer {

    public byte[] serialize(Object o) throws SerializationException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(bos);
            stream.writeObject(o);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }

    public <T> T deserialize(Class<T> clazz, byte[] bytes) throws SerializationException {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream stream = new ObjectInputStream(bis);
            return clazz.cast(stream.readObject());
        } catch (IOException e) {
            throw new SerializationException(e);
        } catch (ClassNotFoundException e) {
            throw new SerializationException(e);
        }
    }


}
