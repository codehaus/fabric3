package org.fabric3.runtime.embedded.api.service;

import org.fabric3.runtime.embedded.exception.EmbeddedFabric3StartupException;

import java.io.File;

/**
 * Resolve/find dependency of some fabric3 module.
 *
 * @author Michal Capo
 */
public interface EmbeddedDependencyResolver {

    /**
     * Find dependency in dependency manager storage folder. E.g.: '.m2/repository' when maven2 is used.
     *
     * @param dependency you want to find
     * @return physical file if these dependency was found
     * @throws EmbeddedFabric3StartupException
     *          when dependency cannot be found. These may also mean that dependency is not downloaded yet.
     */
    File findFile(String dependency) throws EmbeddedFabric3StartupException;

}
