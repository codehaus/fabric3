package org.fabric3.tests.security;

import javax.security.auth.Subject;

import org.fabric3.security.authorization.AuthorizationResult;
import org.fabric3.security.authorization.AuthorizationService;

public class TestAuthorizationService implements AuthorizationService {

    public AuthorizationResult hasRoles(Subject subject, String[] roles) {
        return new AuthorizationResult() {
            public Object getFault() {
                return null;
            }
            public boolean isSuccess() {
                return true;
            }
        };
    }

}
