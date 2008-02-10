package org.fabric3.binding.ejb.transport;

import java.net.URI;

import org.fabric3.binding.ejb.model.logical.EjbBindingDefinition;
import org.fabric3.binding.ejb.wire.EjbResolver;
import org.fabric3.scdl.Signature;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Revision Date: Oct 29, 2007 Time: 6:29:57 PM
 */
public class EjbTargetInterceptorFactory {

    private EjbBindingDefinition bd;
    private EjbResolver resolver;

    private ScopeContainer scopeContainer;
    private EjbStatefulComponent statefulComponent;


    public EjbTargetInterceptorFactory(EjbBindingDefinition bd, EjbResolver resolver,
                                       ScopeRegistry scopeRegistry, URI id) {
        this.bd = bd;
        this.resolver = resolver;

        if(!bd.isStateless()) {
            //TODO: do I need to start the StatefulComponent?
            statefulComponent = new EjbStatefulComponent(id);
            scopeContainer = scopeRegistry.getScopeContainer(Scope.CONVERSATION);
            scopeContainer.register(statefulComponent);
        }
    }

    public Interceptor getEjbTargetInterceptor(Signature signature) {
        if(bd.isStateless()) {
            return new EjbStatelessTargetInterceptor(signature, resolver);
        } else {
            return new EjbStatefulTargetInterceptor(signature, resolver, scopeContainer, statefulComponent);
        }

    }

}
