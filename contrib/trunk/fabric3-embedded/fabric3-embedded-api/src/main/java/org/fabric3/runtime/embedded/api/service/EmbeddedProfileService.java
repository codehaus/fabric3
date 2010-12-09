package org.fabric3.runtime.embedded.api.service;

import org.fabric3.runtime.embedded.api.EmbeddedProfile;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * @author Michal Capo
 */
public interface EmbeddedProfileService {

    void initialize();

    void addProfile(EmbeddedProfile profile);

    Set<EmbeddedProfile> getProfiles();

    List<File> getProfilesFiles();

}
