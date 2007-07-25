package org.fabric3.runtime.development.host;

import java.io.File;
import java.net.URL;

import org.fabric3.host.runtime.HostInfo;

/**
 * HostInfo type for the development runtime
 *
 * @version $Rev$ $Date$
 */
public interface DevelopmentHostInfo extends HostInfo {

    File getExtensionsDirectory();
    
}
