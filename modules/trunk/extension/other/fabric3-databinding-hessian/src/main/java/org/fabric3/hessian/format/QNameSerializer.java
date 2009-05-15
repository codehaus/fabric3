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
import javax.xml.namespace.QName;

import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.AbstractSerializer;

/**
 * Custom Serializer for {@link QName}s.
 *
 * @version $Revision$ $Date$
 */
public class QNameSerializer extends AbstractSerializer {

    public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {

        if (obj == null)
            out.writeNull();
        else {
            Class cl = obj.getClass();

            if (out.addRef(obj))
                return;

            int ref = out.writeObjectBegin(cl.getName());

            QName name = (QName) obj;

            if (ref < -1) {
                out.writeString("value");
                out.writeString(name.getNamespaceURI());
                out.writeString(name.getLocalPart());
                out.writeString(name.getPrefix());
                out.writeMapEnd();
            } else {
                if (ref == -1) {
                    out.writeInt(1);
                    out.writeString("value");
                    out.writeObjectBegin(cl.getName());
                }

                out.writeString(name.getNamespaceURI());
                out.writeString(name.getLocalPart());
                out.writeString(name.getPrefix());
            }
        }
    }


}
