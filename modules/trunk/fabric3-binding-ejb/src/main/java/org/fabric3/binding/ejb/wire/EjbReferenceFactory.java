package org.fabric3.binding.ejb.wire;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.osoa.sca.ServiceRuntimeException;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ejb.model.logical.EjbBindingDefinition;
import org.fabric3.binding.ejb.model.physical.EjbWireTargetDefinition;
import org.fabric3.binding.ejb.spi.EjbLinkResolver;
import org.fabric3.binding.ejb.spi.EjbLinkException;

/**
 * Created by IntelliJ IDEA. User: mshinn Date: Jul 5, 2007 Time: 3:11:40 PM To change this template use File | Settings
 * | File Templates.
 */
public class EjbReferenceFactory {

    //TODO improve error messages.  eg. jndiName might not be set.
    
    private final String jndiName;
    private final String ejbLink;
    private final String homeInterfaceName;
    private final String interfaceName;

    private final EjbLinkResolver ejbLinkResolver;

    private Object cachedReference = null;


    public EjbReferenceFactory(EjbWireTargetDefinition wireTarget, EjbLinkResolver ejbLinkResolver) {
        EjbBindingDefinition bindingDefinition = wireTarget.getBindingDefinition();
        jndiName = bindingDefinition.getJndiName();
        ejbLink = bindingDefinition.getEjbLink();
        homeInterfaceName = bindingDefinition.getHomeInterface();
        interfaceName = wireTarget.getInterfaceName();
        this.ejbLinkResolver = ejbLinkResolver;
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
                        jndiName + " does not define a method with the signature create();");
            } catch(InvocationTargetException ite) {
                throw new ServiceRuntimeException("An error occurred while invoking the create() method "+
                        "of the EJB with the following JNDI name: "+jndiName, ite.getCause());
            } catch(IllegalAccessException iae) {
                throw new ServiceRuntimeException(iae);
            }
        }
        cachedReference = ejb;
        return ejb;
    }

    private Object resolveEjb() {
        if(jndiName == null && ejbLink == null) {
            //TODO: This should be a deployment time check
            throw new ServiceRuntimeException("Ejb bindings must specify either an ejbLink or a JNDI name");
        }

        if(jndiName != null)
            return resolveJndiName();
        else
            return resolveEjbLink();
    }

    private Object resolveJndiName() {
        try {
            InitialContext ic = new InitialContext();
            return ic.lookup(jndiName);
        } catch(NamingException ne) {
            throw new ServiceRuntimeException(ne);
        }
    }

    private Object resolveEjbLink() {
        // resolve the target EJB using an ejb-link.
        if(ejbLinkResolver == null) {
            throw new ServiceRuntimeException("The ejb-link resolver has not been set in the Fabric3 runtime");
        }

        try {
            String ifaceName = homeInterfaceName != null ? homeInterfaceName : interfaceName;
            return ejbLinkResolver.resolveEjbLink(ejbLink, ifaceName);
        } catch(EjbLinkException ele) {
            throw new ServiceRuntimeException(ele);
        }
    }



}
