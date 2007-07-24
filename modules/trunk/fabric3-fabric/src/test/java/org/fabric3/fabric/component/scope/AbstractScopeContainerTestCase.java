package org.fabric3.fabric.component.scope;

import junit.framework.TestCase;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.component.TargetResolutionException;
import org.fabric3.spi.component.TargetDestructionException;
import org.fabric3.spi.Lifecycle;

import org.easymock.EasyMock;

/**
 * @version $Rev$ $Date$
 */
public class AbstractScopeContainerTestCase extends TestCase {
    private static final String scopeName = "TEST";
    private static final Scope<Object> scope = new Scope<Object>(scopeName, Object.class);
    private ScopeContainerMonitor monitor;
    private ScopeRegistry scopeRegistry;
    private AbstractScopeContainer<?> scopeContainer;

    public void testScope() {
        assertSame(scope, scopeContainer.getScope());
    }

    public void testScopeRegistersOnStartAndUnregistersOnStop() {
        scopeRegistry.register(scopeContainer);
        EasyMock.replay(scopeRegistry);
        assertEquals(Lifecycle.UNINITIALIZED, scopeContainer.getLifecycleState());
        scopeContainer.start();
        assertEquals(Lifecycle.RUNNING, scopeContainer.getLifecycleState());
        EasyMock.verify(scopeRegistry);

        EasyMock.reset(scopeRegistry);
        scopeRegistry.unregister(scopeContainer);
        EasyMock.replay(scopeRegistry);
        scopeContainer.stop();
        assertEquals(Lifecycle.STOPPED, scopeContainer.getLifecycleState());
        EasyMock.verify(scopeRegistry);
    }

    protected void setUp() throws Exception {
        super.setUp();

        scopeRegistry = EasyMock.createMock(ScopeRegistry.class);
        monitor = EasyMock.createMock(ScopeContainerMonitor.class);
        scopeContainer = new TestScopeContainer<Object>(scope, monitor);
        scopeContainer.setScopeRegistry(scopeRegistry);
    }

    private static class TestScopeContainer<KEY> extends AbstractScopeContainer<KEY> {

        public TestScopeContainer(Scope<KEY> scope, ScopeContainerMonitor monitor) {
            super(scope, monitor);
        }

        public <T> InstanceWrapper<T> getWrapper(AtomicComponent<T> component, WorkContext workContext) throws TargetResolutionException {
            throw new UnsupportedOperationException();
        }

        public <T> void returnWrapper(AtomicComponent<T> component, WorkContext workContext, InstanceWrapper<T> wrapper) throws TargetDestructionException {
            throw new UnsupportedOperationException();
        }
    }
}
