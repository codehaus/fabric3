package org.fabric3.assembly.configuration;

import org.fabric3.assembly.exception.ValidationException;
import org.fabric3.assembly.utils.StringUtils;

public class ServerValidator {

    public static void validate(Server pServer) {
        if (StringUtils.isBlank(pServer.getServerName())) {
            throw new ValidationException("Server's name cannot be null.");
        }

        if (null == pServer.getServerPath()) {
            throw new ValidationException("Server's build path cannot be null.");
        }
    }
}