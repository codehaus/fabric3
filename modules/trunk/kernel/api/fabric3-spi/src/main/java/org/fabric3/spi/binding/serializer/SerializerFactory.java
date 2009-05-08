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
package org.fabric3.spi.binding.serializer;

import java.util.Set;

/**
 * Creates or returns Serializer instances.
 *
 * @version $Revision$ $Date$
 */
public interface SerializerFactory {

    /**
     * Create or return a Serializer  instance.
     *
     * @param types      the types the Serializer will serialize and deserialize
     * @param faultTypes the fault types the Serializer will serialize and deserialize
     * @param loader     the classloader to load custom parameter types
     * @return a Serializer instance
     * @throws SerializationException if an exception occurs creating or returning a Serializer
     */
    Serializer getInstance(Set<Class<?>> types, Set<Class<?>> faultTypes, ClassLoader loader) throws SerializationException;

}
