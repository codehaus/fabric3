package org.fabric3.assembly.configuration;

import org.fabric3.assembly.profile.Profile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michal Capo
 */
public class ServerConfiguration {

    public static final String SERVER_DEFAULT_NAME = "default-server";

    private String serverName;

    private File serverPath;

    private List<Profile> profiles = new ArrayList<Profile>();

    public ServerConfiguration(String pServerName, File pServerPath, Profile... pProfiles) {
        if (null == pServerName) {
            serverName = SERVER_DEFAULT_NAME;
        } else {
            serverName = pServerName;
        }
        serverPath = pServerPath;
        if (null != pProfiles) {
            profiles.addAll(Arrays.asList(pProfiles));
        }
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String pServerName) {
        serverName = pServerName;
    }

    public File getServerPath() {
        return serverPath;
    }

    public void setServerPath(String pServerPath) {
        serverPath = new File(pServerPath);
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Profile... pProfiles) {
        profiles = Arrays.asList(pProfiles);
    }
}
