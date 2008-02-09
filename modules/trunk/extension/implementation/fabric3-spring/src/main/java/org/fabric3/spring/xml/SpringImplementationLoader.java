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

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.Constants;
import org.osoa.sca.annotations.Reference;

import javax.xml.stream.XMLInputFactory;

import org.fabric3.pojo.processor.IntrospectionRegistry;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.JavaMappedReference;
import org.fabric3.pojo.scdl.JavaMappedService;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.PolicyHelper;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.loader.MissingResourceException;
import org.fabric3.spring.SpringComponentType;
import org.fabric3.spring.SpringImplementation;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

public class SpringImplementationLoader implements StAXElementLoader<SpringImplementation> {
    private static final String SPRING_NS = "http://www.springframework.org/schema/beans";
    private static final QName SERVICE_ELEMENT = new QName(Constants.SCA_NS, "service");
    private static final QName REFERENCE_ELEMENT = new QName(Constants.SCA_NS, "reference");
    private static final QName SCAPROPERTY_ELEMENT = new QName(Constants.SCA_NS, "property");
    private static final QName BEANS_ELEMENT = new QName("beans");
    private static final QName BEAN_ELEMENT = new QName("bean");
    private static final QName PROPERTY_ELEMENT = new QName(SPRING_NS, "property");
    private static final String APPLICATION_CONTEXT = "application-context.xml";

    private final SpringComponentTypeLoader componentTypeLoader;
    private final PolicyHelper policyHelper;
    private final IntrospectionRegistry introspector;

    private boolean debug = false;

    public SpringImplementationLoader(@Reference SpringComponentTypeLoader componentTypeLoader,
                                      @Reference PolicyHelper policyHelper,
                                      @Reference IntrospectionRegistry introspector) {
        this.componentTypeLoader = componentTypeLoader;
        this.policyHelper = policyHelper;
        this.introspector = introspector;
    }


    public SpringImplementation load(XMLStreamReader reader, IntrospectionContext introspectionContext)
            throws XMLStreamException, LoaderException {

        assert SpringImplementation.IMPLEMENTATION_SPRING.equals(reader.getName());

        SpringImplementation implementation = new SpringImplementation();
        SpringComponentType springComponentType = new SpringComponentType();
        implementation.setComponentType(springComponentType);
        
        
        String location = reader.getAttributeValue(null, "location");
        if (location == null) {
          throw new MissingResourceException("implementation.spring does not have required attribute 'location'");
        }
        
        if (debug)
            System.out.println("####################location=" + location);

        loadSpringAppContextXML(location, implementation, introspectionContext);
        

        policyHelper.loadPolicySetsAndIntents(implementation, reader);
        LoaderUtil.skipToEndElement(reader);

        implementation.setLocation(location);
        componentTypeLoader.load(implementation, introspectionContext);
        return implementation;

    }

    private void loadSpringAppContextXML(String location, SpringImplementation implementation, IntrospectionContext introspectionContext)
            throws LoaderException {

        Resource ac = getApplicationContextResource(location);
        implementation.setResource(ac);
        
        if (debug)
            System.out.println("####################ac=" + ac);

        XMLStreamReader reader;

        SpringBeanElement bean = null;
        List<SpringBeanElement> beans = new ArrayList<SpringBeanElement>();

        try {
            XMLInputFactory xmlFactory =  null;
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
            throw new LoaderException(e);
        } catch (XMLStreamException e) {
            throw new LoaderException(e);
        }
        
        generateSpringComponentType(beans, implementation, introspectionContext);
    }
    
    protected void generateSpringComponentType(List<SpringBeanElement> beanElements, SpringImplementation implementation, IntrospectionContext introspectionContext) {
        SpringComponentType springComponentType = implementation.getComponentType();
        
        // don't need this if explicit service is declared, not DONE
        
        // add all beans to service right now, maybe we can limit this to only
        // the beans declared as service
        for (SpringBeanElement beanElement : beanElements) {
            Class<?> implClass;
            try {
                implClass = LoaderUtil.loadClass(beanElement.getClassName(), introspectionContext.getTargetClassLoader());
                PojoComponentType pojoComponentType = new PojoComponentType(implClass.getName());
                introspector.introspect(implClass, pojoComponentType, introspectionContext);
                springComponentType.getServices().putAll(pojoComponentType.getServices());
                
                // TODO work around: Use @Reference in spring bean to create a reference for now
                // Don't need @Reference in spring bean to get a reference
                // <property ... ref="..."> should trigger a reference creation
                springComponentType.getReferences().putAll(pojoComponentType.getReferences());
                for (Map.Entry<String, JavaMappedReference> entry : pojoComponentType.getReferences().entrySet()) {
                    for (Field f : implClass.getDeclaredFields()) {
                        if (f.getName().equals(entry.getKey())) {
                            implementation.addRefNameToFieldType(entry.getKey(), f.getType());
                        }
                    }
                }
                for (JavaMappedService javaMapppdService : pojoComponentType.getServices().values()) {
                    implementation.addServiceNameToBeanId(javaMapppdService.getName(), beanElement.getId());
                }
            } catch (MissingResourceException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ProcessingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        implementation.setComponentType(springComponentType);
    }

    protected Resource getApplicationContextResource(String location)
            throws LoaderException {

        File locationFile = new File(location);

        if (!locationFile.exists()) {
            throw new MissingResourceException("File or directory " + location
                    + " doesn't exist.");
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
                throw new MissingResourceException("Error reading file " + location, e);
            }
            
        } else if (locationFile.isDirectory()) {
            try {
                File mfFile = new File(locationFile, "META-INF/MANIFEST.MF");
                if (mfFile.exists()) {
                Manifest mf;
                    try {
                        mf = new Manifest(new FileInputStream(mfFile));
                    } catch (IOException e) {
                        throw new MissingResourceException("Error reading file " + location + "META-INF/MANIFEST.MF", e);
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
                throw new MissingResourceException("Error path cannot be parsed as a URL", e);
            }
            
        } else {
            throw new MissingResourceException("Specified location '" + location
                + "' for implementation.spring is not a file or a directory.");
        }

        return null;
    }

}
