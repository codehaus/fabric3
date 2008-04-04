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
package org.fabric3.maven.runtime.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.maven.surefire.testset.TestSetFailedException;

import org.fabric3.fabric.runtime.AbstractRuntime;
import org.fabric3.fabric.runtime.ComponentNames;
import static org.fabric3.fabric.runtime.ComponentNames.CONTRIBUTION_SERVICE_URI;
import static org.fabric3.fabric.runtime.ComponentNames.DISTRIBUTED_ASSEMBLY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.XML_FACTORY_URI;
import org.fabric3.fabric.util.FileHelper;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.java.runtime.JavaComponent;
import org.fabric3.maven.contribution.ModuleContributionSource;
import org.fabric3.maven.runtime.MavenEmbeddedRuntime;
import org.fabric3.maven.runtime.MavenHostInfo;
import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.pojo.reflection.InvokerInterceptor;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.assembly.Assembly;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.services.xmlfactory.XMLFactory;

/**
 * Default Maven runtime implementation.
 *
 * @version $Rev$ $Date$
 */
public class MavenEmbeddedRuntimeImpl extends AbstractRuntime<MavenHostInfo> implements MavenEmbeddedRuntime {
    public MavenEmbeddedRuntimeImpl() {
        super(MavenHostInfo.class, null);
    }

    public Composite activate(URL url, QName qName) throws CompositeActivationException {
        try {
            URI contributionUri = URI.create(qName.getLocalPart());
            ModuleContributionSource source =
                    new ModuleContributionSource(contributionUri, FileHelper.toFile(url).toString());
            return activate(source, qName);
        } catch (MalformedURLException e) {
            String identifier = url.toString();
            throw new CompositeActivationException("Invalid project directory: " + identifier, identifier, e);
        }
    }

    public Composite activate(ContributionSource source, QName qName) throws CompositeActivationException {
        try {
            // contribute the Maven project to the application domain
            Assembly assembly = getSystemComponent(Assembly.class, DISTRIBUTED_ASSEMBLY_URI);
            ContributionService contributionService =
                    getSystemComponent(ContributionService.class, CONTRIBUTION_SERVICE_URI);
            contributionService.contribute(source);
            // activate the deployable composite in the domain
            assembly.includeInDomain(qName);
            MetaDataStore store = getSystemComponent(MetaDataStore.class, ComponentNames.METADATA_STORE_URI);
            ResourceElement<?, ?> element = store.resolve(new QNameSymbol(qName));
            assert element != null;
            return (Composite) element.getValue();
        } catch (ContributionException e) {
            throw new CompositeActivationException("Error processing project", e);
        } catch (ActivateException e) {
            String identifier = qName.toString();
            throw new CompositeActivationException("Error activating composite:" + identifier, identifier, e);
        }
    }

    public Composite activate(URL url, URL scdlLocation) throws Exception {
        QName name = parseCompositeQName(scdlLocation);
        return activate(url, name);
    }

    public void startContext(URI groupId) throws GroupInitializationException {
        WorkContext workContext = new WorkContext();
        CallFrame frame = new CallFrame();
        workContext.addCallFrame(frame);
        ScopeRegistry scopeRegistry = getSystemComponent(ScopeRegistry.class, ComponentNames.SCOPE_REGISTRY_URI);
        scopeRegistry.getScopeContainer(Scope.COMPOSITE).startContext(workContext, groupId);
    }

    public void destroy() {
        // destroy system components
        ScopeRegistry scopeRegistry = getSystemComponent(ScopeRegistry.class, ComponentNames.SCOPE_REGISTRY_URI);
        ScopeContainer<?> scopeContainer = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
        WorkContext workContext = new WorkContext();
        CallFrame frame = new CallFrame(null, ComponentNames.RUNTIME_URI);
        workContext.addCallFrame(frame);
        scopeContainer.stopContext(workContext);
    }

    @SuppressWarnings({"unchecked"})
    public void executeTest(URI contextId, String componentName, Operation<?> operation) throws TestSetFailedException {
        WorkContext oldContext = PojoWorkContextTunnel.getThreadWorkContext();
        try {
            WorkContext workContext = new WorkContext();
            CallFrame frame = new CallFrame();
            workContext.addCallFrame(frame);
            URI componentId = URI.create(contextId.toString() + "/" + componentName);

            // FIXME we should not be creating a InvokerInterceptor here
            // FIXME this should create a wire to the JUnit component and invoke the head interceptor on the chain
            JavaComponent component = (JavaComponent) getComponentManager().getComponent(componentId);
            PojoWorkContextTunnel.setThreadWorkContext(workContext);
            Object instance = component.createObjectFactory().getInstance();
            Method m = instance.getClass().getMethod(operation.getName());
            ScopeContainer scopeContainer = component.getScopeContainer();
            InvokerInterceptor<?, ?> interceptor = new InvokerInterceptor(m, false, false, component, scopeContainer);

            Message msg = new MessageImpl();
            msg.setWorkContext(workContext);
            Message response = interceptor.invoke(msg);
            if (response.isFault()) {
                throw new TestSetFailedException(operation.getName(), (Throwable) response.getBody());
            }
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        } catch (ObjectCreationException e) {
            throw new AssertionError(e);
        } finally {
            PojoWorkContextTunnel.setThreadWorkContext(oldContext);
        }
    }

    /**
     * Determines a composite's QName.
     * <p/>
     * This method preserves backward compatibility for specifying SCDL location in an iTest plugin configuration.
     *
     * @param url the SCDL location
     * @return the composite QName
     * @throws IOException        if an error occurs opening the composite file
     * @throws XMLStreamException if an error occurs processing the composite
     */
    private QName parseCompositeQName(URL url) throws IOException, XMLStreamException {
        XMLStreamReader reader = null;
        InputStream stream = null;
        try {
            stream = url.openStream();
            XMLFactory xmlFactory = getSystemComponent(XMLFactory.class, XML_FACTORY_URI);
            reader = xmlFactory.newInputFactoryInstance().createXMLStreamReader(stream);
            reader.nextTag();
            String name = reader.getAttributeValue(null, "name");
            String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
            return new QName(targetNamespace, name);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }

    }

}
