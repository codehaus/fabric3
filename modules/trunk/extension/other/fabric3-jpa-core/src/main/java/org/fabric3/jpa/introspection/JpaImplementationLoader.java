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
package org.fabric3.jpa.introspection;

import java.lang.reflect.Type;
import java.net.URI;

import javax.persistence.EntityManager;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.jpa.ConversationalDaoImpl;
import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.introspection.contract.InvalidServiceContractException;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.java.introspection.JavaImplementationProcessor;
import org.fabric3.java.scdl.JavaImplementation;
import org.fabric3.jpa.scdl.PersistenceUnitResource;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * Implementation loader for JPA component.
 * 
 * @version $Revision$ $Date$
 */
@EagerInit
public class JpaImplementationLoader implements TypeLoader<JavaImplementation> {
    
    public static final QName IMPLEMENTATION_JPA = new QName(Constants.FABRIC3_NS, "implementation.jpa");
    
    private final JavaImplementationProcessor implementationProcessor;
    private final ServiceContract<Type> factoryServiceContract;

    public JpaImplementationLoader(@Reference JavaImplementationProcessor implementationProcessor,
                                   @Reference ContractProcessor contractProcessor) throws InvalidServiceContractException {
        this.implementationProcessor = implementationProcessor;
        factoryServiceContract = contractProcessor.introspect(new TypeMapping(), EntityManager.class);
    }

    /**
     * Creates the instance of the implementation type.
     * 
     * @param reader Stax XML stream reader used for reading data.
     * @param context Introspection context.
     * @return An instance of the JPA implemenation.
     */
    public JavaImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException, LoaderException {
        
        try {
        
            String persistenceUnit = reader.getAttributeValue(null, "persistenceUnit");
            if (persistenceUnit == null) {
                throw new LoaderException("Missing attribute: persistenceUnit");
            }
            
            JavaImplementation implementation = new JavaImplementation();
            implementation.setImplementationClass(ConversationalDaoImpl.class.getName());
            
            URI contributionUri = context.getContributionUri();
            String targetNs = context.getTargetNamespace();
            ClassLoader cl = getClass().getClassLoader();
            
            IntrospectionContext newContext = new DefaultIntrospectionContext(contributionUri, cl, targetNs);
            implementationProcessor.introspect(implementation, newContext);
            PojoComponentType pojoComponentType = implementation.getComponentType();
            
            PersistenceUnitResource resource = new PersistenceUnitResource("unit", persistenceUnit, factoryServiceContract);
            FieldInjectionSite site = new FieldInjectionSite(ConversationalDaoImpl.class.getDeclaredField("entityManager"));
            pojoComponentType.add(resource, site);
            
            return implementation; 
            
        } catch (IntrospectionException e) {
            throw new ProcessingException(e);
        } catch (NoSuchFieldException e) {
            throw new ProcessingException(e);
        }
        
    }

}
