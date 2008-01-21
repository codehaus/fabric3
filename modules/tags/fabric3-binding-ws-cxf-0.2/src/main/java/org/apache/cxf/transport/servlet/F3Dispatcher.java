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
package org.apache.cxf.transport.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.util.Collection;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.BusFactory;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.https.SSLUtils;
import org.apache.cxf.transports.http.QueryHandler;
import org.apache.cxf.transports.http.QueryHandlerRegistry;
import org.xmlsoap.schemas.wsdl.http.AddressType;

/**
 * Dispatches a request to CXF where it will be processed and passed to the target service.
 * <p/>
 * This implementation is a replacement for <code>org.apache.cxf.transport.servlet.CXFServlet</code>, which is hardwired
 * to use Spring as the CXF configuration mechanism. Note this class must be in the CXF package since it accesses {@link
 * ServletDestination#doMessage(org.apache.cxf.message.MessageImpl)}.
 * <p/>
 *
 * @version $Rev$ $Date$
 */
public class F3Dispatcher extends HttpServlet {
    private Bus bus;
    private ServletTransportFactory transport;
    private String lastBase = "";

    public F3Dispatcher(Bus bus, ServletTransportFactory transport) throws BusException {
        this.bus = bus;
        this.transport = transport;
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        invoke(req, resp);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        invoke(req, resp);
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        invoke(req, resp);
    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        invoke(req, resp);
    }

    private void invoke(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            BusFactory.setThreadDefaultBus(bus);
            invokeInternal(req, resp);
        } finally {
            BusFactory.setThreadDefaultBus(null);
        }

    }

    public void invokeInternal(HttpServletRequest request, HttpServletResponse res) throws ServletException {
        try {
            EndpointInfo ei = new EndpointInfo();
            String address = request.getPathInfo() == null ? "" : request.getPathInfo();
            ei.setAddress(address);
            ServletDestination d = transport.getDestinationForPath(ei.getAddress());

            if (d == null) {
                if (request.getRequestURI().endsWith("services")
                        || request.getRequestURI().endsWith("services/")
                        || StringUtils.isEmpty(request.getPathInfo())
                        || "/".equals(request.getPathInfo())) {
                    updateDests(request);
                    generateServiceList(res);
                } else {
                    d = checkRestfulRequest(request);
                    if (d == null || d.getMessageObserver() == null) {
                        generateNotFound(request, res);
                    } else { // the request should be a restful service request
                        updateDests(request);
                        invokeDestination(request, res, d);
                    }
                }
            } else {
                ei = d.getEndpointInfo();
                if (null != request.getQueryString()
                        && request.getQueryString().length() > 0
                        && bus.getExtension(QueryHandlerRegistry.class) != null) {

                    String ctxUri = request.getPathInfo();
                    String baseUri = request.getRequestURL().toString()
                            + "?" + request.getQueryString();
                    // update the EndPoint Address with request url
                    if ("GET".equals(request.getMethod())) {
                        updateDests(request);
                    }

                    for (QueryHandler qh : bus.getExtension(QueryHandlerRegistry.class).getHandlers()) {
                        if (qh.isRecognizedQuery(baseUri, ctxUri, ei)) {

                            res.setContentType(qh.getResponseContentType(baseUri, ctxUri));
                            OutputStream out = res.getOutputStream();
                            try {
                                qh.writeResponse(baseUri, ctxUri, ei, out);
                                out.flush();
                                return;
                            } catch (Exception e) {
                                throw new ServletException(e);
                            }
                        }
                    }
                }

                invokeDestination(request, res, d);
            }
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    private synchronized void updateDests(HttpServletRequest request) {
        String base = getBaseURL(request);

        if (base.equals(lastBase)) {
            return;
        }
        Set<String> paths = transport.getDestinationsPaths();
        for (String path : paths) {
            ServletDestination d2 = transport.getDestinationForPath(path);
            String ad = d2.getEndpointInfo().getAddress();
            if (ad.equals(path)
                    || ad.equals(lastBase + path)) {
                d2.getEndpointInfo().setAddress(base + path);
                if (d2.getEndpointInfo().getExtensor(AddressType.class) != null) {
                    d2.getEndpointInfo().getExtensor(AddressType.class).setLocation(base + path);
                }
            }
        }
        lastBase = base;
    }

    private ServletDestination checkRestfulRequest(HttpServletRequest request) throws IOException {

        String address = request.getPathInfo() == null ? "" : request.getPathInfo();

        for (String path : transport.getDestinationsPaths()) {
            if (address.startsWith(path)) {
                return transport.getDestinationForPath(path);
            }
        }
        return null;
    }

    private void generateServiceList(HttpServletResponse response) throws IOException {
        Collection<ServletDestination> destinations = transport.getDestinations();
        response.setContentType("text/html");
        response.getWriter().write("<html><body>");
        response.getWriter().write("<h2>Fabric3 Runtime Node</h2><br>");
        response.getWriter().write("<h3>Provisioned Endpoints</h3>");

        if (destinations.size() > 0) {
            for (ServletDestination sd : destinations) {
                if (null != sd.getEndpointInfo().getName()) {
                    String address = sd.getEndpointInfo().getAddress();
                    response.getWriter().write("<p> <a href=\"" + address + "?wsdl\">");
                    response.getWriter().write(sd.getEndpointInfo().getName() + "</a> </p>");
                }
            }
        } else {
            response.getWriter().write("No service was found.");
        }
        response.getWriter().write("</body></html>");
    }

    private String getBaseURL(HttpServletRequest request) {
        String reqPerfix = request.getRequestURL().toString();
        String pathInfo = request.getPathInfo() == null ? "" : request.getPathInfo();
        reqPerfix = reqPerfix.substring(0, reqPerfix.length() - pathInfo.length());
        return reqPerfix;
    }

    protected void generateNotFound(HttpServletRequest request, HttpServletResponse res) throws IOException {
        res.setStatus(404);
        res.setContentType("text/html");
        res.getWriter().write("<html><body>No service was found.</body></html>");
    }

    public void invokeDestination(final HttpServletRequest request,
                                  HttpServletResponse response,
                                  ServletDestination d) throws ServletException {
        try {
            MessageImpl inMessage = new MessageImpl();
            inMessage.setContent(InputStream.class, request.getInputStream());
            inMessage.put(AbstractHTTPDestination.HTTP_REQUEST, request);
            inMessage.put(AbstractHTTPDestination.HTTP_RESPONSE, response);
            inMessage.put(AbstractHTTPDestination.HTTP_CONTEXT, getServletContext());
            inMessage.put(Message.HTTP_REQUEST_METHOD, request.getMethod());
            inMessage.put(Message.PATH_INFO, request.getPathInfo());
            inMessage.put(Message.QUERY_STRING, request.getQueryString());
            inMessage.put(Message.CONTENT_TYPE, request.getContentType());
            inMessage.put(Message.BASE_PATH, d.getAddress().getAddress().getValue());
            inMessage.put(SecurityContext.class, new SecurityContext() {
                public Principal getUserPrincipal() {
                    return request.getUserPrincipal();
                }

                public boolean isUserInRole(String role) {
                    return request.isUserInRole(role);
                }
            });

            // work around a bug with Jetty which results in the character
            // encoding not being trimmed correctly.
            String enc = request.getCharacterEncoding();
            if (enc != null && enc.endsWith("\"")) {
                enc = enc.substring(0, enc.length() - 1);
            }

            inMessage.put(Message.ENCODING, enc);
            SSLUtils.propogateSecureSession(request, inMessage);
            d.doMessage(inMessage);
        } catch (IOException e) {
            throw new ServletException(e);
        }

    }


}
