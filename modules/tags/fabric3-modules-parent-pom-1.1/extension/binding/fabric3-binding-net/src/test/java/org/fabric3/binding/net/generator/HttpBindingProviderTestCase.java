/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.binding.net.generator;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.binding.net.model.HttpBindingDefinition;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.topology.DomainManager;

/**
 * @version $Revision$ $Date$
 */
public class HttpBindingProviderTestCase extends TestCase {
    private HttpBindingProvider bindingProvider;

    public void testGenerateServiceAndReference() throws Exception {
        ServiceContract<?> contract = new MockServiceContract();
        ServiceContract<?> callbackContract = new MockServiceContract();
        contract.setCallbackContract(callbackContract);
        
        LogicalComponent<?> source = new LogicalComponent(URI.create("fabric3://runtime/source"), null, null);
        source.setZone("zone1");
        LogicalComponent<?> target = new LogicalComponent(URI.create("fabric3://runtime/source"), null, null);
        target.setZone("zone2");
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("fabric3://runtime/source#reference"), referenceDefinition, source);
        ServiceDefinition serviceDefinition = new ServiceDefinition("service", contract);
        LogicalService service = new LogicalService(URI.create("fabric3://runtime/source#service"), serviceDefinition, target);
        bindingProvider.bind(reference, service);

        // verify reference
        LogicalBinding generatedReference = reference.getBindings().get(0);
        HttpBindingDefinition generatedReferenceBinding = (HttpBindingDefinition) generatedReference.getDefinition();
        assertEquals("http://localhost:8082/source/service", generatedReferenceBinding.getTargetUri().toString());

        // verify reference callback
        LogicalBinding generatedCallbackReference = reference.getCallbackBindings().get(0);
        HttpBindingDefinition generatedReferenceCallbackBinding = (HttpBindingDefinition) generatedCallbackReference.getDefinition();
        assertEquals("/source/reference", generatedReferenceCallbackBinding.getTargetUri().toString());

        // verify service
        LogicalBinding generatedService = service.getBindings().get(0);
        HttpBindingDefinition generatedServiceBinding = (HttpBindingDefinition) generatedService.getDefinition();
        assertEquals("/source/service", generatedServiceBinding.getTargetUri().toString());

        // verify service callback
        LogicalBinding generatedCallbackService = service.getCallbackBindings().get(0);
        HttpBindingDefinition generatedCallbackServiceBinding = (HttpBindingDefinition) generatedCallbackService.getDefinition();
        assertEquals("http://localhost:8081/source/reference", generatedCallbackServiceBinding.getTargetUri().toString());

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DomainManager manager = EasyMock.createMock(DomainManager.class);
        manager.getTransportMetaData("zone2", String.class, "binding.net.http");
        EasyMock.expectLastCall().andReturn("localhost:8082");
        manager.getTransportMetaData("zone1", String.class, "binding.net.http");
        EasyMock.expectLastCall().andReturn("localhost:8081");
        EasyMock.replay(manager);
        bindingProvider = new HttpBindingProvider();
        bindingProvider.setDomainManager(manager);
    }

    private class MockServiceContract extends ServiceContract {

        public boolean isAssignableFrom(ServiceContract serviceContract) {
            return false;
        }

        public String getQualifiedInterfaceName() {
            return null;
        }
    }
}
