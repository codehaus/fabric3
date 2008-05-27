/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you unde the Apache License, Version 2.0 (the
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
package org.fabric3.spring.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.java.ImplementationNotFoundException;
import org.fabric3.introspection.xml.InvalidValue;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.MissingAttribute;
import org.fabric3.introspection.xml.ResourceNotFound;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.validation.MissingResource;
import org.fabric3.spring.SpringComponentType;
import org.fabric3.spring.SpringImplementation;

public class SpringImplementationLoader implements TypeLoader<SpringImplementation> {
    private static final String SPRING_NS = "http://www.springframework.org/schema/beans";
    private static final QName BEANS_ELEMENT = new QName("beans");
    private static final QName BEAN_ELEMENT = new QName("bean");
    private static final String APPLICATION_CONTEXT = "application-context.xml";

    private final SpringComponentTypeLoader componentTypeLoader;
    private final LoaderHelper loaderHelper;
    private final IntrospectionHelper introspectionHelper;

    private boolean debug = false;

    public SpringImplementationLoader(@Reference SpringComponentTypeLoader componentTypeLoader,
                                      @Reference LoaderHelper loaderHelper,
                                      @Reference IntrospectionHelper introspectionHelper) {
        this.componentTypeLoader = componentTypeLoader;
        this.loaderHelper = loaderHelper;
        this.introspectionHelper = introspectionHelper;
    }


    public SpringImplementation load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {

        assert SpringImplementation.IMPLEMENTATION_SPRING.equals(reader.getName());

        SpringImplementation implementation = new SpringImplementation();
        SpringComponentType springComponentType = new SpringComponentType();
        implementation.setComponentType(springComponentType);


        String location = reader.getAttributeValue(null, "location");
        if (location == null) {
            MissingAttribute failure = new MissingAttribute("Location attribute on implementation.spring must be specified", "location", reader);
            introspectionContext.addError(failure);
            return implementation;
        }

        if (debug)
            System.out.println("####################location=" + location);

        loadSpringAppContextXML(location, implementation, reader, introspectionContext);


        loaderHelper.loadPolicySetsAndIntents(implementation, reader, introspectionContext);
        LoaderUtil.skipToEndElement(reader);

        implementation.setLocation(location);
        try {
            componentTypeLoader.load(implementation, introspectionContext);
        } catch (LoaderException e) {
            InvalidValue failure = new InvalidValue("Error parsing property value", null, reader);
            introspectionContext.addError(failure);
            return null;

        }
        return implementation;

    }

    private void loadSpringAppContextXML(String location,
                                         SpringImplementation implementation,
                                         XMLStreamReader originalReader,
                                         IntrospectionContext introspectionContext) {

        Resource ac = getApplicationContextResource(location, originalReader, introspectionContext);
        implementation.setResource(ac);

        if (debug)
            System.out.println("####################ac=" + ac);

        XMLStreamReader reader;

        SpringBeanElement bean = null;
        List<SpringBeanElement> beans = new ArrayList<SpringBeanElement>();

        try {
            XMLInputFactory xmlFactory = null;
            ClassLoader cl = getClass().getClassLoader();
            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(cl);
                xmlFactory = XMLInputFactory.newInstance();
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }
            reader = xmlFactory.createXMLStreamReader(ac.getInputStream());

            // System.out.println("Starting to read application-context.xml file");

            boolean completed = false;
            while (!completed) {
                switch (reader.next()) {
                case START_ELEMENT:
                    QName qname = reader.getName();
                    /*
                  //System.out.println("Spring TypeLoader - found element with name: " + qname.toString());
                  if (SERVICE_ELEMENT.equals(qname)) {
                      SpringSCAServiceElement service =
                          new SpringSCAServiceElement(reader.getAttributeValue(null, "name"), reader
                              .getAttributeValue(null, "type"), reader.getAttributeValue(null, "target"));
                      services.add(service);
                  } else if (REFERENCE_ELEMENT.equals(qname)) {
                      SpringSCAReferenceElement reference =
                          new SpringSCAReferenceElement(reader.getAttributeValue(null, "name"), reader
                              .getAttributeValue(null, "type"));
                      references.add(reference);
                  } else if (SCAPROPERTY_ELEMENT.equals(qname)) {
                      SpringSCAPropertyElement scaproperty =
                          new SpringSCAPropertyElement(reader.getAttributeValue(null, "name"), reader
                              .getAttributeValue(null, "type"));
                      scaproperties.add(scaproperty);
                  } else */
                    if (BEAN_ELEMENT.equals(qname)) {
                        // TODO FIX THIS !!
                        //FIXME count is never used
                        //int count = reader.getAttributeCount();
                        bean =
                                new SpringBeanElement(reader.getAttributeValue(null, "id"), reader
                                        .getAttributeValue(null, "class"));
                        beans.add(bean);
                        /*
                        } else if (PROPERTY_ELEMENT.equals(qname)) {
                            SpringPropertyElement property =
                                new SpringPropertyElement(reader.getAttributeValue(null, "name"), reader
                                    .getAttributeValue(null, "ref"));
                            bean.addProperty(property);
                        */
                    }
                    break;
                case END_ELEMENT:
                    if (BEANS_ELEMENT.equals(reader.getName())) {
                        completed = true;
                        break;
                    }
                }
            }

        } catch (IOException e) {
            InvalidApplicationContextFile failure =
                    new InvalidApplicationContextFile("Error loading application context: " + location, location, e, originalReader);
            introspectionContext.addError(failure);
            return;
        } catch (XMLStreamException e) {
            InvalidApplicationContextFile failure =
                    new InvalidApplicationContextFile("Error loading application context: " + location, location, e, originalReader);
            introspectionContext.addError(failure);
            return;
        }

        generateSpringComponentType(beans, implementation, reader, introspectionContext);
    }

    protected void generateSpringComponentType(List<SpringBeanElement> beanElements,
                                               SpringImplementation implementation,
                                               XMLStreamReader reader,
                                               IntrospectionContext introspectionContext) {
        SpringComponentType springComponentType = implementation.getComponentType();

        // don't need this if explicit service is declared, not DONE

        // add all beans to service right now, maybe we can limit this to only
        // the beans declared as service
        for (SpringBeanElement beanElement : beanElements) {
            Class<?> implClass;
            try {
                implClass = introspectionHelper.loadClass(beanElement.getClassName(), introspectionContext.getTargetClassLoader());
            } catch (ImplementationNotFoundException e) {
                String bean = beanElement.getClassName();
                ResourceNotFound failure = new ResourceNotFound("Bean class not found: " + bean, bean, reader);
                introspectionContext.addError(failure);
                return;
            }

            PojoComponentType pojoComponentType = new PojoComponentType(implClass.getName());
//            introspector.introspect(implClass, pojoComponentType, introspectionContext);
            springComponentType.getServices().putAll(pojoComponentType.getServices());

            // TODO work around: Use @Reference in spring bean to create a reference for now
            // Don't need @Reference in spring bean to get a reference
            // <property ... ref="..."> should trigger a reference creation
            springComponentType.getReferences().putAll(pojoComponentType.getReferences());
            for (Map.Entry<String, ReferenceDefinition> entry : pojoComponentType.getReferences().entrySet()) {
                for (Field f : implClass.getDeclaredFields()) {
                    if (f.getName().equals(entry.getKey())) {
                        implementation.addRefNameToFieldType(entry.getKey(), f.getType());
                    }
                }
            }
            for (ServiceDefinition javaMapppdService : pojoComponentType.getServices().values()) {
                implementation.addServiceNameToBeanId(javaMapppdService.getName(), beanElement.getId());
            }

        }

        implementation.setComponentType(springComponentType);
    }

    protected Resource getApplicationContextResource(String location, XMLStreamReader reader, IntrospectionContext introspectionContext) {

        File locationFile = new File(location);

        if (!locationFile.exists()) {
            MissingResource failure = new MissingResource("File or directory " + location + " does not exist", location);
            introspectionContext.addError(failure);
            return null;
        }

        if (locationFile.isFile()) {
            try {
                JarFile jf = new JarFile(locationFile);
                JarEntry je = null;

                Manifest mf = jf.getManifest();
                if (mf != null) {
                    Attributes attributes = mf.getMainAttributes();
                    String path = attributes.getValue("Spring-Context");

                    // FIXME all path;path, not just path/applicaton-context.xml
                    if (path != null) {
                        je = jf.getJarEntry(path + "/" + APPLICATION_CONTEXT);
                        if (je != null) {
                            return new UrlResource(new URL("jar:" + locationFile.toURL() + "!/" + path + "/" + APPLICATION_CONTEXT));
                        }
                    }
                }

                // FIXME all *.xml, not just applicaton-context.xml
                // no manifest or Spring-Context specified, build an application context
                // using all the *.xml files in the METAINF/spring directory
                je = jf.getJarEntry("META-INF/spring/" + APPLICATION_CONTEXT);
                if (je != null) {
                    return new UrlResource(new URL("jar:" + locationFile.toURI().toURL() + "!/META-INF/spring/" + APPLICATION_CONTEXT));
                }

            } catch (IOException e) {
                InvalidApplicationContextFile failure = new InvalidApplicationContextFile("Error reading file: " + location, location, e, reader);
                introspectionContext.addError(failure);
                return null;
            }

        } else if (locationFile.isDirectory()) {
            try {
                File mfFile = new File(locationFile, "META-INF/MANIFEST.MF");
                if (mfFile.exists()) {
                    Manifest mf;
                    try {
                        mf = new Manifest(new FileInputStream(mfFile));
                    } catch (IOException e) {
                        String id = location + "META-INF/MANIFEST.MF";
                        InvalidApplicationContextFile failure = new InvalidApplicationContextFile("Error reading file: " + id, id, e, reader);
                        introspectionContext.addError(failure);
                        return null;
                    }

                    Attributes attributes = mf.getMainAttributes();
                    String path = attributes.getValue("Spring-Context");
                    if (path != null) {
                        // FIXME all path;path, not just path/applicaton-context.xml
                        File acFile = new File(locationFile, path + "/" + APPLICATION_CONTEXT);
                        if (acFile != null) {
                            return new UrlResource(acFile.toURL());
                        }
                    }
                }

                // FIXME all *.xml, not just applicaton-context.xml
                // no manifest or Spring-Context specified, build an application context
                // using all the *.xml files in the METAINF/spring directory
                File acFile = new File(locationFile, "META-INF/spring/" + APPLICATION_CONTEXT);
                if (acFile.exists()) {
                    return new UrlResource(acFile.toURL());
                }
            } catch (MalformedURLException e) {
                String id = locationFile + "/META-INF/spring/" + APPLICATION_CONTEXT;
                InvalidApplicationContextFile failure = new InvalidApplicationContextFile("Error reading file: " + id, id, e, reader);
                introspectionContext.addError(failure);
                return null;
            }

        } else {
            InvalidApplicationContextFile failure =
                    new InvalidApplicationContextFile("Specified location is not a file or directory: " + location, location, reader);
            introspectionContext.addError(failure);
            return null;
        }

        return null;
    }

}
