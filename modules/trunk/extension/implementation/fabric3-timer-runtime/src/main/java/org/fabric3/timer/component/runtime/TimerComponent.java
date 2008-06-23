/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.timer.component.runtime;

import java.net.URI;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.fabric3.java.runtime.JavaComponent;
import org.fabric3.pojo.injection.MultiplicityObjectFactory;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.services.proxy.ProxyService;
import org.fabric3.timer.spi.TimerService;

/**
 * A timer component implementation.
 *
 * @version $Revision$ $Date$
 */
public class TimerComponent<T> extends JavaComponent<T> {
    private TriggerData data;
    private TimerService timerService;
    private TimerService trxTimerService;
    private ScheduledFuture<?> future;

    /**
     * Constructor for a timer component.
     *
     * @param componentId             the component's uri
     * @param instanceFactoryProvider the provider for the instance factory
     * @param scopeContainer          the container for the component's implementation scope
     * @param groupId                 the component group this component belongs to
     * @param initLevel               the initialization level
     * @param maxIdleTime             the time after which idle instances of this component can be expired
     * @param maxAge                  the time after which instances of this component can be expired
     * @param proxyService            the service used to create reference proxies
     * @param propertyFactories       map of factories for property values
     * @param referenceFactories      object factories for multiplicity references
     * @param data                    timer fire data
     * @param nonTrxTimerService      the non transactional timer service
     * @param trxTimerService         the transactional timer service
     */
    public TimerComponent(URI componentId,
                          InstanceFactoryProvider<T> instanceFactoryProvider,
                          ScopeContainer scopeContainer,
                          URI groupId,
                          int initLevel,
                          long maxIdleTime,
                          long maxAge,
                          ProxyService proxyService,
                          Map<String, ObjectFactory<?>> propertyFactories,
                          Map<String, MultiplicityObjectFactory<?>> referenceFactories,
                          TriggerData data,
                          TimerService nonTrxTimerService,
                          TimerService trxTimerService) {
        super(componentId,
              instanceFactoryProvider,
              scopeContainer,
              groupId,
              initLevel,
              maxIdleTime,
              maxAge,
              proxyService,
              propertyFactories,
              referenceFactories);
        this.data = data;
        this.timerService = nonTrxTimerService;
        this.trxTimerService = trxTimerService;
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
