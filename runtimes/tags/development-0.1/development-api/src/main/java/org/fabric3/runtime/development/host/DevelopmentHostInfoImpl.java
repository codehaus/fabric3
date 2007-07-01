package org.fabric3.runtime.development.host;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.fabric3.host.runtime.AbstractHostInfo;

/**
 * @version $Rev$ $Date$
 */
public class DevelopmentHostInfoImpl extends AbstractHostInfo implements DevelopmentHostInfo {
    private final File installDirectory;
    private final ClassLoader hostClassLoader;
    private final ClassLoader bootClassLoader;


    public DevelopmentHostInfoImpl(final URI domain,
                                   final URL baseUrl,
                                   File installDirectory,
                                   ClassLoader hostClassLoader,
                                   ClassLoader bootClassLoader) {
        super(domain, baseUrl, true, "DevelopmentRuntime");
        this.installDirectory = installDirectory;
        this.hostClassLoader = hostClassLoader;
        this.bootClassLoader = bootClassLoader;
    }

    public File getInstallDirectory() {
        return installDirectory;
    }

    public ClassLoader getHostClassLoader() {
        return hostClassLoader;
    }

    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }
}
