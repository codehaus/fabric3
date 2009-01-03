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

import java.net.URI;
import java.text.ParseException;
import java.util.concurrent.ScheduledFuture;
import javax.xml.namespace.QName;

import org.fabric3.java.runtime.JavaComponent;
import org.fabric3.pojo.builder.ProxyService;
import org.fabric3.pojo.instancefactory.InstanceFactoryProvider;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.timer.component.provision.TriggerData;
import org.fabric3.timer.spi.TimerService;

/**
 * A timer component implementation.
 *
 * @version $Revision$ $Date$
 */
public class TimerComponent<T> extends JavaComponent<T> {
    private TriggerData data;
    private TimerService timerService;
    private ScheduledFuture<?> future;

    /**
     * Constructor for a timer component.
     *
     * @param componentId             the component's uri
     * @param instanceFactoryProvider the provider for the instance factory
     * @param scopeContainer          the container for the component's implementation scope
     * @param deployable              the deployable composite this component is deployed with  
     * @param initLevel               the initialization level
     * @param maxIdleTime             the time after which idle instances of this component can be expired
     * @param maxAge                  the time after which instances of this component can be expired
     * @param proxyService            the service used to create reference proxies
     * @param data                    timer fire data
     * @param timerService            the timer service
     */
    public TimerComponent(URI componentId,
                          InstanceFactoryProvider<T> instanceFactoryProvider,
                          ScopeContainer scopeContainer,
                          QName deployable,
                          int initLevel,
                          long maxIdleTime,
                          long maxAge,
                          ProxyService proxyService,
                          TriggerData data,
                          TimerService timerService) {
        super(componentId,
              instanceFactoryProvider,
              scopeContainer,
              deployable,
              initLevel,
              maxIdleTime,
              maxAge,
              proxyService);
        this.data = data;
        this.timerService = timerService;
    }

    public void start() {
        super.start();
        TimerComponentInvoker<T> invoker = new TimerComponentInvoker<T>(this);
        switch (data.getType()) {
        case CRON:
            try {
                future = timerService.schedule(invoker, data.getCronExpression());
            } catch (ParseException e) {
                // this should be caught on the controller
                throw new TimerComponentInitException(e);
            }
            break;
        case FIXED_RATE:
            throw new UnsupportedOperationException("Not yet implemented");
            // break;
        case INTERVAL:
            future = timerService.scheduleWithFixedDelay(invoker, data.getStartTime(), data.getRepeatInterval(), data.getTimeUnit());
            break;
        case ONCE:
            future = timerService.schedule(invoker, data.getFireOnce(), data.getTimeUnit());
            break;
        }
    }

    public void stop() {
        super.stop();
        if (future != null && !future.isCancelled() && !future.isDone()) {
            future.cancel(true);
        }
    }


}
