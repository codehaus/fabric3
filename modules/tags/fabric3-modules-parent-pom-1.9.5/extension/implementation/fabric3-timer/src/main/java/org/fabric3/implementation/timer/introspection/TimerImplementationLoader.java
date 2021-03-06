/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.implementation.timer.introspection;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.implementation.java.introspection.ImplementationArtifactNotFound;
import org.fabric3.implementation.java.introspection.JavaImplementationProcessor;
import org.fabric3.implementation.timer.model.TimerImplementation;
import org.fabric3.implementation.timer.provision.TimerData;
import org.fabric3.implementation.timer.provision.TimerType;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.model.type.java.InjectingComponentType;

import static org.fabric3.implementation.timer.provision.TimerData.UNSPECIFIED;

/**
 * Loads <implementation.timer> entries in a composite.
 *
 * @version $Rev: 7881 $ $Date: 2009-11-22 10:32:23 +0100 (Sun, 22 Nov 2009) $
 */
public class TimerImplementationLoader extends AbstractValidatingTypeLoader<TimerImplementation> {
    private final JavaImplementationProcessor implementationProcessor;
    private final LoaderHelper loaderHelper;

    public TimerImplementationLoader(@Reference JavaImplementationProcessor implementationProcessor, @Reference LoaderHelper loaderHelper) {
        this.implementationProcessor = implementationProcessor;
        this.loaderHelper = loaderHelper;
        addAttributes("class","intervalClass","fixedRate","repeatInterval","fireOnce","initialDelay","unit","requires","policySets","poolName");
    }

    public TimerImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        TimerImplementation implementation = new TimerImplementation();
        if (!processImplementationClass(implementation, reader, context)) {
            // an error with the implementation class, return a dummy implementation
            InjectingComponentType type = new InjectingComponentType(null);
            implementation.setComponentType(type);
            return implementation;
        }
        TimerData data = new TimerData();
        implementation.setTimerData(data);

        String poolName = reader.getAttributeValue(null, "poolName");
        if (poolName != null) {
            data.setPoolName(poolName);
        }
        processInitialDelay(data, reader, context);
        processTimeUnit(data, reader, context);
        processIntervalClass(reader, context, data);
        processRepeatInterval(reader, context, data);
        processRepeatFixedRate(reader, context, data);
        processFireOnce(reader, context, data);
        processIntervalMethod(context, implementation);
        validateData(reader, context, data);

        loaderHelper.loadPolicySetsAndIntents(implementation, reader, context);

        InjectingComponentType componentType = implementationProcessor.introspect(implementation.getImplementationClass(), context);
        implementation.setComponentType(componentType);

        LoaderUtil.skipToEndElement(reader);

        return implementation;
    }

    private void processInitialDelay(TimerData data, XMLStreamReader reader, IntrospectionContext context) {
        String initialDelay = reader.getAttributeValue(null, "initialDelay");
        if (initialDelay != null) {
            try {
                data.setInitialDelay(Long.parseLong(initialDelay));
            } catch (NumberFormatException e) {
                InvalidValue failure = new InvalidValue("Invalid initial delay", reader, e);
                context.addError(failure);
            }
        }
    }

    private void processTimeUnit(TimerData data, XMLStreamReader reader, IntrospectionContext context) {
        String units = reader.getAttributeValue(null, "unit");
        if (units != null) {
            try {
                TimeUnit timeUnit = TimeUnit.valueOf(units.toUpperCase());
                data.setTimeUnit(timeUnit);
            } catch (IllegalArgumentException e) {
                InvalidValue failure = new InvalidValue("Invalid time unit: " + units, reader);
                context.addError(failure);
            }
        }
    }

    private void validateData(XMLStreamReader reader, IntrospectionContext context, TimerData data) {
        if (!data.isIntervalMethod()
                && data.getIntervalClass() == null
                && data.getFixedRate() == UNSPECIFIED
                && data.getRepeatInterval() == UNSPECIFIED
                && data.getFireOnce() == UNSPECIFIED) {
            MissingAttribute failure =
                    new MissingAttribute("A task, fixed rate, repeat interval, or time must be specified on the timer component", reader);
            context.addError(failure);
        }
    }

    private boolean processImplementationClass(TimerImplementation implementation, XMLStreamReader reader, IntrospectionContext context)
            throws XMLStreamException {

        String implClass = reader.getAttributeValue(null, "class");
        if (implClass == null) {
            MissingAttribute failure = new MissingAttribute("The class attribute was not specified", reader);
            context.addError(failure);
            LoaderUtil.skipToEndElement(reader);
            return false;
        }
        implementation.setImplementationClass(implClass);
        try {
            Class<?> clazz = context.getClassLoader().loadClass(implClass);
            if (!(Runnable.class.isAssignableFrom(clazz))) {
                InvalidTimerInterface failure = new InvalidTimerInterface(implementation);
                context.addError(failure);
                LoaderUtil.skipToEndElement(reader);
                return false;

            }
        } catch (ClassNotFoundException e) {
            ImplementationArtifactNotFound failure = new ImplementationArtifactNotFound(implClass, e.getMessage());
            context.addError(failure);
            LoaderUtil.skipToEndElement(reader);
            return false;
        }
        return true;
    }

    private void processIntervalClass(XMLStreamReader reader, IntrospectionContext context, TimerData data) throws XMLStreamException {
        String intervalClass = reader.getAttributeValue(null, "intervalClass");
        if (intervalClass == null) {
            // no task defined
            return;
        }
        try {
            data.setType(TimerType.RECURRING);
            Class<?> clazz = context.getClassLoader().loadClass(intervalClass);
            try {
                clazz.getMethod("nextInterval");
            } catch (NoSuchMethodException e) {
                InvalidIntervalClass failure = new InvalidIntervalClass(intervalClass);
                context.addError(failure);
            }
        } catch (ClassNotFoundException e) {
            ImplementationArtifactNotFound failure = new ImplementationArtifactNotFound(intervalClass, e.getMessage());
            context.addError(failure);
        }

        data.setIntervalClass(intervalClass);
    }

    private void processIntervalMethod(IntrospectionContext context, TimerImplementation implementation) throws XMLStreamException {
        try {
            String name = implementation.getImplementationClass();
            Class<?> clazz = context.getClassLoader().loadClass(name);
            Method intervalMethod = clazz.getMethod("nextInterval");
            Class<?> type = intervalMethod.getReturnType();
            TimerData data = implementation.getTimerData();
            data.setIntervalMethod(true);  // set regardless of whether the method is valid
            data.setType(TimerType.RECURRING);
            if (!Long.class.equals(type) && !Long.TYPE.equals(type)) {
                InvalidIntervalMethod failure = new InvalidIntervalMethod("The nextInterval method must return a long value: " + name);
                context.addError(failure);
            }
        } catch (ClassNotFoundException e) {
            // this should not happen as the impl class should already be loaded
        } catch (NoSuchMethodException e) {
            // do nothing, the class does not define an interval method
        }
    }

    private void processRepeatInterval(XMLStreamReader reader, IntrospectionContext introspectionContext, TimerData data) {
        String repeatInterval = reader.getAttributeValue(null, "repeatInterval");
        if (repeatInterval != null) {
            if (data.getIntervalClass() != null) {
                InvalidTimerExpression failure = new InvalidTimerExpression("A task and repeat interval are both specified", reader);
                introspectionContext.addError(failure);
            }
            try {
                long repeat = Long.parseLong(repeatInterval);
                data.setType(TimerType.INTERVAL);
                data.setRepeatInterval(repeat);
            } catch (NumberFormatException e) {
                InvalidTimerExpression failure =
                        new InvalidTimerExpression("Repeat interval is invalid: " + repeatInterval, reader, e);
                introspectionContext.addError(failure);
            }
        }
    }

    private void processRepeatFixedRate(XMLStreamReader reader, IntrospectionContext introspectionContext, TimerData data) {
        String fixedRate = reader.getAttributeValue(null, "fixedRate");
        if (fixedRate != null) {
            if (data.getIntervalClass() != null) {
                InvalidTimerExpression failure = new InvalidTimerExpression("A task and fixed rate are both specified", reader);
                introspectionContext.addError(failure);
            }
            if (data.getRepeatInterval() != UNSPECIFIED) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Repeat interval and fixed rate are both specified", reader);
                introspectionContext.addError(failure);
            }
            try {
                long rate = Long.parseLong(fixedRate);
                data.setType(TimerType.FIXED_RATE);
                data.setFixedRate(rate);
            } catch (NumberFormatException e) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Fixed rate interval is invalid: " + fixedRate, reader, e);
                introspectionContext.addError(failure);
            }
        }
    }

    private void processFireOnce(XMLStreamReader reader, IntrospectionContext introspectionContext, TimerData data) {
        String time = reader.getAttributeValue(null, "fireOnce");
        if (time != null) {
            if (data.getIntervalClass() != null) {
                InvalidTimerExpression failure = new InvalidTimerExpression("A task and fire once are both specified", reader);
                introspectionContext.addError(failure);
            }
            if (data.getRepeatInterval() != UNSPECIFIED) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Repeat interval and fire once are both specified", reader);
                introspectionContext.addError(failure);
            }
            if (data.getFixedRate() != UNSPECIFIED) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Fixed rate and fire once are both specified", reader);
                introspectionContext.addError(failure);
            }
            try {
                long rate = Long.parseLong(time);
                data.setType(TimerType.ONCE);
                data.setFireOnce(rate);
            } catch (NumberFormatException e) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Fire once time is invalid: " + time, reader, e);
                introspectionContext.addError(failure);
            }
        }
    }

}