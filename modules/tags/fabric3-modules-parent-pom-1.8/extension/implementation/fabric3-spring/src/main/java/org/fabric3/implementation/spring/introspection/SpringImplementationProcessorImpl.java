/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.implementation.spring.introspection;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.Constants;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.stream.Source;
import org.fabric3.implementation.spring.model.BeanDefinition;
import org.fabric3.implementation.spring.model.SpringComponentType;
import org.fabric3.implementation.spring.model.SpringConsumer;
import org.fabric3.implementation.spring.model.SpringService;
import org.fabric3.model.type.component.ConsumerDefinition;
import org.fabric3.model.type.component.ProducerDefinition;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.xml.XMLFactory;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * Default SpringImplementationProcessor implementation.
 *
 * @version $Rev$ $Date$
 */
public class SpringImplementationProcessorImpl implements SpringImplementationProcessor {
    private static final String BEAN = "bean";
    private static final QName SERVICE = new QName(Constants.SCA_NS, "service");
    private static final QName REFERENCE = new QName(Constants.SCA_NS, "reference");
    private static final QName PROPERTY = new QName(Constants.SCA_NS, "property");
    private static final QName PRODUCER = new QName(Constants.SCA_NS, "producer");
    private static final QName CONSUMER = new QName(Constants.SCA_NS, "consumer");

    private JavaContractProcessor contractProcessor;
    private XMLFactory factory;

    public SpringImplementationProcessorImpl(@Reference JavaContractProcessor contractProcessor, @Reference XMLFactory factory) {
        this.contractProcessor = contractProcessor;
        this.factory = factory;
    }

    public SpringComponentType introspect(Source source, IntrospectionContext context) throws XMLStreamException {
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            SpringComponentType type = new SpringComponentType();
            stream = source.openStream();
            reader = factory.newInputFactoryInstance().createXMLStreamReader(stream);
            while (true) {
                switch (reader.next()) {
                case START_ELEMENT:
                    if (BEAN.equals(reader.getName().getLocalPart())) {
                        if (!processBean(type, reader, context)) {
                            return type;
                        }
                    } else if (SERVICE.equals(reader.getName())) {
                        if (!processService(type, reader, context)) {
                            return type;
                        }
                    } else if (REFERENCE.equals(reader.getName())) {
                        if (!processReference(type, reader, context)) {
                            return type;
                        }
                    } else if (PROPERTY.equals(reader.getName())) {
                        if (!processProperty(type, reader, context)) {
                            return type;
                        }
                    } else if (PRODUCER.equals(reader.getName())) {
                        if (!processProducer(type, reader, context)) {
                            return type;
                        }
                    } else if (CONSUMER.equals(reader.getName())) {
                        if (!processConsumer(type, reader, context)) {
                            return type;
                        }
                    }
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    postProcess(type, context);
                    return type;
                }
            }

        } catch (IOException e) {
            throw new XMLStreamException(e);
        } finally {
            if (reader != null) {
                reader.close();
            }
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                // ignore
                e.printStackTrace();
            }
        }
    }

    /**
     * Processes a Spring <code>bean</code> definition.
     *
     * @param type    the component type
     * @param reader  the reader
     * @param context the context for reporting errors
     * @return true if processing completed without validation errors
     */
    private boolean processBean(SpringComponentType type, XMLStreamReader reader, IntrospectionContext context) {
        String id = reader.getAttributeValue(null, "id");
        String name = reader.getAttributeValue(null, "name");
        if (id == null && name == null) {
            MissingAttribute failure = new MissingAttribute("A bean id or name must be specified", reader);
            context.addError(failure);
            return false;
        }
        String classAttr = reader.getAttributeValue(null, "class");
        Class<?> clazz = null;
        if (classAttr != null) {
            try {
                clazz = context.getClassLoader().loadClass(classAttr);
            } catch (ClassNotFoundException e) {
                InvalidValue failure = new InvalidValue("Bean class not found: " + classAttr, reader, e);
                context.addError(failure);
            }
        }
        BeanDefinition bean = new BeanDefinition();
        bean.setId(id);
        bean.setName(name);
        bean.setBeanClass(clazz);
        type.add(bean);

        return true;
    }

    /**
     * Processes an SCA <code>service</code> element.
     *
     * @param type    the component type
     * @param reader  the reader
     * @param context the context for reporting errors
     * @return true if processing completed without validation errors
     */
    private boolean processService(SpringComponentType type, XMLStreamReader reader, IntrospectionContext context) {
        // TODO This does not currently support policy declarations
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("A service name must be specified", reader);
            context.addError(failure);
            return false;
        }
        if (type.getServices().containsKey(name)) {
            DuplicateService failure = new DuplicateService(name, reader);
            context.addError(failure);
            return false;
        }
        String target = reader.getAttributeValue(null, "target");
        if (target == null) {
            MissingAttribute failure = new MissingAttribute("A service target must be specified", reader);
            context.addError(failure);
            return false;
        }
        String typeAttr = reader.getAttributeValue(null, "type");
        ServiceContract contract = null;
        if (typeAttr != null) {
            Class<?> interfaze;
            try {
                ClassLoader loader = context.getClassLoader();
                interfaze = loader.loadClass(typeAttr);
            } catch (ClassNotFoundException e) {
                InvalidValue failure = new InvalidValue("Service interface not found: " + typeAttr, reader);
                context.addError(failure);
                return false;
            }
            contract = contractProcessor.introspect(interfaze, context);
        }
        SpringService definition = new SpringService(name, contract, target);
        type.add(definition);
        return true;
    }

    /**
     * Processes an SCA <code>reference</code> element.
     *
     * @param type    the component type
     * @param reader  the reader
     * @param context the context for reporting errors
     * @return true if processing completed without validation errors
     */
    private boolean processReference(SpringComponentType type, XMLStreamReader reader, IntrospectionContext context) {
        // TODO This does not currently support policy declarations
        // TODO This does not currently support the @default attribute
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("A reference name must be specified", reader);
            context.addError(failure);
            return false;
        }
        if (type.getReferences().containsKey(name)) {
            DuplicateReference failure = new DuplicateReference(name, reader);
            context.addError(failure);
            return false;
        }
        String typeAttr = reader.getAttributeValue(null, "type");
        if (typeAttr == null) {
            MissingAttribute failure = new MissingAttribute("A service type must be specified", reader);
            context.addError(failure);
            return false;
        }
        Class<?> interfaze;
        try {
            ClassLoader loader = context.getClassLoader();
            interfaze = loader.loadClass(typeAttr);
        } catch (ClassNotFoundException e) {
            InvalidValue failure = new InvalidValue("Service interface not found: " + typeAttr, reader);
            context.addError(failure);
            return false;
        }
        ServiceContract contract = contractProcessor.introspect(interfaze, context);
        ReferenceDefinition definition = new ReferenceDefinition(name, contract);
        type.add(definition);
        return true;
    }

    /**
     * Processes an SCA <code>property</code> element.
     *
     * @param type    the component type
     * @param reader  the reader
     * @param context the context for reporting errors
     * @return true if processing completed without validation errors
     */
    private boolean processProperty(SpringComponentType type, XMLStreamReader reader, IntrospectionContext context) {
        // TODO handle types
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("A property name must be specified", reader);
            context.addError(failure);
            return false;
        }
        if (type.getProperties().containsKey(name)) {
            DuplicateProperty failure = new DuplicateProperty(name, reader);
            context.addError(failure);
            return false;
        }
        Property property = new Property(name);
        type.add(property);
        return true;
    }

    /**
     * Processes an SCA <code>consumer</code> element.
     *
     * @param type    the component type
     * @param reader  the reader
     * @param context the context for reporting errors
     * @return true if processing completed without validation errors
     */
    private <T> boolean processConsumer(SpringComponentType type, XMLStreamReader reader, IntrospectionContext context) {
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("A consumer name must be specified", reader);
            context.addError(failure);
            return false;
        }
        if (type.getConsumers().containsKey(name)) {
            DuplicateConsumer failure = new DuplicateConsumer(name, reader);
            context.addError(failure);
            return false;
        }
        String typeAttr = reader.getAttributeValue(null, "type");
        if (typeAttr == null) {
            MissingAttribute failure = new MissingAttribute("A consumer data type must be specified", reader);
            context.addError(failure);
            return false;
        }
        Class<T> consumerType;
        try {
            ClassLoader loader = context.getClassLoader();
            consumerType = cast(loader.loadClass(typeAttr));
        } catch (ClassNotFoundException e) {
            InvalidValue failure = new InvalidValue("Consumer interface not found: " + typeAttr, reader);
            context.addError(failure);
            return false;
        }
        JavaClass<T> dataType = new JavaClass<T>(consumerType);
        String target = reader.getAttributeValue(null, "target");
        if (target == null) {
            MissingAttribute failure = new MissingAttribute("A consumer target must be specified", reader);
            context.addError(failure);
            return false;
        }
        String[] targetTokens = target.split("/");
        if (targetTokens.length != 2) {
            InvalidValue failure = new InvalidValue("Target value must be in the form beanName/methodName", reader);
            context.addError(failure);
            return false;
        }
        ConsumerDefinition definition = new SpringConsumer(name, dataType, targetTokens[0], targetTokens[1]);
        type.add(definition);
        return true;
    }


    /**
     * Processes an SCA <code>producer</code> element.
     *
     * @param type    the component type
     * @param reader  the reader
     * @param context the context for reporting errors
     * @return true if processing completed without validation errors
     */
    private boolean processProducer(SpringComponentType type, XMLStreamReader reader, IntrospectionContext context) {
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("A producer name must be specified", reader);
            context.addError(failure);
            return false;
        }
        if (type.getConsumers().containsKey(name)) {
            DuplicateConsumer failure = new DuplicateConsumer(name, reader);
            context.addError(failure);
            return false;
        }

        String typeAttr = reader.getAttributeValue(null, "type");
        if (typeAttr == null) {
            MissingAttribute failure = new MissingAttribute("A producer data type must be specified", reader);
            context.addError(failure);
            return false;
        }
        Class<?> interfaze;
        try {
            ClassLoader loader = context.getClassLoader();
            interfaze = loader.loadClass(typeAttr);
        } catch (ClassNotFoundException e) {
            InvalidValue failure = new InvalidValue("Service interface not found: " + typeAttr, reader);
            context.addError(failure);
            return false;
        }
        ServiceContract contract = contractProcessor.introspect(interfaze, context);

        ProducerDefinition definition = new ProducerDefinition(name, contract);
        type.add(definition);
        return true;
    }


    /**
     * Performs heuristic introspection and validation.
     *
     * @param type    the component type
     * @param context the context for reporting errors
     */
    private void postProcess(SpringComponentType type, IntrospectionContext context) {
        if (type.getServices().isEmpty() && type.getReferences().isEmpty() && type.getProperties().isEmpty()) {
            processHueristics(type, context);
            return;
        }
        // introspect service contracts for service elements that do not explicitly have a type element
        postProcessServices(type, context);
    }

    /**
     * Performs heuristic introspection.
     *
     * @param type    the component type
     * @param context the context for reporting errors
     */
    private void processHueristics(SpringComponentType type, IntrospectionContext context) {
        // TODO synthesize optional references
        // TODO synthesize services
        // TODO synthesize properties
    }

    /**
     * Performs heuristic introspection and validation of services.
     *
     * @param type    the component type
     * @param context the context for reporting errors
     */
    private void postProcessServices(SpringComponentType type, IntrospectionContext context) {
        for (SpringService service : type.getSpringServices().values()) {
            String target = service.getTarget();
            BeanDefinition definition = type.getBeansById().get(target);
            if (definition == null) {
                definition = type.getBeansByName().get(target);
            }
            if (definition == null) {
                ServiceTargetNotFound failure = new ServiceTargetNotFound(service.getName(), target);
                context.addError(failure);
                continue;
            }
            if (service.getServiceContract() == null) {
                introspectContract(service, definition, context);
            }
        }
    }

    /**
     * Introspects a service contract from a bean definition.
     *
     * @param service    the service
     * @param definition the bean definition
     * @param context    the context for reporting errors
     */
    private void introspectContract(SpringService service, BeanDefinition definition, IntrospectionContext context) {
        Class<?> beanClass = definition.getBeanClass();
        String serviceName = service.getName();
        if (beanClass == null) {
            UnknownServiceType failure = new UnknownServiceType(serviceName);
            context.addError(failure);
            return;
        }
        Class<?>[] interfaces = beanClass.getInterfaces();
        if (interfaces.length == 0) {
            // use the implementation class
            ServiceContract contract = contractProcessor.introspect(beanClass, context);
            service.setServiceContract(contract);
        } else if (interfaces.length == 1) {
            // default service
            ServiceContract contract = contractProcessor.introspect(interfaces[0], context);
            service.setServiceContract(contract);
        } else {
            // match on service name
            ServiceContract contract = null;
            for (Class<?> interfaze : interfaces) {
                if (serviceName.equals(interfaze.getSimpleName())) {
                    contract = contractProcessor.introspect(interfaze, context);
                    service.setServiceContract(contract);
                    break;
                }
            }
            if (contract == null) {
                UnknownServiceType failure = new UnknownServiceType(serviceName);
                context.addError(failure);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    private <T> T cast(Object o) {
        return (T) o;
    }

}
