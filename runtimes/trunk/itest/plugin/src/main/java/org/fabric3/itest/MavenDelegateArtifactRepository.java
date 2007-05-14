package org.fabric3.itest;

import java.util.Collection;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.services.artifact.Artifact;
import org.fabric3.spi.services.artifact.ArtifactRepository;

/**
 * @version $Rev$ $Date$
 */
public class MavenDelegateArtifactRepository implements ArtifactRepository {
    private final ArtifactRepository artifactRepository;

    public MavenDelegateArtifactRepository(@Reference MavenHostInfo runtimeInfo) {
        artifactRepository = runtimeInfo.getArtifactRepository();
    }

    public void resolve(Artifact artifact) {
        artifactRepository.resolve(artifact);
    }

    public void resolve(Collection<? extends Artifact> artifacts) {
        artifactRepository.resolve(artifacts);
    }
}
