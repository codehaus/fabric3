<%@ page import="java.net.URI" %>
<%@ page import="org.fabric3.fabric.assembly.Assembly" %>
<%@ page import="org.fabric3.runtime.webapp.WebappRuntimeImpl" %>
<%@ page import="org.fabric3.fabric.assembly.RuntimeInfo" %>
<%@ page import="java.util.Collection" %>
<%@ page import="org.fabric3.spi.model.type.CompositeImplementation" %>
<%@ page import="org.fabric3.spi.model.instance.LogicalComponent" %>
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
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    WebappRuntimeImpl runtime = (WebappRuntimeImpl) application.getAttribute("fabric3.runtime");
    Assembly assembly = runtime.getSystemComponent(Assembly.class,
                                                   URI.create("fabric3://./runtime/main/distributedAssembly"));
    LogicalComponent<CompositeImplementation> domain = assembly.getDomain();
%>
<html>
<head><title>fabric3 controller</title></head>
<body>
<h1>fabric3 controller for <%= domain.getUri() %>
</h1>
<%
    Collection<RuntimeInfo> runtimes = assembly.getRuntimes();
%>
<h2>Participating Runtimes</h2>
<table><tr><th>Runtime Id</th></tr>
<%
    for (RuntimeInfo runtimeInfo : runtimes) {
        out.print("<tr><td>" + runtimeInfo.getId() + "</td></tr>");
    }
%>
</table>
<h2>Components</h2>
<table>
    <th>ComponentId</th><th>Runtime</th>
<%
    Collection<LogicalComponent<?>> components = domain.getComponents();
    for (LogicalComponent<?> component : components) {
%>  <tr><td><%= component.getUri()%></td><td><%= component.getRuntimeId()%></td></tr><%        
    }
%>
</table>
<h2>Create New Component</h2>
<form action="upload.jsp" method="POST">
    <table>
        <tr><td>Component name</td><td><input name="name" type="text" size="40"/></td></tr>
        <tr><td valign="top">Assembly XML</td><td><textarea name="scdl" rows="30" cols="40"></textarea></td></tr>
        <tr><td/><td align="center"><input type="submit" value="Create"/></td></tr>
        
    </table>
</form>
</body>
</html>