package org.fabric3.runtime.embedded.service;

import org.fabric3.runtime.embedded.api.service.EmbeddedDependencyResolver;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3StartupException;

import java.io.File;
import java.text.MessageFormat;

/**
 * @author Michal Capo
 */
public class MavenDependencyResolver implements EmbeddedDependencyResolver {

    private static String userHomeFolder = System.getProperty("user.home");

    private static String mavenDefaultFolder = ".m2" + File.separator + "repository";

    private String prefix;

    {
        prefix = userHomeFolder + addSlash(userHomeFolder) + mavenDefaultFolder + addSlash(mavenDefaultFolder);
    }

    public File findFile(final String dependency) throws EmbeddedFabric3StartupException {
        File result = new File(prefix + addSlash(prefix) + convertDependencyToPath(dependency));

        if (!result.exists()) {
            throw new EmbeddedFabric3StartupException(MessageFormat.format("Depedency ''{0}'' wasn''t found at ''{1}''.", dependency, result.getAbsolutePath()));
        }

        return result;
    }

    private String addSlash(String partialPath) {
        if (!partialPath.endsWith(File.separator)) {
            return File.separator;
        } else {
            return "";
        }
    }

    private String convertDependencyToPath(final String dependency) throws EmbeddedFabric3StartupException {
        EmbeddedFabric3StartupException ex = new EmbeddedFabric3StartupException(MessageFormat.format("Dependency {0} must match group:artifact:version:classifier@type pattern or just group:artifact:version pattern to resolve a jar dependency.", dependency));

        if (!dependency.matches("\\S.*:\\S.*:\\S.*")) {
            throw ex;
        }

        String[] dep = dependency.split(":");
        String group, artifact, version, classifier = null, type;
        switch (dep.length) {
            case 3:
                group = dep[0];
                artifact = dep[1];
                if (!dep[2].contains("@")) {
                    version = dep[2];
                    type = "jar";
                } else {
                    String[] add = dep[2].split("@");
                    version = add[0];
                    type = add[1];
                }
                break;
            case 4:
                group = dep[0];
                artifact = dep[1];
                version = dep[2];
                if (!dep[3].contains("@")) {
                    classifier = dep[3];
                    type = "jar";
                } else {
                    String[] add = dep[3].split("@");
                    classifier = add[0];
                    type = add[1];
                }
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
