  /*
   * Fabric3
   * Copyright (c) 2009 Metaform Systems
   *
   * Fabric3 is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as
   * published by the Free Software Foundation, either version 3 of
   * the License, or (at your option) any later version, with the
   * following exception:
   *
   * Linking this software statically or dynamically with other
   * modules is making a combined work based on this software.
   * Thus, the terms and conditions of the GNU General Public
   * License cover the whole combination.
   *
   * As a special exception, the copyright holders of this software
   * give you permission to link this software with independent
   * modules to produce an executable, regardless of the license
   * terms of these independent modules, and to copy and distribute
   * the resulting executable under terms of your choice, provided
   * that you also meet, for each linked independent module, the
   * terms and conditions of the license of that module. An
   * independent module is a module which is not derived from or
   * based on this software. If you modify this software, you may
   * extend this exception to your version of the software, but
   * you are not obligated to do so. If you do not wish to do so,
   * delete this exception statement from your version.
   *
   * Fabric3 is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty
   * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   * See the GNU General Public License for more details.
   *
   * You should have received a copy of the
   * GNU General Public License along with Fabric3.
   * If not, see <http://www.gnu.org/licenses/>.
   */
package org.fabric3.binding.ws.axis2.runtime.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.AxisServlet;
import org.fabric3.spi.classloader.MultiParentClassLoader;

/**
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class F3AxisServlet extends AxisServlet {
    
    private Map<String, ClassLoader> classLoaders = new HashMap<String, ClassLoader>();

    /**
     * Initializes the Axis configuration context.
     *
     * @param configurationContext Axis configuration context.
     */
    public F3AxisServlet(final ConfigurationContext configurationContext) {
        this.configContext = configurationContext;
    }
    
    /**
     * Registers a classloader with the incoming path.
     * 
     * @param path Path of the incoming request.
     * @param classLoader Classloader used by the request.
     */
    public void registerClassLoader(String path, ClassLoader classLoader) {
        classLoaders.put(path, classLoader);
    }

    /**
     * Adds the Axis configuration context to the servlet context.
     *
     * @see org.apache.axis2.transport.http.AxisServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init(ServletConfig config) throws ServletException {

        ServletContext servletContext = config.getServletContext();
        servletContext.setAttribute(AxisServlet.CONFIGURATION_CONTEXT, configContext);

        super.init(config);

    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Thread currentThread = Thread.currentThread();
        ClassLoader oldCl = currentThread.getContextClassLoader();

        try {

            String requestUri = request.getRequestURI();
            ClassLoader classLoader = classLoaders.get(requestUri);
            if (classLoader instanceof MultiParentClassLoader) {
                MultiParentClassLoader mpcl = (MultiParentClassLoader) classLoader;
                mpcl.addParent(getClass().getClassLoader());
            }
            currentThread.setContextClassLoader(getClass().getClassLoader());
            
            super.service(request, response);

        } finally {
            currentThread.setContextClassLoader(oldCl);
        }

    }

    /**
     * Implementaion of POST interface
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    /*protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //set the initial buffer for a larger value
        response.setBufferSize(1024 * 8);

        initContextRoot(request);

        MessageContext msgContext;
        OutputStream out = response.getOutputStream();
        String contentType = request.getContentType();
        if (!HTTPTransportUtils.isRESTRequest(contentType)) {
            msgContext = createMessageContext(request, response);
            msgContext.setProperty(Constants.Configuration.CONTENT_TYPE, contentType);
            try {
                // adding ServletContext into msgContext;
                InvocationResponse pi = HTTPTransportUtils.
                        processHTTPPostRequest(msgContext,
                                new BufferedInputStream(request.getInputStream()),
                                new BufferedOutputStream(out),
                                contentType,
                                request.getHeader(HTTPConstants.HEADER_SOAP_ACTION),
                                request.getRequestURL().toString());

                Boolean holdResponse =
                        (Boolean) msgContext.getProperty(RequestResponseTransport.HOLD_RESPONSE);

                if (pi.equals(InvocationResponse.SUSPEND) ||
                        (holdResponse != null && Boolean.TRUE.equals(holdResponse))) {
                    ((RequestResponseTransport) msgContext
                            .getProperty(RequestResponseTransport.TRANSPORT_CONTROL))
                            .awaitResponse();
                }
                response.setContentType("text/xml; charset="
                        + msgContext
                        .getProperty(Constants.Configuration.CHARACTER_SET_ENCODING));
                // if data has not been sent back and this is not a signal response
                if (!TransportUtils.isResponseWritten(msgContext)  
                        && (((RequestResponseTransport) 
                                msgContext.getProperty(
                                        RequestResponseTransport.TRANSPORT_CONTROL)).
                                        getStatus() != RequestResponseTransport.
                                        RequestResponseTransportStatus.SIGNALLED)) {
                    response.setStatus(HttpServletResponse.SC_ACCEPTED);
                }

            } catch (AxisFault e) {
                e.printStackTrace();
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                TransportUtils.deleteAttachments(msgContext);
            }
        } else {
            if (!disableREST) {
                new RestRequestProcessor(Constants.Configuration.HTTP_METHOD_POST, request, response)
                        .processXMLRequest();
            } else {
                showRestDisabledErrorMessage(response);
            }
        }
    }*/

}
