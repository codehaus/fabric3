package org.fabric3.assembly.validation;

import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.exception.ValidationException;
import org.fabric3.assembly.utils.StringUtils;

public class ServerValidator {

    public void validate(Server pServer) {
        if (StringUtils.isBlank(pServer.getServerName())) {
            throw new ValidationException("Server's name cannot be null.");
        }

        if (null == pServer.getServerPath()) {
            throw new ValidationException("Server's build path cannot be null.");
        }
    }
}