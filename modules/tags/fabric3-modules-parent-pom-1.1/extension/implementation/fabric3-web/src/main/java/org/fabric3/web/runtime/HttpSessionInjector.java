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

import javax.servlet.http.HttpSession;

import org.fabric3.pojo.reflection.Injector;
import org.fabric3.pojo.injection.MultiplicityObjectFactory;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;

/**
 * Injects an instance (e.g. a reference proxy) into an HTTP session object.
 *
 * @version $Revision$ $Date$
 */
public class HttpSessionInjector implements Injector<HttpSession> {
    private ObjectFactory<?> objectFactory;
    private String name;

    public void inject(HttpSession session) throws ObjectCreationException {
        session.setAttribute(name, objectFactory.getInstance());
    }

    public void setObjectFactory(ObjectFactory<?> objectFactory, Object name) {
        this.objectFactory = objectFactory;
        this.name = name.toString();
    }

    public void clearObjectFactory() {
        if (this.objectFactory instanceof MultiplicityObjectFactory<?>) {
            ((MultiplicityObjectFactory<?>) this.objectFactory).clear();
        } else {
            objectFactory = null;
        }
    }

}
