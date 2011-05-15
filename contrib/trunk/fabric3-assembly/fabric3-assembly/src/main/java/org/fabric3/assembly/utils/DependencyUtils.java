package org.fabric3.assembly.utils;

import org.fabric3.assembly.dependency.Dependency;
import org.fabric3.assembly.dependency.Version;
import org.fabric3.assembly.exception.AssemblyException;

import java.text.MessageFormat;

/**
 * @author Michal Capo
 */
public class DependencyUtils {

    public static final String SPLITTER = ":";
    public static final String SPLITTER_TYPE = "@";

    public static Dependency parseDependency(String pStringDependency) {
        AssemblyException ex = new AssemblyException(MessageFormat.format("Dependency {0} must match group:artifact:version:classifier@type pattern or just group:artifact:version pattern to resolve a jar dependency. For version less dependency you can skip version flag.", pStringDependency));

        if (!pStringDependency.matches("\\S.*:\\S.*")) {
            throw ex;
        }

        String[] dep = pStringDependency.split(SPLITTER);
        String group, artifact, classifier = null, type = "jar";
        Version version = null;
        switch (dep.length) {
            case 4:
                //group:artifact:version:classifier@type
                String[] temp = dep[3].split(SPLITTER_TYPE);
                classifier = temp[0];
                type = temp[1];
            case 3:
                if (dep[2].contains(SPLITTER_TYPE)) {
                    //group:artifact:classifier@type
                    temp = dep[2].split(SPLITTER_TYPE);
                    classifier = temp[0];
                    type = temp[1];
                } else {
                    //group:artifact:version
                    version = new Version(dep[2]);
                }
            case 2:
                //group:artifact
                group = dep[0];
                artifact = dep[1];
                break;
            default:
                throw ex;
        }

        return new Dependency(group, artifact, version, classifier, type);
    }

    public static String convertToString(final Dependency dependency) {
        if (dependency.isVersionLess()) {
            if (null == dependency.getClassifier()) {
                return MessageFormat.format("{0}:{1}",
                        dependency.getGroup(),
                        dependency.getArtifact()
                );
            } else {
                return MessageFormat.format("{0}:{1}:{2}@{3}",
                        dependency.getGroup(),
                        dependency.getArtifact(),
                        dependency.getClassifier(),
                        dependency.getType()
                );
            }
        } else {
            if (null == dependency.getClassifier()) {
                return MessageFormat.format("{0}:{1}:{2}",
                        dependency.getGroup(),
                        dependency.getArtifact(),
                        dependency.getVersion()
                );
            } else {
                return MessageFormat.format("{0}:{1}:{2}:{3}@{4}",
                        dependency.getGroup(),
                        dependency.getArtifact(),
                        dependency.getVersion(),
                        dependency.getClassifier(),
                        dependency.getType()
                );
            }
        }
    }

}
