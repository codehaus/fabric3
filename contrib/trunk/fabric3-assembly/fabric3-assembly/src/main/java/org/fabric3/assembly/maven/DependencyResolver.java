package org.fabric3.assembly.maven;

import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.fabric3.assembly.dependency.Dependency;
import org.fabric3.assembly.dependency.Version;
import org.fabric3.assembly.exception.AssemblyException;
import org.fabric3.assembly.utils.LoggerUtils;
import org.fabric3.assembly.utils.PathUtils;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactResolutionException;

import java.io.File;
import java.text.MessageFormat;

/**
 * Resolve/find dependency of some fabric3 module.
 *
 * @author Michal Capo
 */
public class DependencyResolver {

    private DependencyDownloader downloader = new DependencyDownloader();

    /**
     * Find dependency in dependency manager storage folder. (folder '.m2/repository'). When this dependency doesn't
     * exists it tries to download them.
     *
     * @param dependency you want to find
     * @return physical file if these dependency was found
     */
    public File findFile(final Dependency dependency) {

        final File result = new File(PathUtils.MAVEN_FOLDER + convertDependencyToPath(dependency));
        final AssemblyException missingDependencyException = new AssemblyException(MessageFormat.format("Dependency ''{0}'' wasn''t found at ''{1}''.", dependency.toString(), result.getAbsolutePath()));

        if (!result.exists()) {
            try {
                downloader.downloadDependencies(new DependencyDownloadListener() {
                    public void dependencyDownloading(String dependencyName) {
                        LoggerUtils.log(MessageFormat.format("downloading dependency - {0}", dependencyName));
                    }

                    public void dependencyMissing(String dependencyName) {
                        throw missingDependencyException;

                    }
                }, dependency.toString());
            } catch (PlexusContainerException e) {
                throw new AssemblyException(e);
            } catch (ComponentLookupException e) {
                throw new AssemblyException(e);
            } catch (ArtifactDescriptorException e) {
                throw new AssemblyException(e);
            } catch (ArtifactResolutionException e) {
                throw new AssemblyException(e);
            }
        }

        if (!result.exists()) {
            throw missingDependencyException;
        }

        return result;
    }

    private String convertDependencyToPath(final Dependency dependency) {
        return dependency.getGroup().replaceAll("\\.", File.separator) + File.separator + // group
                dependency.getArtifact() + File.separator + // artifact
                dependency.getVersion() + File.separator +  // version
                dependency.getArtifact() + makeSuffix(dependency.getVersion(), dependency.getClassifier(), dependency.getType()); // file
    }

    private String makeSuffix(final Version version, final String classifier, final String type) {
        String temp = "";
        if (null != classifier) {
            temp = "-" + classifier;
        }

        return "-" + version.toString() + temp + "." + type;
    }

}
