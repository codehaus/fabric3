  /*
   * Fabric3
   * Copyright (C) 2009 Metaform Systems
   *
   * Fabric3 is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as
   * published by the Free Software Foundation, either version 3 of
   * the License, or (at your option) any later version, with the
   * following exception:
   *
   * Linking this software statically or dynamically with other
   * modules is making a combined work based on this software.
   * Thus, the terms and conditions of the GNU General Public
   * License cover the whole combination.
   *
   * As a special exception, the copyright holders of this software
   * give you permission to link this software with independent
   * modules to produce an executable, regardless of the license
   * terms of these independent modules, and to copy and distribute
   * the resulting executable under terms of your choice, provided
   * that you also meet, for each linked independent module, the
   * terms and conditions of the license of that module. An
   * independent module is a module which is not derived from or
   * based on this software. If you modify this software, you may
   * extend this exception to your version of the software, but
   * you are not obligated to do so. If you do not wish to do so,
   * delete this exception statement from your version.
   *
   * Fabric3 is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty
   * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   * See the GNU General Public License for more details.
   *
   * You should have received a copy of the
   * GNU General Public License along with Fabric3.
   * If not, see <http://www.gnu.org/licenses/>.
   */
package org.fabric3.timer.component.runtime;

import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.wire.InvocationRuntimeException;

/**
 * Implementation registered with the runtime TimerService to receive notifications and invoke a component instance when a trigger has fired.
 *
 * @version $Revision$ $Date$
 */
public class TimerComponentInvoker<T> implements Runnable {
    private TimerComponent<T> component;
    private ScopeContainer scopeContainer;

    public TimerComponentInvoker(TimerComponent<T> component) {
        this.component = component;
        this.scopeContainer = component.getScopeContainer();
    }

    public void run() {
        // create a new work context
        WorkContext workContext = new WorkContext();
        CallFrame frame = new CallFrame();
        workContext.addCallFrame(frame);
        InstanceWrapper<T> wrapper;
        try {
            // TODO handle conversations
            //startOrJoinContext(workContext);
            wrapper = scopeContainer.getWrapper(component, workContext);
        } catch (InstanceLifecycleException e) {
            throw new InvocationRuntimeException(e);
        }

        try {
            Object instance = wrapper.getInstance();
            assert instance instanceof Runnable;  // all timer components must implement java.lang.Runnable
            WorkContext oldWorkContext = WorkContextTunnel.setThreadWorkContext(workContext);
            try {
                ((Runnable) instance).run();
            } finally {
                WorkContextTunnel.setThreadWorkContext(oldWorkContext);
            }
        } finally {
            try {
                scopeContainer.returnWrapper(component, workContext, wrapper);
                // TODO handle conversations
            } catch (InstanceDestructionException e) {
                //noinspection ThrowFromFinallyBlock
                throw new InvocationRuntimeException(e);
            }
        }

    }
}
