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
package org.fabric3.hessian.format;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.binding.format.BaseParameterEncoder;
import org.fabric3.spi.binding.format.EncoderException;

/**
 * ParameterEncoder that uses Hessian.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class HessianParameterEncoder extends BaseParameterEncoder {
    private SerializerFactory factory;

    /**
     * Constructor.
     *
     * @param loader the classloader for deserializing parameter and fault types. Hessian requires this classloader to be set as the TCCL.
     */
    public HessianParameterEncoder(ClassLoader loader) {
        super(loader);
        factory = new SerializerFactory();
        // add custom serializers
        factory.addFactory(new QNameSerializerFactory());
    }


    protected byte[] serialize(Object o) throws EncoderException {
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
            throw new EncoderException(e);
        }
    }


    protected <T> T deserialize(Class<T> clazz, byte[] bytes, ClassLoader classLoader) throws EncoderException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            InputStream is = new ByteArrayInputStream(bytes);
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

}