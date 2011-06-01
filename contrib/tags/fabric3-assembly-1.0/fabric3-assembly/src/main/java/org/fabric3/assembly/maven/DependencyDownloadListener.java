package org.fabric3.assembly.maven;

/**
 * @author Michal Capo
 */
public interface DependencyDownloadListener {

    void dependencyDownloading(String dependencyName);

    void dependencyMissing(String dependencyName);

}
