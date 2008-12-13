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
package org.fabric3.pojo.injection;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;

/**
 * A set based object factory.
 *
 * @version $Rev: 1 $ $Date: 2007-05-14 18:40:37 +0100 (Mon, 14 May 2007) $
 */
public class SetMultiplicityObjectFactory implements MultiplicityObjectFactory<Set<?>> {

    // Object factories
    private Set<ObjectFactory<?>> factories = new CopyOnWriteArraySet<ObjectFactory<?>>();

    public Set<Object> getInstance() throws ObjectCreationException {
        Set<Object> set = new CopyOnWriteArraySet<Object>();
        for (ObjectFactory<?> factory : factories) {
            set.add(factory.getInstance());
        }
        return set;
    }

    public void addObjectFactory(ObjectFactory<?> objectFactory, Object key) {
        factories.add(objectFactory);
    }

}
