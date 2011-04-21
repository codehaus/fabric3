package org.fabric3.assembly.maven;

import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

import java.util.Map;

/**
 * @author Michal Capo
 */
public abstract class MavenDownloader {

    public abstract String getLocalMavenFolder();

    public abstract Map<String, String> getRemoteMavenUrl();

    private RepositorySystem newRepositorySystem() throws PlexusContainerException, ComponentLookupException {
        return new DefaultPlexusContainer().lookup(RepositorySystem.class);
    }

    private RepositorySystemSession newSession(RepositorySystem system) {
        // add local maven folder
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(new LocalRepository(getLocalMavenFolder())));

        return session;
    }

    public void downloadTransientDependencies(String... dependencies) throws DependencyCollectionException, DependencyResolutionException, PlexusContainerException, ComponentLookupException {
        // setup
        RepositorySystem repoSystem = newRepositorySystem();
        RepositorySystemSession session = newSession(repoSystem);

        CollectRequest collectRequest = new CollectRequest();

        // add remote repositories
        for (Map.Entry<String, String> repository : getRemoteMavenUrl().entrySet()) {
            collectRequest.addRepository(new RemoteRepository(repository.getKey(), "default", repository.getValue()));
        }

        // add required dependencies
        for (String stringDependency : dependencies) {
            collectRequest.addDependency(new Dependency(new DefaultArtifact(stringDependency), "compile"));
        }
        DependencyNode node = repoSystem.collectDependencies(session, collectRequest).getRoot();

        DependencyRequest dependencyRequest = new DependencyRequest(node, null);
        repoSystem.resolveDependencies(session, dependencyRequest);

        PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
        node.accept(nlg);
    }

    public void downloadDependencies(DependencyDownloadListener pNotifier, String... dependencies) throws PlexusContainerException, ComponentLookupException, ArtifactDescriptorException, ArtifactResolutionException {
        // setup
        RepositorySystem repoSystem = newRepositorySystem();
        RepositorySystemSession session = newSession(repoSystem);

        for (String dependency : dependencies) {
            ArtifactRequest artifactRequest = new ArtifactRequest();
            DefaultArtifact artifact = new DefaultArtifact(dependency);
            artifactRequest.setArtifact(artifact);
            for (Map.Entry<String, String> repository : getRemoteMavenUrl().entrySet()) {
                artifactRequest.addRepository(new RemoteRepository(repository.getKey(), "default", repository.getValue()));
            }

            pNotifier.dependencyDownloading(artifact.toString());
            ArtifactResult result = repoSystem.resolveArtifact(session, artifactRequest);
            if (result.isMissing()) {
                pNotifier.dependencyMissing(result.getArtifact().toString());
            }
        }

    }
}
