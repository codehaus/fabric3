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
package org.fabric3.pojo.component;

import javax.security.auth.Subject;

import org.oasisopen.sca.ServiceReference;

import org.fabric3.api.Fabric3RequestContext;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;

/**
 * @version $Rev$ $Date$
 */
public class OASISPojoRequestContext implements Fabric3RequestContext {
    public Subject getSecuritySubject() {
        WorkContext workContext = WorkContextTunnel.getThreadWorkContext();
        return workContext.getSubject();
    }

    public String getServiceName() {
        return null;
    }

    public <B> ServiceReference<B> getServiceReference() {
        return null;
    }

    public <CB> CB getCallback() {
        return null;
    }

    public <CB> ServiceReference<CB> getCallbackReference() {
        return null;
    }

    public <T> T getHeader(Class<T> type, String name) {
        WorkContext workContext = WorkContextTunnel.getThreadWorkContext();
        return workContext.getHeader(type, name);
    }

    public void setHeader(String name, Object value) {
        WorkContext workContext = WorkContextTunnel.getThreadWorkContext();
        workContext.setHeader(name, value);
    }

    public void removeHeader(String name) {
        WorkContext workContext = WorkContextTunnel.getThreadWorkContext();
        workContext.removeHeader(name);
    }
}