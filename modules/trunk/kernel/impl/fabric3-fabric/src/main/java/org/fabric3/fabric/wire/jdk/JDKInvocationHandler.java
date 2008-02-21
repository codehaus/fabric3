package org.fabric3.fabric.wire.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.UUID;

import org.osoa.sca.Conversation;
import org.osoa.sca.ServiceReference;
import org.osoa.sca.ServiceUnavailableException;

import org.fabric3.fabric.wire.NoMethodForOperationException;
import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.component.CallFrame;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.TargetInvocationException;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;

/**
 * Dispatches to a target through a wire.
 *
 * @version $Rev$ $Date$
 */
public final class JDKInvocationHandler<B> implements InvocationHandler, ServiceReference<B> {
    private final Class<B> businessInterface;
    private final B proxy;
    private final boolean conversational;
    private final Map<Method, InvocationChain> chains;
    private final ScopeContainer<Conversation> scopeContainer;

    private ConversationImpl conversation;
    private Object userConversationId;
    private String callbackUri;

    public JDKInvocationHandler(Class<B> businessInterface,
                                String callbackUri,
                                boolean conversational,
                                Map<Method, InvocationChain> mapping,
                                ScopeContainer<Conversation> scopeContainer)
            throws NoMethodForOperationException {
        this.callbackUri = callbackUri;
        assert mapping != null;
        this.businessInterface = businessInterface;
        this.proxy = businessInterface.cast(
                Proxy.newProxyInstance(businessInterface.getClassLoader(), new Class[]{businessInterface}, this));
        this.conversational = conversational;
        this.chains = mapping;
        this.scopeContainer = scopeContainer;
    }

    public B getService() {
        return proxy;
    }

    public ServiceReference<B> getServiceReference() {
        return this;
    }

    public boolean isConversational() {
        return conversational;
    }

    public Class<B> getBusinessInterface() {
        return businessInterface;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public Object getConversationID() {
        return userConversationId;
    }

    public void setConversationID(Object conversationId) throws IllegalStateException {
        if (conversation != null) {
            throw new IllegalStateException("A conversation is already active");
        }
        userConversationId = conversationId;
    }

    public Object getCallbackID() {
        throw new UnsupportedOperationException();
    }

    public void setCallbackID(Object callbackID) {
        throw new UnsupportedOperationException();
    }

    public Object getCallback() {
        throw new UnsupportedOperationException();
    }

    public void setCallback(Object callback) {
        throw new UnsupportedOperationException();
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        InvocationChain chain = chains.get(method);
        if (chain == null) {
            return handleProxyMethod(method);
        }

        Interceptor headInterceptor = chain.getHeadInterceptor();
        assert headInterceptor != null;

        WorkContext workContext = PojoWorkContextTunnel.getThreadWorkContext();
        Conversation oldConversation;
        if (conversational) {
            oldConversation = workContext.getScopeIdentifier(Scope.CONVERSATION);
            if (conversation == null) {
                conversation = new ConversationImpl(createConversationID());
                workContext.setScopeIdentifier(Scope.CONVERSATION, conversation);
                scopeContainer.startContext(workContext, null);
            } else {
                workContext.setScopeIdentifier(Scope.CONVERSATION, conversation);
            }
        } else {
            oldConversation = null;
        }
        CallFrame frame = new CallFrame(callbackUri, conversation, oldConversation);
        workContext.addCallFrame(frame);
        // send the invocation down the wire
        Message msg = new MessageImpl();
        msg.setBody(args);
        msg.setWorkContext(workContext);
        try {
            // dispatch the wire down the chain and get the response
            Message resp;
            try {
                resp = headInterceptor.invoke(msg);
            } catch (ServiceUnavailableException e) {
                // simply rethrow ServiceUnavailableExceptions
                throw e;
            } catch (RuntimeException e) {
                // wrap other exceptions raised by the runtime
                throw new ServiceUnavailableException(e);
            }

            // handle response from the application, returning or throwing is as appropriate
            Object body = resp.getBody();
            if (resp.isFault()) {
                throw (Throwable) body;
            } else {
                return body;
            }
        } finally {
            if (conversational) {
                PhysicalOperationDefinition operation = chain.getPhysicalOperation();
                if (operation.getConversationSequence() == Operation.CONVERSATION_END) {
                    scopeContainer.stopContext(workContext);
                    conversation = null;
                }
                workContext.setScopeIdentifier(Scope.CONVERSATION, oldConversation);
            }
            workContext.popCallFrame();
        }

    }

    /**
     * Creates a new conversational id
     *
     * @return the conversational id
     */
    private Object createConversationID() {
        if (userConversationId != null) {
            return userConversationId;
        } else {
            return UUID.randomUUID().toString();
        }
    }

    private Object handleProxyMethod(Method method) throws TargetInvocationException {
        if (method.getParameterTypes().length == 0 && "toString".equals(method.getName())) {
            return "[Proxy - " + Integer.toHexString(hashCode()) + "]";
        } else if (method.getDeclaringClass().equals(Object.class)
                && "equals".equals(method.getName())) {
            // TODO implement
            throw new UnsupportedOperationException();
        } else if (Object.class.equals(method.getDeclaringClass())
                && "hashCode".equals(method.getName())) {
            return hashCode();
            // TODO beter hash algorithm
        }
        throw new TargetInvocationException("Operation not configured", method.getName());
    }
}
