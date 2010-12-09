package org.fabric3.runtime.embedded.service;

import org.fabric3.runtime.embedded.api.EmbeddedUpdatePolicy;
import org.fabric3.runtime.embedded.api.service.EmbeddedUpdatePolicyService;

/**
 * @author Michal Capo
 */
public class EmbeddedUpdatePolicyServiceImpl implements EmbeddedUpdatePolicyService {

    private EmbeddedUpdatePolicy mUpdatePolicy;

    public void initialize() {
        // no-op
    }

    public void setUpdatePolicy(EmbeddedUpdatePolicy updatePolicy) {
        mUpdatePolicy = updatePolicy;
    }

    public EmbeddedUpdatePolicy getUpdatePolicy() {
        // default update policy is daily
        if (null == mUpdatePolicy) {
            mUpdatePolicy = EmbeddedUpdatePolicy.DAILY;
        }

        return mUpdatePolicy;
    }
}
