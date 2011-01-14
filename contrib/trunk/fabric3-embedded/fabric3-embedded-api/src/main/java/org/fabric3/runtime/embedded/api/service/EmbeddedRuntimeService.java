package org.fabric3.runtime.embedded.api.service;

import org.fabric3.runtime.embedded.api.EmbeddedRuntime;

import java.util.Collection;

/**
 * @author Michal Capo
 */
public interface EmbeddedRuntimeService {

    ThreadGroup getRuntimesGroup();

    void initialize();

    void addRuntime(EmbeddedRuntime runtime);

    EmbeddedRuntime getController();

    Collection<EmbeddedRuntime> getParticipants();

    Collection<EmbeddedRuntime> getAllRuntimes();

    int getRuntimesCount();

    EmbeddedRuntime getRuntimeByName(String name);

}
