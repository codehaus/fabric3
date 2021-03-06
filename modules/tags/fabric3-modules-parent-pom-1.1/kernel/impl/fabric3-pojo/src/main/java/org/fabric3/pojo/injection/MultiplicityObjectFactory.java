/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.pojo.injection;

import org.fabric3.spi.ObjectFactory;

/**
 * Common interface for all multiplicity object factories.
 *
 * @version $Revision$ $Date$
 * @param <T>
 */
public interface MultiplicityObjectFactory<T> extends ObjectFactory<T> {

    /**
     * Adds a constituent object factory.
     *
     * @param objectFactory Constituent object factory
     * @param key           the target key
     */
    void addObjectFactory(ObjectFactory<?> objectFactory, Object key);

    /**
     * Clears the contents of the object factory
     */
    void clear();
}
