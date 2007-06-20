<%@ page import="java.net.URI" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.fabric3.fabric.assembly.DistributedAssembly" %>
<%@ page import="org.fabric3.runtime.webapp.WebappRuntimeImpl" %>
<%@ page import="org.fabric3.spi.model.instance.LogicalComponent" %>
<%@ page import="org.fabric3.spi.model.topology.RuntimeInfo" %>
<%@ page import="org.fabric3.spi.model.type.CompositeImplementation" %>
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
    DistributedAssembly assembly = runtime.getSystemComponent(DistributedAssembly.class,
                                                              URI.create("fabric3://./runtime/main/distributedAssembly"));
    LogicalComponent<CompositeImplementation> domain = assembly.getDomain();
%>
<html>
<head><title>Fabric3 controller</title></head>
<body>
<h2>Current Domain: <%= domain.getUri() %>
</h2>

<h3>Participating Runtimes</h3>
<%
    Map<String, RuntimeInfo> runtimes = assembly.getRuntimes();
%>
<%
    if (runtimes.size() == 0) {
        out.print("No remote runtimes");
    } else {
%>
<table>
    <th width="120"/>
    <th align="left" width="120">ID</th>
    <th align="left">Status</th>
    <%
        for (RuntimeInfo runtimeInfo : runtimes.values()) {
            out.print("<tr><td><img src=\"runtimes.gif\"></img></td><td>" + runtimeInfo.getId() + "</td><td>Running</td></tr>");
        }

    %>
</table>
<%
    }
%>
<h3>Components</h3>
<table>
    <th></th>
    <th width="300" align="left">ID</th>
    <th align="left">Runtime</th>
    <%
        Collection<LogicalComponent<?>> components = domain.getComponents();
        for (LogicalComponent<?> component : components) {
            out.print("<tr><td><img src=\"component.gif\"></td><td>" + component.getUri() + "</td>");
            if (component.getRuntimeId() != null) {
                out.print("<td>" + component.getRuntimeId() + "</td></tr>");
            } else {
                out.print("<td>Local</td></tr>");
            }
        }
    %>


</table>
</body>
</html>