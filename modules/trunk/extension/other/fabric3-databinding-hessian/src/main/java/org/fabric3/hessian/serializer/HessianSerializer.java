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

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;

import org.fabric3.spi.services.serializer.SerializationException;
import org.fabric3.spi.services.serializer.Serializer;

/**
 * Serializer that uses Hessian serialization.
 *
 * @version $Revision$ $Date$
 */
public class HessianSerializer implements Serializer {
    private SerializerFactory factory = new SerializerFactory();

    public byte[] serialize(Object o) throws SerializationException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Hessian2Output out = new Hessian2Output(bos);
            out.setSerializerFactory(factory);
            out.startMessage();
            out.writeObject(o);
            out.completeMessage();
            out.close();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }

    public <T> T deserialize(Class<T> clazz, byte[] bytes) throws SerializationException {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            Hessian2Input in = new Hessian2Input(bis);
            in.setSerializerFactory(factory);
            in.startMessage();
            Object ret = in.readObject(clazz);
            in.completeMessage();
            in.close();
            return clazz.cast(ret);
        } catch (IOException e) {
            throw new SerializationException(e);
        }

    }
}
