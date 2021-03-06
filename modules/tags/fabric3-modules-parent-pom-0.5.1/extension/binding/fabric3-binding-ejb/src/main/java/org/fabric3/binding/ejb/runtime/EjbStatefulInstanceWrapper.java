package org.fabric3.binding.ejb.runtime;

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceInitializationException;

/**
 * @version $Revision Date: Oct 29, 2007 Time: 5:01:22 PM
 */
public class EjbStatefulInstanceWrapper implements InstanceWrapper {

    private Object instance;
    private boolean started = false;

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Object getInstance() {
        return instance;
    }

    public boolean isStarted() {
        return started;
    }

    public void start() throws InstanceInitializationException {
        started = true;
    }

    public void stop() throws InstanceDestructionException {

        try {
            if(instance instanceof EJBObject) {
                ((EJBObject)instance).remove();

            } else if(instance instanceof EJBLocalObject) {
                ((EJBLocalObject)instance).remove();
            }
        } catch(Exception e) {
            throw new InstanceDestructionException("Error removing target EJB", e);
        }

        
        started = false;
    }
    
    public void reinject() {
    }

    public void addObjectFactory(String referenceName, ObjectFactory factory, Object key) {
        // TODO Auto-generated method stub
        
    }
    
}
