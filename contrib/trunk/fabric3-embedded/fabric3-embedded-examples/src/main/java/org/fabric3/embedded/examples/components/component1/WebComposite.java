package org.fabric3.embedded.examples.components.component1;

import org.fabric3.api.annotation.scope.Scopes;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Scope;

/**
 * @author Michal Capo
 */
@EagerInit
@Scope(Scopes.COMPOSITE)
public class WebComposite {

    @Init
    public void init() {
        System.out.println("Hello from web composite");
    }
}
