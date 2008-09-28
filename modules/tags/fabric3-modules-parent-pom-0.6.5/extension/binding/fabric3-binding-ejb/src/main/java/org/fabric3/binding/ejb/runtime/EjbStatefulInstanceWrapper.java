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
