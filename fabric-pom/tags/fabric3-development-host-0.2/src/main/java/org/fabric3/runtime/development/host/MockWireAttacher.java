package org.fabric3.runtime.development.host;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * WireAttacher for the client binding
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class MockWireAttacher implements WireAttacher<PhysicalWireSourceDefinition, MockWireTargetDefinition> {
    private MockObjectCache mockCache;
    private WireAttacherRegistry registry;

    public MockWireAttacher(@Reference WireAttacherRegistry registry,
                            @Reference MockObjectCache mockCache) {
        this.registry = registry;
        this.mockCache = mockCache;
    }

    @Init
    public void init() {
        registry.register(MockWireTargetDefinition.class, this);
    }

    public void attachToSource(PhysicalWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) {
        throw new UnsupportedOperationException();
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, MockWireTargetDefinition target, Wire wire)
            throws WiringException {
        URI targetUri = target.getUri();
        URI sourceUri = source.getUri();
        String name = target.getMockName();
        MockDefinition mock = mockCache.getMockDefinition(name);
        if (mock == null) {
            throw new MockObjectNotRegisteredException("Mock not registered for", sourceUri, targetUri);
        }
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            PhysicalOperationDefinition op = entry.getKey();
            InvocationChain chain = entry.getValue();
            Method m = findMethod(mock.getInterfaze(), op, sourceUri, targetUri);
            chain.addInterceptor(new MockInvokerInterceptor(m, mock.getMock()));
        }

    }

    private Method findMethod(Class<?> interfaze, PhysicalOperationDefinition operation, URI sourceUri, URI targetUri)
            throws WireAttachException {
        List<String> params = operation.getParameters();
        Class<?>[] paramTypes = new Class<?>[params.size()];
        for (int i = 0; i < params.size(); i++) {
            String param = params.get(i);
            try {
                paramTypes[i] = Class.forName(param);
            } catch (ClassNotFoundException e) {
                throw new WireAttachException("Implementation class not found", sourceUri, targetUri, e);
            }
        }
        try {
            return interfaze.getMethod(operation.getName(), paramTypes);
        } catch (NoSuchMethodException e) {
            throw new WireAttachException("No matching method found", sourceUri, targetUri, e);
        }

    }
}
