package org.fabric3.assembly.configuration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.Version;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michal Capo
 */
public class Server {

    public static final String SERVER_DEFAULT_NAME = "default-server";

    private String serverName;

    private File serverPath;

    private List<Profile> profiles = new ArrayList<Profile>();

    private Version version;

    private UpdatePolicy updatePolicy;

    public Server(String pServerName, File pServerPath, Version pVersion, UpdatePolicy pUpdatePolicy, Profile... pProfiles) {
        if (null == pServerName) {
            serverName = SERVER_DEFAULT_NAME;
        } else {
            serverName = pServerName;
        }
        serverPath = pServerPath;
        version = pVersion;
        updatePolicy = pUpdatePolicy;
        if (null != pProfiles) {
            profiles.addAll(Arrays.asList(pProfiles));
        }
    }

    public String getServerName() {
        return serverName;
    }

    public File getServerPath() {
        return serverPath;
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public Version getVersion() {
        return version;
    }

    public UpdatePolicy getUpdatePolicy() {
        return updatePolicy;
    }

    public void validate() {
        ServerValidator.validate(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("serverName", serverName).
                append("serverPath", serverPath).
                append("profiles", profiles).
                append("version", version).
                append("updatePolicy", updatePolicy).
                toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Server server = (Server) o;

        if (serverName != null ? !serverName.equals(server.serverName) : server.serverName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return serverName != null ? serverName.hashCode() : 0;
    }
}
