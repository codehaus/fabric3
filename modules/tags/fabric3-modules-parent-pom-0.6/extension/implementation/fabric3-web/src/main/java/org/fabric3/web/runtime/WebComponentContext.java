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
package org.fabric3.web.runtime;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import org.osoa.sca.CallableReference;
import org.osoa.sca.ComponentContext;
import org.osoa.sca.RequestContext;
import org.osoa.sca.ServiceReference;
import org.osoa.sca.ServiceRuntimeException;

import org.fabric3.container.web.spi.WebRequestTunnel;
import org.fabric3.host.Fabric3RuntimeException;
import org.fabric3.spi.ObjectCreationException;

/**
 * Implementation of ComponentContext for Web components.
 *
 * @version $Rev: 1363 $ $Date: 2007-09-20 16:16:35 -0700 (Thu, 20 Sep 2007) $
 */
public class WebComponentContext implements ComponentContext {
    private final WebComponent<?> component;

    public WebComponentContext(WebComponent<?> component) {
        this.component = component;
    }

    public String getURI() {
        try {
            return component.getUri().toString();
        } catch (Fabric3RuntimeException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public <B, R extends CallableReference<B>> R cast(B target) throws IllegalArgumentException {
        try {
            return (R) component.cast(target);
        } catch (Fabric3RuntimeException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public <B> B getService(Class<B> interfaze, String referenceName) {
        try {
            return interfaze.cast(getSession().getAttribute(referenceName));
        } catch (Fabric3RuntimeException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    // method is a proposed spec change
    public <B> B createService(Class<B> interfaze, String referenceName) {
        try {
            return component.getService(interfaze, referenceName);
        } catch (Fabric3RuntimeException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        } catch (ObjectCreationException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    @SuppressWarnings({"unchecked"})
    public <B> ServiceReference<B> getServiceReference(Class<B> interfaze, String referenceName) {
        try {
            return ServiceReference.class.cast(getSession().getAttribute(referenceName));
        } catch (Fabric3RuntimeException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public <B> B getProperty(Class<B> type, String propertyName) {
        try {
            return component.getProperty(type, propertyName);
        } catch (ObjectCreationException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        } catch (Fabric3RuntimeException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface) {
        return null;
    }

    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface, String serviceName) {
        return null;
    }

    public RequestContext getRequestContext() {
        return null;
    }

    private HttpSession getSession() {
        HttpServletRequest request = WebRequestTunnel.getRequest();
        if (request == null) {
            throw new ServiceRuntimeException("HTTP request not bound. Check filter configuration.");
        }
        return request.getSession(true);  // force creation of session 
    }


}
