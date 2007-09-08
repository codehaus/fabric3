package org.fabric3.runtime.development.host;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.fabric3.host.runtime.AbstractHostInfo;

/**
 * @version $Rev$ $Date$
 */
public class DevelopmentHostInfoImpl extends AbstractHostInfo implements DevelopmentHostInfo {
    private File extensionsDir;

    public DevelopmentHostInfoImpl(final URI domain, final URL baseUrl, File extensionsDir) {
        super(domain, baseUrl, true, "DevelopmentRuntime");
        this.extensionsDir = extensionsDir;
    }

    public File getExtensionsDirectory() {
        return extensionsDir;
    }

}
