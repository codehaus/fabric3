package org.fabric3.runtime.embedded.api.service;

import org.fabric3.runtime.embedded.api.EmbeddedUpdatePolicy;

/**
 * Update policy holder. Used by dependency resolver.
 *
 * @author Michal Capo
 */
public interface EmbeddedDependencyUpdatePolicy {

    /**
     * Set update policy used by dependency resolver.
     *
     * @param updatePolicy to be set
     */
    void setUpdatePolicy(EmbeddedUpdatePolicy updatePolicy);

    /**
     * Get update policy hold by this container.
     *
     * @return update policy
     */
    EmbeddedUpdatePolicy getUpdatePolicy();

}
