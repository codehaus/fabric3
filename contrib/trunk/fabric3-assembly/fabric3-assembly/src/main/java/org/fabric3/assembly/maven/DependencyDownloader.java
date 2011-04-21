package org.fabric3.assembly.maven;

import org.fabric3.assembly.utils.PathUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Michal Capo
 */
public class DependencyDownloader extends MavenDownloader {

    @Override
    public String getLocalMavenFolder() {
        return PathUtils.MAVEN_FOLDER;
    }

    @Override
    public Map<String, String> getRemoteMavenUrl() {
        return new HashMap<String, String>() {
            {
                put("central", "http://repo1.maven.org/maven2/");
                put("codehaus", "http://repository.codehaus.org/");
                put("codehaus-ci", "http://ci.repository.codehaus.org/");
            }
        };
    }

}
