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
package org.fabric3.fabric.deployer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;
import org.osoa.sca.annotations.Constructor;

import org.fabric3.fabric.builder.Connector;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.builder.resource.ResourceContainerBuilderRegistry;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.ComponentManager;
import org.fabric3.spi.component.RegistrationException;
import org.fabric3.spi.marshaller.MarshallerRegistry;
import org.fabric3.spi.model.physical.PhysicalChangeSet;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalResourceContainerDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.services.messaging.MessagingService;
import org.fabric3.spi.services.messaging.RequestListener;

/**
 * Deploys components in response to asynchronous messages from the Assembly.
 *
 * @version $Revision$ $Date$
 */
@Service(Deployer.class)
@EagerInit
public class DeployerImpl implements RequestListener, Deployer {

    /**
     * Marshaller registry.
     */
    private MarshallerRegistry marshallerRegistry;

    /**
     * Physical component builder registry.
     */
    private ComponentBuilderRegistry componentBuilderRegistry;

    /**
     * Resource builder registry.
     */
    private ResourceContainerBuilderRegistry resourceBuilderRegistry;

    /**
     * Component manager.
     */
    private ComponentManager componentManager;

    /**
     * Connector.
     */
    private Connector connector;

    /**
     * Sink for monitor events
     */
    private DeployerMonitor monitor;


    @Constructor
    public DeployerImpl(@Reference MonitorFactory factory) {
        monitor = factory.getMonitor(DeployerMonitor.class);
    }

    public DeployerImpl(DeployerMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Deploys the component.
     *
     * @param content SCDL content.
     * @return Response to the request message.
     *         <p/>
     *         TODO Handle response messages.
     */
    public XMLStreamReader onRequest(XMLStreamReader content) {

        try {
            final PhysicalChangeSet changeSet = (PhysicalChangeSet) marshallerRegistry.unmarshall(content);
            applyChangeSet(changeSet);
        } catch (Throwable ex) {
            monitor.error("Demarshalling receiving changeset", ex);
            return null;
        }

        return null;
    }

    public void applyChangeSet(PhysicalChangeSet changeSet) throws BuilderException, RegistrationException {
        monitor.receivedChangeSet("Applying changeset");
        Set<PhysicalComponentDefinition> componentDefinitions = changeSet.getComponentDefinitions();
        List<Component> components = new ArrayList<Component>(componentDefinitions.size());
        for (PhysicalResourceContainerDefinition definition : changeSet.getAllResourceDefinitions()) {
            resourceBuilderRegistry.build(definition);
            monitor.provisionResource("Provisioned resource", definition.getUri().toString());
        }
        for (PhysicalComponentDefinition pcd : componentDefinitions) {
            final Component component = componentBuilderRegistry.build(pcd);
            components.add(component);
        }
        for (Component component : components) {
            componentManager.register(component);
        }
        for (PhysicalWireDefinition pwd : changeSet.getWireDefinitions()) {
            connector.connect(pwd);
        }
        for (Component component : components) {
            component.start();
            monitor.startComponent("Started component", component.getUri().toString());
        }
    }

    /**
     * Injects the messaging service.
     *
     * @param messagingService messaging service to be injected.
     */
    @Reference
    public void setMessagingService(MessagingService messagingService) {
        QName qName = new QName(PhysicalChangeSet.class.getName());
        messagingService.registerRequestListener(qName, this);
    }

    /**
     * Injects the model marshaller registry.
     *
     * @param marshallerRegistry Marshaller registry.
     */
    @Reference
    public void setMarshallerRegistry(MarshallerRegistry marshallerRegistry) {
        this.marshallerRegistry = marshallerRegistry;
    }

    /**
     * Injects the builder registry.
     *
     * @param builderRegistry Builder registry.
     */
    @Reference
    public void setBuilderRegistry(ComponentBuilderRegistry builderRegistry) {
        this.componentBuilderRegistry = builderRegistry;
    }


    @Reference
    public void setResourceBuilderRegistry(ResourceContainerBuilderRegistry registry) {
        this.resourceBuilderRegistry = registry;
    }

    /**
     * Injects the component manager.
     *
     * @param componentManager Component manager.
     */
    @Reference
    public void setComponentManager(ComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    /**
     * Injects the connector.
     *
     * @param connector Connector.
     */
    @Reference
    public void setConnector(Connector connector) {
        this.connector = connector;
    }

}
