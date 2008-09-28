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
package org.fabric3.spring.xml;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.spring.SpringComponentType;
import org.fabric3.spring.SpringImplementation;

/**
 * @version $Rev$ $Date$
 */
public class SpringComponentTypeLoaderImpl implements SpringComponentTypeLoader {

    public SpringComponentTypeLoaderImpl() {
    }

    public void load(SpringImplementation implementation, IntrospectionContext introspectionContext) throws LoaderException {
        SpringComponentType componentType = implementation.getComponentType();
//        componentType = loadByIntrospection(implementation, introspectionContext);
        if (componentType.getScope() == null) {
            componentType.setScope("STATELESS");
        }
        implementation.setComponentType(componentType);
    }

//    protected PojoComponentType loadByIntrospection(SpringImplementation implementation, IntrospectionContext context)
//            throws ProcessingException, MissingResourceException {
//        Class<?> implClass = null;
////                LoaderUtil.loadClass(implementation.getLocation(), context.getTargetClassLoader());
//        PojoComponentType componentType = new PojoComponentType(implClass.getName());
//        introspector.introspect(implClass, componentType, context);
//        return componentType;
//    }
}
