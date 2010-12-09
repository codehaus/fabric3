package org.fabric3.runtime.embedded.service;

import org.fabric3.runtime.embedded.api.EmbeddedProfile;
import org.fabric3.runtime.embedded.api.service.EmbeddedProfileService;
import org.fabric3.runtime.embedded.api.service.EmbeddedSharedFoldersService;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3SetupException;
import org.fabric3.runtime.embedded.util.FileSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Michal Capo
 */
public class EmbeddedProfileServiceImpl implements EmbeddedProfileService {

    private Set<EmbeddedProfile> profiles = new HashSet<EmbeddedProfile>();

    private EmbeddedSharedFoldersService mSharedFolderService;

    public EmbeddedProfileServiceImpl(EmbeddedSharedFoldersService sharedFolderService) {
        mSharedFolderService = sharedFolderService;

        if (null == mSharedFolderService) {
            throw new EmbeddedFabric3SetupException("Shared folders service cannot be null.");
        }
    }

    public void initialize() {
        // no-op
    }

    public void addProfile(EmbeddedProfile profile) {
        if (EmbeddedProfile.ALL == profile) {
            profiles.addAll(Arrays.asList(EmbeddedProfile.values()));
            profiles.remove(EmbeddedProfile.ALL);
        } else {
            profiles.add(profile);
        }
    }

    public Set<EmbeddedProfile> getProfiles() {
        return profiles;
    }

    public List<File> getProfilesFiles() {
        if (0 == getProfiles().size()) {
            return new ArrayList<File>();
        }

        List<File> result = new ArrayList<File>();
        for (EmbeddedProfile prof : getProfiles()) {
            result.addAll(collectFilesForProfile(mSharedFolderService, prof));
        }

        return result;
    }

    private List<File> collectFilesForProfile(final EmbeddedSharedFoldersService sharedFoldersService, final EmbeddedProfile profile) {
        switch (profile) {
            case JMS:
                return FileSystem.filesIn(sharedFoldersService.getProfileJmsFolder());
            case JPA:
                return FileSystem.filesIn(sharedFoldersService.getProfileJpaFolder());
            case NET:
                return FileSystem.filesIn(sharedFoldersService.getProfileNetFolder());
            case TIMER:
                return FileSystem.filesIn(sharedFoldersService.getProfileTimerFolder());
            case WEB:
                return FileSystem.filesIn(sharedFoldersService.getProfileWebFolder());
            default:
                throw new EmbeddedFabric3SetupException(String.format("Profile %1$s cannot be resolved.", profile));
        }
    }
}
