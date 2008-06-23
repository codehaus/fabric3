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
package org.fabric3.timer.component.introspection;

import java.text.ParseException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.MissingAttribute;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.java.introspection.ImplementationNotFound;
import org.fabric3.java.introspection.JavaImplementationProcessor;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.timer.component.provision.TriggerType;
import org.fabric3.timer.component.scdl.TimerImplementation;
import static org.fabric3.timer.component.scdl.TimerImplementation.UNSPECIFIED;

/**
 * Loads <implementation.timer> entries in a composite.
 */
public class TimerImplementationLoader implements TypeLoader<TimerImplementation> {

    private final JavaImplementationProcessor implementationProcessor;
    private final LoaderHelper loaderHelper;


    public TimerImplementationLoader(@Reference JavaImplementationProcessor implementationProcessor, @Reference LoaderHelper loaderHelper) {
        this.implementationProcessor = implementationProcessor;
        this.loaderHelper = loaderHelper;
    }


    public TimerImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        assert TimerImplementation.IMPLEMENTATION_TIMER.equals(reader.getName());

        TimerImplementation implementation = new TimerImplementation();
        if (!processImplementationClass(implementation, reader, context)) {
            // an error with the implementation class, return a dummy implementation
            PojoComponentType type = new PojoComponentType();
            implementation.setComponentType(type);
            return implementation;
        }
        processCronExpression(reader, context, implementation);
        processRepeatInterval(reader, context, implementation);
        processRepeatFixedRate(reader, context, implementation);
        processFireOnce(reader, context, implementation);
        validateFireData(reader, context, implementation);
        loaderHelper.loadPolicySetsAndIntents(implementation, reader, context);

        LoaderUtil.skipToEndElement(reader);

        implementationProcessor.introspect(implementation, context);
        return implementation;
    }

    private void validateFireData(XMLStreamReader reader, IntrospectionContext context, TimerImplementation implementation) {
        if (implementation.getCronExpression() == null
                && implementation.getFixedRate() == UNSPECIFIED
                && implementation.getRepeatInterval() == UNSPECIFIED
                && implementation.getFireOnce() == UNSPECIFIED) {
            MissingAttribute failure =
                    new MissingAttribute("A cron expression, fixed rate, repeat interval, or fire once time must be specified on the timer component",
                                         null,
                                         reader);
            context.addError(failure);
        }
    }

    private boolean processImplementationClass(TimerImplementation implementation, XMLStreamReader reader, IntrospectionContext context)
            throws XMLStreamException {

        String implClass = reader.getAttributeValue(null, "class");
        if (implClass == null) {
            MissingAttribute failure = new MissingAttribute("The class attribute was not specified", "class", reader);
            context.addError(failure);
            LoaderUtil.skipToEndElement(reader);
            return false;
        }
        implementation.setImplementationClass(implClass);
        try {
            Class<?> clazz = context.getTargetClassLoader().loadClass(implClass);
            if (!(Runnable.class.isAssignableFrom(clazz))) {
                InvalidInterface failure = new InvalidInterface(implementation);
                context.addError(failure);
                LoaderUtil.skipToEndElement(reader);
                return true;  // have processing continue

            }
        } catch (ClassNotFoundException e) {
            ImplementationNotFound failure = new ImplementationNotFound(implementation);
            context.addError(failure);
            LoaderUtil.skipToEndElement(reader);
            return false;
        }
        return true;
    }

    private void processCronExpression(XMLStreamReader reader, IntrospectionContext introspectionContext, TimerImplementation implementation) {
        String cronExpression = reader.getAttributeValue(null, "cronExpression");
        if (cronExpression != null) {
            try {
                new CronExpression(cronExpression);
                implementation.setTriggerType(TriggerType.CRON);
                implementation.setCronExpression(cronExpression);
            } catch (ParseException e) {
                InvalidTimerExpression failure =
                        new InvalidTimerExpression("Cron expression is invalid: " + cronExpression, cronExpression, reader, e);
                introspectionContext.addError(failure);
            }
        }
    }

    private void processRepeatInterval(XMLStreamReader reader, IntrospectionContext introspectionContext, TimerImplementation implementation) {
        String repeatInterval = reader.getAttributeValue(null, "repeatInterval");
        if (repeatInterval != null) {
            if (implementation.getCronExpression() != null) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Cron expression and repeat interval both specified", reader);
                introspectionContext.addError(failure);
            }
            try {
                long repeat = Long.parseLong(repeatInterval);
                implementation.setTriggerType(TriggerType.INTERVAL);
                implementation.setRepeatInterval(repeat);
            } catch (NumberFormatException e) {
                InvalidTimerExpression failure =
                        new InvalidTimerExpression("Repeat interval is invalid: " + repeatInterval, repeatInterval, reader, e);
                introspectionContext.addError(failure);
            }
        }
    }

    private void processRepeatFixedRate(XMLStreamReader reader, IntrospectionContext introspectionContext, TimerImplementation implementation) {
        String fixedRate = reader.getAttributeValue(null, "fixedRate");
        if (fixedRate != null) {
            if (implementation.getCronExpression() != null) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Cron expression and fixed rate both specified", reader);
                introspectionContext.addError(failure);
            }
            if (implementation.getRepeatInterval() != UNSPECIFIED) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Repeat interval and fixed rate both specified", reader);
                introspectionContext.addError(failure);
            }
            try {
                long rate = Long.parseLong(fixedRate);
                implementation.setTriggerType(TriggerType.FIXED_RATE);
                implementation.setFixedRate(rate);
            } catch (NumberFormatException e) {
                InvalidTimerExpression failure =
                        new InvalidTimerExpression("Fixed rate interval is invalid: " + fixedRate, fixedRate, reader, e);
                introspectionContext.addError(failure);
            }
        }
    }

    private void processFireOnce(XMLStreamReader reader, IntrospectionContext introspectionContext, TimerImplementation implementation) {
        String time = reader.getAttributeValue(null, "fireOnce");
        if (time != null) {
            if (implementation.getCronExpression() != null) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Cron expression and fire once both specified", reader);
                introspectionContext.addError(failure);
            }
            if (implementation.getRepeatInterval() != UNSPECIFIED) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Repeat interval and fire once both specified", reader);
                introspectionContext.addError(failure);
            }
            if (implementation.getFixedRate() != UNSPECIFIED) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Ficed rate and fire once both specified", reader);
                introspectionContext.addError(failure);
            }
            try {
                long rate = Long.parseLong(time);
                implementation.setTriggerType(TriggerType.ONCE);
                implementation.setFireOnce(rate);
            } catch (NumberFormatException e) {
                InvalidTimerExpression failure =
                        new InvalidTimerExpression("Fire once time is invalid: " + time, time, reader, e);
                introspectionContext.addError(failure);
            }
        }
    }

}