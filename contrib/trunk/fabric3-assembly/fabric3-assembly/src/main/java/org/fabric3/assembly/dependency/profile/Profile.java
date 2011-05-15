package org.fabric3.assembly.dependency.profile;

import org.fabric3.assembly.dependency.PartialDependency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fabrics profile specified via maven dependencies.
 *
 * @author Michal Capo
 */
public class Profile {

    /**
     * Profile name.
     */
    private String name;

    /**
     * Holds list of alternative name for this profile.
     */
    private List<String> alternativeNames = new ArrayList<String>();

    /**
     * Dependencies specified for this profile.
     */
    protected List<PartialDependency> files = new ArrayList<PartialDependency>();

    /**
     * Create profile with given name.
     *
     * @param pName of profile
     */
    public Profile(String pName) {
        name = pName;
    }

    /**
     * Create profile with given name and his alternative names.
     *
     * @param pName             of profile
     * @param pAlternativeNames of this profile
     */
    public Profile(String pName, String... pAlternativeNames) {
        name = pName;
        alternativeNames.addAll(Arrays.asList(pAlternativeNames));
    }

    /**
     * Name of profile.
     *
     * @return profile name
     */
    public String getName() {
        return name;
    }

    public List<String> getAlternativeNames() {
        return alternativeNames;
    }

    /**
     * Name of shared folder, where all profiles files will be unpacked/copied. This will be in most cases the same like
     * profiles name.
     *
     * @return name of profiles shared folder
     */
    public String getFolderName() {
        return name;
    }

    /**
     * String representation of maven dependencies. E.g.: 'org.codehaus.fabric3:profile-jpa:1.7:bin@zip' or
     * 'org.codehaus.fabric3:fabric3-junit:1.7@jar'.
     * <ul>
     * <li>The bin zip have to have one folder 'extensions' inside, which will contain all files. These files will be
     * will be unpacked/used</li>
     * <li>The jar file will just be copied</li>
     * </ul>
     *
     * @return list of dependencies used for specific profile
     */
    public List<PartialDependency> getFiles() {
        return files;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Profile profile = (Profile) o;

        if (name != null ? !name.equalsIgnoreCase(profile.name) : profile.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
