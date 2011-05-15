package org.fabric3.assembly.dependency.profile.fabric;

import org.fabric3.assembly.dependency.PartialDependency;
import org.fabric3.assembly.dependency.profile.Profile;

/**
 * @author Michal Capo
 */
public class FabricDependencyFactory {

    public static Profile createProfile(final String jarName, final String... alternativeNames) {
        return new Profile(jarName, alternativeNames) {{
            files.add(zip(jarName));
        }};
    }

    public static PartialDependency jar(String jarName) {
        return new PartialDependency("org.codehause.fabric3", jarName);
    }

    public static PartialDependency zip(String zipName) {
        return new PartialDependency("org.codehause.fabric3", zipName, "bin", "zip");
    }

}
