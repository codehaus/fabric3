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
