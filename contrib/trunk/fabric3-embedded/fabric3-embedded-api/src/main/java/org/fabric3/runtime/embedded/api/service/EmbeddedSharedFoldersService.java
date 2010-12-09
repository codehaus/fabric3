package org.fabric3.runtime.embedded.api.service;

import org.fabric3.host.runtime.ScanException;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3StartupException;

import java.io.File;
import java.io.IOException;

/**
 * @author Michal Capo
 */
public interface EmbeddedSharedFoldersService {

    void initialize() throws ScanException, IOException, EmbeddedFabric3StartupException;

    File getBootFolder();

    File getExtensionsFolder();

    File getHostFolder();

    File getLibFolder();

    File getProfileJmsFolder();

    File getProfileJpaFolder();

    File getProfileNetFolder();

    File getProfileTimerFolder();

    File getProfileWebFolder();
}
