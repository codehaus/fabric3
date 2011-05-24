package org.fabric3.assembly.validation;

import org.fabric3.assembly.configuration.Profile;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.exception.ValidationException;
import org.fabric3.assembly.utils.StringUtils;

public class ServerValidator {

    public void validate(Server pServer, ProfileValidator pProfileValidator) {
        String serverName = pServer.getServerName();
        if (StringUtils.isBlank(serverName)) {
            throw new ValidationException("Server's name cannot be null.");
        }

        if (null == pServer.getServerPath()) {
            throw new ValidationException("Server's build path cannot be null.");
        }

        if (null == pServer.getVersion()) {
            throw new ValidationException("Servers ''{0}'' version is null.", serverName);
        }

        if (null == pServer.getUpdatePolicy()) {
            throw new ValidationException("Servers ''{0}'' update policy is null.", serverName);
        }

        for (Profile profile : pServer.getProfiles()) {
            pProfileValidator.validate(profile);
        }
        ValidationHelper.validateSameProfileName(serverName, pServer.getProfiles());

    }
}