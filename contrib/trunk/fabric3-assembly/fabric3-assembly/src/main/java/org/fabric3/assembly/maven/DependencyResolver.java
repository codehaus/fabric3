package org.fabric3.assembly.maven;

import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
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
     * Find dependency in dependency manager storage folder. (folder '.m2/repository'). When this dependency doens't
     * exists it tries to download them.
     *
     * @param dependency you want to find
     * @return physical file if these dependency was found
     */
    public File findFile(final String dependency) {

        final File result = new File(PathUtils.MAVEN_FOLDER + convertDependencyToPath(dependency));
        final AssemblyException missingDependencyException = new AssemblyException(MessageFormat.format("Depedency ''{0}'' wasn''t found at ''{1}''.", dependency, result.getAbsolutePath()));

        if (!result.exists()) {
            try {
                downloader.downloadDependencies(new DependencyDownloadListener() {
                    public void dependencyDownloading(String dependencyName) {
                        LoggerUtils.log(MessageFormat.format("downloading dependency - {0}", dependencyName));
                    }

                    public void dependencyMissing(String dependencyName) {
                        throw missingDependencyException;

                    }
                }, dependency);
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

    private String convertDependencyToPath(final String dependency) {
        AssemblyException ex = new AssemblyException(MessageFormat.format("Dependency {0} must match group:artifact:type:classifier:version pattern or just group:artifact:version pattern to resolve a jar dependency.", dependency));

        if (!dependency.matches("\\S.*:\\S.*:\\S.*")) {
            throw ex;
        }

        String[] dep = dependency.split(":");
        String group, artifact, version, classifier = null, type;
        switch (dep.length) {
            case 3:
                group = dep[0];
                artifact = dep[1];
                version = dep[2];
                type = "jar";
                break;
            case 5:
                group = dep[0];
                artifact = dep[1];
                type = dep[2];
                classifier = dep[3];
                version = dep[4];
                break;
            default:
                throw ex;
        }

        return group.replaceAll("\\.", File.separator) + File.separator + // group
                artifact + File.separator + // artifact
                version + File.separator +  // version
                artifact + makeSuffix(version, classifier, type); // file

    }

    private String makeSuffix(final String version, final String classifier, final String type) {
        String temp = "";
        if (null != classifier) {
            temp = "-" + classifier;
        }

        return "-" + version + temp + "." + type;
    }

}
