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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;

/**
 * Resolves targets configured in a multiplicity by delegating to object factories and returning an <code>List</code>
 * containing object instances
 *
 * @version $Rev$ $Date$
 */
public class ListMultiplicityObjectFactory implements MultiplicityObjectFactory<List<?>> {

    // Object factories
    private List<ObjectFactory<?>> factories = new CopyOnWriteArrayList<ObjectFactory<?>>();

    public List<Object> getInstance() throws ObjectCreationException {
        List<Object> list = new CopyOnWriteArrayList<Object>();
        for (ObjectFactory<?> factory : factories) {
            list.add(factory.getInstance());
        }
        return list;
    }

    public void addObjectFactory(ObjectFactory<?> objectFactory, Object key) {
        factories.add(objectFactory);
    }

}
