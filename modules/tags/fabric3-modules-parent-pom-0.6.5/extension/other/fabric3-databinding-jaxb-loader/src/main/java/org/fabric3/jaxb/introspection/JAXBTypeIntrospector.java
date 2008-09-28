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
package org.fabric3.jaxb.introspection;

import java.lang.reflect.Method;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.fabric3.introspection.contract.OperationIntrospector;
import org.fabric3.jaxb.provision.JAXBConstants;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ValidationContext;

/**
 * Introspects operations for the presence of JAXB types. If a parameter is a JAXB type, the JAXB intent is added to the operation.
 *
 * @version $Revision$ $Date$
 */
public class JAXBTypeIntrospector implements OperationIntrospector {

    public <T> void introspect(Operation<T> operation, Method method, ValidationContext context) {
        // TODO perform error checking, e.g. mixing of databindings
        DataType<List<DataType<T>>> inputType = operation.getInputType();
        for (DataType<?> type : inputType.getLogical()) {
            if (isJAXB(type)) {
                operation.addIntent(JAXBConstants.DATABINDING_INTENT);
                return;
            }
        }
    }

    private boolean isJAXB(DataType<?> dataType) {
        if (dataType.getLogical() instanceof Class) {
            Class clazz = (Class) dataType.getLogical();
            if (clazz.isAnnotationPresent(XmlRootElement.class) || JAXBElement.class.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

}
