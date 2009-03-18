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

import javax.servlet.ServletContext;

import org.fabric3.pojo.reflection.Injector;
import org.fabric3.pojo.injection.MultiplicityObjectFactory;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;

/**
 * Injects objects (reference proxies, properties, contexts) into a ServletContext.
 *
 * @version $Revision$ $Date$
 */
public class ServletContextInjector implements Injector<ServletContext> {
    private ObjectFactory<?> objectFactory;
    private String key;

    public void inject(ServletContext context) throws ObjectCreationException {
        context.setAttribute(key, objectFactory.getInstance());
    }

    public void setObjectFactory(ObjectFactory<?> objectFactory, Object key) {
        this.objectFactory = objectFactory;
        this.key = key.toString();
    }

    public void clearObjectFactory() {
        if (this.objectFactory instanceof MultiplicityObjectFactory<?>) {
            ((MultiplicityObjectFactory<?>) this.objectFactory).clear();
        } else {
            objectFactory = null;
            key = null;
        }
    }

}
