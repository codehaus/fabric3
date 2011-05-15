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

    public static Dependency convertToDependency(String pDependency) {
        AssemblyException ex = new AssemblyException(MessageFormat.format("Dependency {0} must match group:artifact:type:classifier:version pattern or just group:artifact:version pattern to resolve a jar dependency.", pDependency));

        if (!pDependency.matches("\\S.*:\\S.*:\\S.*")) {
            throw ex;
        }

        String[] dep = pDependency.split(SPLITTER);
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

        return new Dependency(group, artifact, classifier, type, new Version(version));
    }

    public static String convertToString(final Dependency dependency) {
        if (null == dependency.getClassifier()) {
            return MessageFormat.format("{0}:{1}:{2}",
                    dependency.getGroup(),
                    dependency.getArtifact(),
                    dependency.getVersion()
            );
        } else {
            return MessageFormat.format("{0}:{1}:{2}:{3}:{4}",
                    dependency.getGroup(),
                    dependency.getArtifact(),
                    dependency.getClassifier(),
                    dependency.getType(),
                    dependency.getVersion()
            );
        }
    }

}
