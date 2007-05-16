package org.fabric3.runtime.development.host;

import java.io.File;

import org.fabric3.host.runtime.HostInfo;

/**
 * HostInfo type for the development runtime
 *
 * @version $Rev$ $Date$
 */
public interface DevelopmentHostInfo extends HostInfo {

    /**
     * Return the directory where the development distribution was installed.
     *
     * @return the directory where the development distribution was installed
     */
    File getInstallDirectory();

    /**
     * Returns the host's ClassLoader. This is the root classloader for the runtime supplied by the host environment.
     *
     * @return the host's ClassLoader
     */
    ClassLoader getHostClassLoader();

    /**
     * Returns the runtime boot ClassLoader. This is the classloader used to load runtime components.
     *
     * @return the runtime boot ClassLoader
     */
    ClassLoader getBootClassLoader();
}
