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

import javax.xml.namespace.QName;

import com.caucho.hessian.io.AbstractSerializerFactory;
import com.caucho.hessian.io.Deserializer;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.Serializer;

/**
 * Creates QNameSerializers and QNameDeserializers for use with Hessian.
 *
 * @version $Revision$ $Date$
 */
public class QNameSerializerFactory extends AbstractSerializerFactory {
    public Serializer getSerializer(Class cl) throws HessianProtocolException {
        if (QName.class.isAssignableFrom(cl)) {
            return new QNameSerializer();
        }
        return null;
    }

    public Deserializer getDeserializer(Class cl) throws HessianProtocolException {
        if (QName.class.isAssignableFrom(cl)) {
            return new QNameDeserializer();
        }
        return null;
    }
}
