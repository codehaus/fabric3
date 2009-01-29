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
package org.fabric3.binding.jms.provider;

/**
 * Registers a JMS provider classloader. Used to instantiate provider connections.
 *
 * @version $Revision$ $Date$
 */
public interface ProviderRegistry {

    /**
     * Register the JMS provider classloader.
     *
     * @param loader the classloader
     */
    void registerProviderClassLoader(ClassLoader loader);

    /**
     * Loads a provider class.
     *
     * @param name the class name
     * @return the class
     * @throws ClassNotFoundException if the class is not visibile from the registered classloader
     */
    Class loadClass(String name) throws ClassNotFoundException;

}
