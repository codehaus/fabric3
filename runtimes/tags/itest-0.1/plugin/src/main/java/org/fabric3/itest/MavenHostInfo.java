package org.fabric3.itest;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.services.artifact.ArtifactRepository;

/**
 * @version $Rev$ $Date$
 */
public interface MavenHostInfo extends HostInfo {
    ArtifactRepository getArtifactRepository();
}
