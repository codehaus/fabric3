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
package org.fabric3.fabric.binding.format;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.fabric3.spi.binding.format.BaseMessageEncoder;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.classloader.ClassLoaderObjectInputStream;

/**
 * MessageEncoder that uses JDK serialization to encode/decode messages.
 *
 * @version $Revision$ $Date$
 */
public class JDKMessageEncoder extends BaseMessageEncoder {

    protected byte[] encode(Object o) throws EncoderException {
        ObjectOutputStream stream = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            stream = new ObjectOutputStream(bos);
            stream.writeObject(o);
            stream.flush();
            return bos.toByteArray();
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

    protected <T> T decode(Class<T> clazz, byte[] bytes) throws EncoderException {
        ByteArrayInputStream bis = null;
        ObjectInputStream stream = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            stream = new ClassLoaderObjectInputStream(bis, this.getClass().getClassLoader());
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


}