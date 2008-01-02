package org.fabric3.runtime.development.host;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacherRegistry;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Wire Attacher for the client binding
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class MockWireAttacher implements TargetWireAttacher<MockWireTargetDefinition> {
    private final TargetWireAttacherRegistry targetWireAttacherRegistry;
    private final MockObjectCache mockCache;

    public MockWireAttacher(@Reference TargetWireAttacherRegistry targetWireAttacherRegistry,
                            @Reference MockObjectCache mockCache) {
        this.targetWireAttacherRegistry = targetWireAttacherRegistry;
        this.mockCache = mockCache;
    }

    @Init
    public void init() {
        targetWireAttacherRegistry.register(MockWireTargetDefinition.class, this);
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
