package org.fabric3.runtime.component;

import org.fabric3.api.annotation.scope.Scopes;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Scope;

/**
 * @author Michal Capo
 */
@EagerInit
@Scope(Scopes.COMPOSITE)
public class PutSomethingToSystemOut {

    @Init
    public void init() {
        System.out.println("Hello from test composite");
    }
}
