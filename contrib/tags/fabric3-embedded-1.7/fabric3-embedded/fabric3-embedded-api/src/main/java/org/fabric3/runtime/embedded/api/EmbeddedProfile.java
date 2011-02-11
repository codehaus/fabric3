package org.fabric3.runtime.embedded.api;

import java.util.List;

/**
 * Fabrics profile specified via maven dependencies.
 *
 * @author Michal Capo
 */
public interface EmbeddedProfile {

    /**
     * Name of profile.
     *
     * @return profile name
     */
    public String getName();

    /**
     * Set name to profile.
     *
     * @param pName of profile
     */
    void setName(String pName);

    /**
     * Name of shared folder, where all profiles files will be unpacked/copied. This will be in most cases the same like
     * profiles name.
     *
     * @return name of profiles shared folder
     */
    public String getFolderName();

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
    public List<String> getFiles();

    /**
     * Physical folder which will contain all copied/unpacked files and will be used for fabric startup
     *
     * @return shared folder for profile
     */
    //public File getProfileFolder();

    /**
     * Set folder where all profiles files are located.
     *
     * @param pProfileFolder physical existing folder
     */
    //void setProfileFolder(File pProfileFolder);

    /**
     * Indicates if this profile is already attached to some share folder.
     *
     * @return <code>true</code> if shared folder is pointing to existing folder, otherwise it return <code>false</code>
     */
    //public boolean isAttached();
}
