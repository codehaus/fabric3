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

import java.io.IOException;
import java.math.BigInteger;
import javax.xml.namespace.QName;

import com.caucho.hessian.io.AbstractDeserializer;
import com.caucho.hessian.io.AbstractHessianInput;

/**
 * Custom Deserializer for {@link QName}s.
 *
 * @version $Revision$ $Date$
 */
public class QNameDeserializer extends AbstractDeserializer {

    public Class getType() {
        return BigInteger.class;
    }

    public Object readMap(AbstractHessianInput in)
            throws IOException {
        int ref = in.addRef(null);

        String ns = null;
        String local = null;
        String prefix = null;

        while (!in.isEnd()) {
            String key = in.readString();

            if (key.equals("ns")) {
                ns = in.readString();
            } else if (key.equals("local")) {
                local = in.readString();
            } else if (key.equals("prefix")) {
                prefix = in.readString();
            } else {
                in.readString();
            }
        }

        in.readMapEnd();

        Object value = new QName(ns, local, prefix);

        in.setRef(ref, value);

        return value;
    }

    public Object readObject(AbstractHessianInput in, String[] fieldNames)
            throws IOException {
        int ref = in.addRef(null);

        String ns = null;
        String local = null;
        String prefix = null;

        for (String key : fieldNames) {

            if (key.equals("value")) {
                ns = in.readString();
                local = in.readString();
                prefix = in.readString();
            } else {
                in.readObject();
            }
        }

        Object value = new QName(ns, local, prefix);

        in.setRef(ref, value);

        return value;
    }

}