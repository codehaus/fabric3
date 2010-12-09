package org.fabric3.runtime.embedded.api.service;

import org.fabric3.runtime.embedded.api.EmbeddedUpdatePolicy;

/**
 * @author Michal Capo
 */
public interface EmbeddedUpdatePolicyService {

    void initialize();

    void setUpdatePolicy(EmbeddedUpdatePolicy updatePolicy);

    EmbeddedUpdatePolicy getUpdatePolicy();

}
