package org.fabric3.binding.ejb.transport;

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;

import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.TargetDestructionException;
import org.fabric3.spi.component.TargetInitializationException;

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

    public void start() throws TargetInitializationException {
        started = true;
    }

    public void stop() throws TargetDestructionException {

        try {
            if(instance instanceof EJBObject) {
                ((EJBObject)instance).remove();

            } else if(instance instanceof EJBLocalObject) {
                ((EJBLocalObject)instance).remove();
            }
        } catch(Exception e) {
            throw new TargetDestructionException("Error removing target EJB", e);
        }

        
        started = false;
    }
    
    public void reinject() {
    }
    
}
