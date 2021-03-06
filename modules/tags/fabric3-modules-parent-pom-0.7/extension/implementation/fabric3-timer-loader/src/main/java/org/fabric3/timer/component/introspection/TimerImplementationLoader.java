/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.timer.component.introspection;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.java.introspection.ImplementationArtifactNotFound;
import org.fabric3.java.introspection.JavaImplementationProcessor;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.timer.component.provision.TriggerData;
import static org.fabric3.timer.component.provision.TriggerData.UNSPECIFIED;
import org.fabric3.timer.component.provision.TriggerType;
import org.fabric3.timer.component.scdl.TimerImplementation;

/**
 * Loads <implementation.timer> entries in a composite.
 */
public class TimerImplementationLoader implements TypeLoader<TimerImplementation> {
    private static final Map<String, String> ATTRIBUTES = new HashMap<String, String>();

    static {
        ATTRIBUTES.put("class", "class");
        ATTRIBUTES.put("cronExpression", "cronExpression");
        ATTRIBUTES.put("fixedRate", "fixedRate");
        ATTRIBUTES.put("repeatInterval", "repeatInterval");
        ATTRIBUTES.put("fireOnce", "fireOnce");
        ATTRIBUTES.put("requires", "requires");
        ATTRIBUTES.put("policySets", "policySets");
    }

    private final JavaImplementationProcessor implementationProcessor;
    private final LoaderHelper loaderHelper;


    public TimerImplementationLoader(@Reference JavaImplementationProcessor implementationProcessor, @Reference LoaderHelper loaderHelper) {
        this.implementationProcessor = implementationProcessor;
        this.loaderHelper = loaderHelper;
    }


    public TimerImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        assert TimerImplementation.IMPLEMENTATION_TIMER.equals(reader.getName());

        validateAttributes(reader, context);
        TimerImplementation implementation = new TimerImplementation();
        if (!processImplementationClass(implementation, reader, context)) {
            // an error with the implementation class, return a dummy implementation
            PojoComponentType type = new PojoComponentType();
            implementation.setComponentType(type);
            return implementation;
        }
        TriggerData data = new TriggerData();
        implementation.setTriggerData(data);
        processCronExpression(reader, context, data);
        processRepeatInterval(reader, context, data);
        processRepeatFixedRate(reader, context, data);
        processFireOnce(reader, context, data);
        validateFireData(reader, context, data);
        loaderHelper.loadPolicySetsAndIntents(implementation, reader, context);

        LoaderUtil.skipToEndElement(reader);

        implementationProcessor.introspect(implementation, context);
        return implementation;
    }

    private void validateFireData(XMLStreamReader reader, IntrospectionContext context, TriggerData data) {
        if (data.getCronExpression() == null
                && data.getFixedRate() == UNSPECIFIED
                && data.getRepeatInterval() == UNSPECIFIED
                && data.getFireOnce() == UNSPECIFIED) {
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
            ImplementationArtifactNotFound failure = new ImplementationArtifactNotFound(implClass, e.getMessage());
            context.addError(failure);
            LoaderUtil.skipToEndElement(reader);
            return false;
        }
        return true;
    }

    private void processCronExpression(XMLStreamReader reader, IntrospectionContext introspectionContext, TriggerData data) {
        String cronExpression = reader.getAttributeValue(null, "cronExpression");
        if (cronExpression != null) {
            try {
                new CronExpression(cronExpression);
                data.setType(TriggerType.CRON);
                data.setCronExpression(cronExpression);
            } catch (ParseException e) {
                InvalidTimerExpression failure =
                        new InvalidTimerExpression("Cron expression is invalid: " + cronExpression, cronExpression, reader, e);
                introspectionContext.addError(failure);
            }
        }
    }

    private void processRepeatInterval(XMLStreamReader reader, IntrospectionContext introspectionContext, TriggerData data) {
        String repeatInterval = reader.getAttributeValue(null, "repeatInterval");
        if (repeatInterval != null) {
            if (data.getCronExpression() != null) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Cron expression and repeat interval both specified", reader);
                introspectionContext.addError(failure);
            }
            try {
                long repeat = Long.parseLong(repeatInterval);
                data.setType(TriggerType.INTERVAL);
                data.setRepeatInterval(repeat);
            } catch (NumberFormatException e) {
                InvalidTimerExpression failure =
                        new InvalidTimerExpression("Repeat interval is invalid: " + repeatInterval, repeatInterval, reader, e);
                introspectionContext.addError(failure);
            }
        }
    }

    private void processRepeatFixedRate(XMLStreamReader reader, IntrospectionContext introspectionContext, TriggerData data) {
        String fixedRate = reader.getAttributeValue(null, "fixedRate");
        if (fixedRate != null) {
            if (data.getCronExpression() != null) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Cron expression and fixed rate both specified", reader);
                introspectionContext.addError(failure);
            }
            if (data.getRepeatInterval() != UNSPECIFIED) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Repeat interval and fixed rate both specified", reader);
                introspectionContext.addError(failure);
            }
            try {
                long rate = Long.parseLong(fixedRate);
                data.setType(TriggerType.FIXED_RATE);
                data.setFixedRate(rate);
            } catch (NumberFormatException e) {
                InvalidTimerExpression failure =
                        new InvalidTimerExpression("Fixed rate interval is invalid: " + fixedRate, fixedRate, reader, e);
                introspectionContext.addError(failure);
            }
        }
    }

    private void processFireOnce(XMLStreamReader reader, IntrospectionContext introspectionContext, TriggerData data) {
        String time = reader.getAttributeValue(null, "fireOnce");
        if (time != null) {
            if (data.getCronExpression() != null) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Cron expression and fire once both specified", reader);
                introspectionContext.addError(failure);
            }
            if (data.getRepeatInterval() != UNSPECIFIED) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Repeat interval and fire once both specified", reader);
                introspectionContext.addError(failure);
            }
            if (data.getFixedRate() != UNSPECIFIED) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Ficed rate and fire once both specified", reader);
                introspectionContext.addError(failure);
            }
            try {
                long rate = Long.parseLong(time);
                data.setType(TriggerType.ONCE);
                data.setFireOnce(rate);
            } catch (NumberFormatException e) {
                InvalidTimerExpression failure =
                        new InvalidTimerExpression("Fire once time is invalid: " + time, time, reader, e);
                introspectionContext.addError(failure);
            }
        }
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!ATTRIBUTES.containsKey(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

}