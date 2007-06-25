<%@ page import="java.io.StringReader" %>
<%@ page import="java.net.URI" %>
<%@ page import="javax.xml.stream.XMLInputFactory" %>
<%@ page import="javax.xml.stream.XMLStreamReader" %>
<%@ page import="org.fabric3.extension.component.SimpleWorkContext" %>
<%@ page import="org.fabric3.fabric.assembly.DistributedAssembly" %>
<%@ page import="static org.fabric3.fabric.runtime.ComponentNames.LOADER_URI" %>
<%@ page import="org.fabric3.fabric.loader.LoaderContextImpl" %>
<%@ page import="org.fabric3.fabric.runtime.ComponentNames" %>
<%@ page import="org.fabric3.runtime.webapp.WebappRuntimeImpl" %>
<%@ page import="org.fabric3.spi.component.ScopeContainer" %>
<%@ page import="org.fabric3.spi.component.ScopeRegistry" %>
<%@ page import="org.fabric3.spi.component.WorkContext" %>
<%@ page import="static org.fabric3.fabric.runtime.ComponentNames.DISTRIBUTED_ASSEMBLY_URI" %>
<%@ page import="org.fabric3.spi.loader.LoaderContext" %>
<%@ page import="org.fabric3.spi.loader.LoaderRegistry" %>
<%@ page import="org.fabric3.spi.model.type.ComponentDefinition" %>
<%@ page import="org.fabric3.spi.model.type.CompositeComponentType" %>
<%@ page import="org.fabric3.spi.model.type.CompositeImplementation" %>
<%@ page import="org.fabric3.spi.model.type.Scope" %>
<%@ page import="javax.xml.stream.XMLStreamConstants" %>
<%--
 See the NOTICE file distributed with this work for information
 regarding copyright ownership.  This file is licensed
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
--%><%
    String componentName = request.getParameter("name");
    String scdl = request.getParameter("scdl");

    XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
    WebappRuntimeImpl runtime = (WebappRuntimeImpl) application.getAttribute("fabric3.runtime");

    LoaderRegistry loader = runtime.getSystemComponent(LoaderRegistry.class, LOADER_URI);
    DistributedAssembly assembly = runtime.getSystemComponent(DistributedAssembly.class, DISTRIBUTED_ASSEMBLY_URI);
    ScopeRegistry scopeRegistry = runtime.getSystemComponent(ScopeRegistry.class, ComponentNames.SCOPE_REGISTRY_URI);

    XMLStreamReader reader = xmlFactory.createXMLStreamReader(new StringReader(scdl));
    while (reader.next() != XMLStreamConstants.START_ELEMENT) ;
    URI componentURI = URI.create(assembly.getDomain().getUri().toString() + "/" + componentName);
    try {
        LoaderContext loaderContext = new LoaderContextImpl(Thread.currentThread().getContextClassLoader(), null);
        CompositeComponentType componentType = loader.load(reader, CompositeComponentType.class, loaderContext);
        CompositeImplementation implementation = new CompositeImplementation();
        implementation.setComponentType(componentType);
        ComponentDefinition component = new ComponentDefinition(componentURI.toString(), implementation);
        assembly.activate(component, true);

        ScopeContainer<URI> container = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
        WorkContext workContext = new SimpleWorkContext();
        workContext.setScopeIdentifier(Scope.COMPOSITE, componentURI);

        container.startContext(workContext, componentURI);
    } finally {
        reader.close();
    }
    request.getRequestDispatcher("index.jsp").forward(request, response);
%>
