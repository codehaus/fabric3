/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.console.handler.scdl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.assembly.DistributedAssembly;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.model.type.CompositeImplementation;
import org.fabric3.spi.services.discovery.DiscoveryService;

/**
 * A temporary console servlet for showing the status of the distributed domain.
 *
 * @version $Rev$ $Date$
 */
public class ConsoleServlet extends Fabric3Servlet {
    private DistributedAssembly assembly;
    private DiscoveryService discoveryService;

    /**
     * Injects the servlet host and path mapping.
     *
     * @param servletHost      Servlet host to use.
     * @param path             Path mapping for the servlet.
     * @param assembly         the distrbituted assembly
     * @param discoveryService the discovery service
     */
    public ConsoleServlet(@Reference ServletHost servletHost,
                          @Reference DistributedAssembly assembly,
                          @Reference DiscoveryService discoveryService,
                          @Property(name = "path")String path) {
        super(servletHost, path);
        this.assembly = assembly;
        this.discoveryService = discoveryService;
    }

    protected void process(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String requestURI = req.getRequestURI();
        int pos = req.getRequestURI().lastIndexOf("/");
        if (pos > 0) {
            String path = requestURI.substring(pos + 1);
            if (path.equals("runtimes.gif")) {
                InputStream stream = getClass().getResourceAsStream("runtimes.gif");
                OutputStream os = res.getOutputStream();
                copy(stream, os);
                return;
            } else if (path.equals("component.gif")) {
                InputStream stream = getClass().getResourceAsStream("component.gif");
                OutputStream os = res.getOutputStream();
                copy(stream, os);
                return;
            }
        }
        LogicalComponent<CompositeImplementation> domain = assembly.getDomain();
        Set<RuntimeInfo> runtimes = discoveryService.getParticipatingRuntimes();
        PrintWriter writer = res.getWriter();
        writer.write("<html><head><title>Fabric3 controller</title></head><body><h2>Current Domain: ");
        writer.write(domain.getUri().toString());
        writer.write("</h2><h3>Participating Runtimes</h3>");
        if (runtimes.size() == 0) {
            writer.write("No remote runtimes");
        } else {
            writer.write(
                    "<table><th width=\"120\"/><th align=\"left\" width=\"120\">ID</th><th align=\"left\">Status</th><th align=\"left\">Features</th>");
            for (RuntimeInfo runtimeInfo : runtimes) {
                writer.write("<tr><td><img src=\"\\console\\runtimes.gif\"></img></td><td>");
                writer.write(runtimeInfo.getId());
                writer.write("</td><td>Running</td><td>");
                for(QName feature : runtimeInfo.getFeatures()) {
                    writer.write(feature.getLocalPart());
                    writer.write("</br>");

                }
                writer.write("</table>");
                writer.write("</td></tr>");
            }
            writer.write("</table>");
        }
        writer.write(
                "<h3>Components</h3><table><th></th><th width=\"300\" align=\"left\">ID</th><th align=\"left\">Runtime</th>");

        Collection<LogicalComponent<?>> components = domain.getComponents();
        for (LogicalComponent<?> component : components) {
            writer.write("<tr><td><img src=\"\\console\\component.gif\"></td><td>" + component.getUri() + "</td>");
            if (component.getRuntimeId() != null) {
                writer.write("<td>" + component.getRuntimeId() + "</td></tr>");
            } else {
                writer.write("<td>Local</td></tr>");
            }
        }
        writer.write("</table></body></html>");
    }

    public int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) { // NOPMD
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

}
