package org.fabric3.binding.ejb.wire;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

import org.osoa.sca.ServiceRuntimeException;

import org.fabric3.binding.ejb.model.logical.EjbBindingDefinition;
import org.fabric3.binding.ejb.model.physical.EjbWireTargetDefinition;
import org.fabric3.binding.ejb.spi.EjbRegistry;

import org.fabric3.spi.builder.WiringException;

/**
 * Created by IntelliJ IDEA. User: mshinn Date: Jul 5, 2007 Time: 3:11:40 PM To change this template use File | Settings
 * | File Templates.
 */
public class EjbReferenceFactory {

    //TODO improve error messages.  eg. jndiName might not be set.
    
    private final URI uri;
    private final String ejbLink;
    private final String homeInterfaceName;
    private final String interfaceName;

    private final EjbRegistry ejbRegistry;

    private Object cachedReference = null;


    public EjbReferenceFactory(EjbWireTargetDefinition wireTarget, EjbRegistry ejbRegistry) {
        EjbBindingDefinition bindingDefinition = wireTarget.getBindingDefinition();
        uri = bindingDefinition.getTargetUri();
        ejbLink = bindingDefinition.getEjbLink();
        homeInterfaceName = bindingDefinition.getHomeInterface();
        interfaceName = wireTarget.getInterfaceName();
        this.ejbRegistry = ejbRegistry;
    }

    public Object getEjbReference() {
        if(cachedReference != null) return cachedReference;

        Object ejb = resolveEjb();
        
        if(homeInterfaceName != null) {
            try {
                Method method = ejb.getClass().getMethod("create", null);
                ejb = method.invoke(ejb, null);
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

    private Object resolveEjb() {

        if(uri == null && ejbLink == null) {
            //TODO: This should be a deployment time check
            throw new ServiceRuntimeException("Ejb bindings must specify either an ejbLink or a JNDI name");
        }

        try {
            if(uri != null) {
                return ejbRegistry.resolveEjb(uri);
            } else {
                String ifaceName = homeInterfaceName != null ? homeInterfaceName : interfaceName;
                return ejbRegistry.resolveEjbLink(ejbLink, ifaceName);
            }
        } catch(WiringException we) {
            throw new ServiceRuntimeException(we);
        }
    }

}
