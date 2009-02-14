/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.web.runtime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.oasisopen.sca.ServiceRuntimeException;
import org.osoa.sca.CallableReference;
import org.osoa.sca.ComponentContext;
import org.osoa.sca.RequestContext;
import org.osoa.sca.ServiceReference;

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
