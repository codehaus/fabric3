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
package org.fabric3.binding.ws.axis2.introspection;

import java.lang.reflect.Method;

import javax.jws.WebMethod;

import org.fabric3.binding.ws.axis2.common.Constant;
import org.fabric3.spi.introspection.contract.OperationIntrospector;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ValidationContext;

/**
 * Introspects operations for the presence of JAX-WS annotations. JAX-WS annotations are used to configure the Axis2 engine.
 * 
 * @version $Revision$ $Date$
 */
public class JAXWSTypeIntrospector implements OperationIntrospector {

    public <T> void introspect(Operation<T> operation, Method method, ValidationContext context) {
        WebMethod webMethod = method.getAnnotation(WebMethod.class);
        if (webMethod != null) {
            String soapAction = webMethod.action();
            if (soapAction != null) {
                operation.addInfo(Constant.AXIS2_JAXWS_QNAME, Constant.SOAP_ACTION, soapAction);
            }
        }

    }
}
