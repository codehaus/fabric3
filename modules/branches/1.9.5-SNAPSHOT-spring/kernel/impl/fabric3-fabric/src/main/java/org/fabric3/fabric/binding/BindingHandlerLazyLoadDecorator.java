package org.fabric3.fabric.binding;

import java.net.URI;
import javax.xml.namespace.QName;

import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.ServiceUnavailableException;

import org.fabric3.spi.binding.handler.BindingHandler;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.ScopedComponent;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContextTunnel;

/**
 * A {@link BindingHandler} decorator that resolves the target {@link BindingHandler} in a lazy fashion. Lazy loading is used to ensure the full
 * initialization of the target instance.
 *
 * @version $Rev$ $Date$
 */
public class BindingHandlerLazyLoadDecorator<T> implements BindingHandler<T> {
    private URI handlerUri;
    private ComponentManager componentManager;
    private volatile ScopedComponent delegate;

    public BindingHandlerLazyLoadDecorator(URI handlerUri, ComponentManager componentManager) {
        this.handlerUri = handlerUri;
        this.componentManager = componentManager;
    }

    public QName getType() {
        return inject().getType();
    }

    public void handleOutbound(Message message, T context) {
        inject().handleOutbound(message, context);

    }

    public void handleInbound(T context, Message message) {
        inject().handleInbound(context, message);
    }

    @SuppressWarnings("unchecked")
    private BindingHandler<T> inject() {
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    Component component = componentManager.getComponent(handlerUri);
                    if (component == null) {
                        throw new ServiceUnavailableException("Handler component not found: " + handlerUri);
                    }
                    if (!(component instanceof ScopedComponent)) {
                        throw new ServiceRuntimeException("Handler component must be a scoped component type: " + handlerUri);
                    }
                    delegate = (ScopedComponent) component;
                }
            }
        }
        try {
            // resolve the instance on every invocation so that stateless scoped components receive a new instance
            return (BindingHandler<T>) delegate.getInstance(WorkContextTunnel.getThreadWorkContext());
        } catch (InstanceLifecycleException e) {
            throw new ServiceRuntimeException(e);
        }
    }

}
