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
package org.fabric3.binding.ejb.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

import org.osoa.sca.ServiceRuntimeException;

import org.fabric3.binding.ejb.provision.EjbWireTargetDefinition;
import org.fabric3.binding.ejb.scdl.EjbBindingDefinition;

import org.fabric3.spi.builder.WiringException;

/**
 *
 */
public class EjbResolver {

    //TODO improve error messages.  eg. jndiName might not be set.
    
    private final URI uri;
    private final String ejbLink;
    private final Class<?> interfaceClass;

    private final EjbRegistry ejbRegistry;

    private Object cachedReference = null;


    public EjbResolver(EjbWireTargetDefinition wireTarget, EjbRegistry ejbRegistry, Class<?> interfaceClass)
            throws WiringException {

        this.ejbRegistry = ejbRegistry;
        this.interfaceClass = interfaceClass;

        EjbBindingDefinition bindingDefinition = wireTarget.getBindingDefinition();
        uri = bindingDefinition.getTargetUri();
        ejbLink = bindingDefinition.getEjbLink();

        if(uri == null && ejbLink == null) {
            throw new WiringException("Ejb bindings must specify either an ejbLink or a JNDI name");
        }

    }

    public Object resolveStatelessEjb() {
        if(cachedReference != null) return cachedReference;

        Object ejb = resolveEjb();

        if(javax.ejb.EJBHome.class.isAssignableFrom(ejb.getClass()) || javax.ejb.EJBLocalHome.class.isAssignableFrom(ejb.getClass())) {

            try {
                Method method = ejb.getClass().getMethod("create");
                ejb = method.invoke(ejb);
            } catch(NoSuchMethodException nsme) {
                throw new ServiceRuntimeException("Invalid EJB 2.x binding: the object at the JNDI name "+
                        uri + " does not define a method with the signature create();");
            } catch(InvocationTargetException ite) {
                throw new ServiceRuntimeException("An error occurred while invoking the create() method "+
                        "of the EJB with the following JNDI name: "+uri, ite.getCause());
            } catch(IllegalAccessException iae) {
                throw new ServiceRuntimeException(iae);
            }
        }
        cachedReference = ejb;
        return ejb;
    }

    public Object resolveStatefulEjb() {
        if(cachedReference != null) return cachedReference;
        Object ejb = resolveEjb();

        if(javax.ejb.EJBHome.class.isAssignableFrom(ejb.getClass()) ||
                javax.ejb.EJBLocalHome.class.isAssignableFrom(ejb.getClass())) {
            // it's safe to cache SFSB home objects
            cachedReference = ejb;
        }

        return ejb;
    }

    private Object resolveEjb() {

        try {
            if(uri != null) {
                return ejbRegistry.resolveEjb(uri);
            } else {
                return ejbRegistry.resolveEjbLink(ejbLink, interfaceClass);
            }
        } catch(Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

}
