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
package org.fabric3.fabric.policy;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.model.type.definitions.BindingType;
import org.fabric3.model.type.definitions.ImplementationType;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;

/**
 * @version $Revision$ $Date$
 */
public class DefaultPolicyRegistryTestCase extends TestCase {
    private DefaultPolicyRegistry registry;
    private MetaDataStore store;

    public void testActivateDeactivatePolicySet() throws Exception {
        URI uri = URI.create("test");
        Contribution contribution = new Contribution(uri);
        Resource resource = new Resource(new URL("file://test"), "text/xml");
        QName name = new QName("test", "policyset");
        QNameSymbol symbol = new QNameSymbol(name);
        PolicySet policySet = new PolicySet(name, null, null, null, null, null, null);
        ResourceElement<QNameSymbol, PolicySet> element = new ResourceElement<QNameSymbol, PolicySet>(symbol, policySet);
        resource.addResourceElement(element);
        contribution.addResource(resource);

        EasyMock.expect(store.find(uri)).andReturn(contribution).atLeastOnce();
        EasyMock.replay(store);

        List<URI> uris = new ArrayList<URI>();
        uris.add(uri);

        registry.activateDefinitions(uris);
        assertNotNull(registry.getDefinition(name, PolicySet.class));

        registry.deactivateDefinitions(uris);
        assertNull(registry.getDefinition(name, PolicySet.class));
    }

    public void testActivateDeactivateIntent() throws Exception {
        URI uri = URI.create("test");
        Contribution contribution = new Contribution(uri);
        Resource resource = new Resource(new URL("file://test"), "text/xml");
        QName name = new QName("test", "intent");
        QNameSymbol symbol = new QNameSymbol(name);
        Intent intent = new Intent(name, null, null, null);
        ResourceElement<QNameSymbol, Intent> element = new ResourceElement<QNameSymbol, Intent>(symbol, intent);
        resource.addResourceElement(element);
        contribution.addResource(resource);

        EasyMock.expect(store.find(uri)).andReturn(contribution).atLeastOnce();
        EasyMock.replay(store);

        List<URI> uris = new ArrayList<URI>();
        uris.add(uri);

        registry.activateDefinitions(uris);
        assertNotNull(registry.getDefinition(name, Intent.class));

        registry.deactivateDefinitions(uris);
        assertNull(registry.getDefinition(name, Intent.class));
    }

    public void testActivateDeactivateBindingType() throws Exception {
        URI uri = URI.create("test");
        Contribution contribution = new Contribution(uri);
        Resource resource = new Resource(new URL("file://test"), "text/xml");
        QName name = new QName("test", "bindingtype");
        QNameSymbol symbol = new QNameSymbol(name);
        BindingType bindingType = new BindingType(name, null, null);
        ResourceElement<QNameSymbol, BindingType> element = new ResourceElement<QNameSymbol, BindingType>(symbol, bindingType);
        resource.addResourceElement(element);
        contribution.addResource(resource);

        EasyMock.expect(store.find(uri)).andReturn(contribution).atLeastOnce();
        EasyMock.replay(store);

        List<URI> uris = new ArrayList<URI>();
        uris.add(uri);

        registry.activateDefinitions(uris);
        assertNotNull(registry.getDefinition(name, BindingType.class));

        registry.deactivateDefinitions(uris);
        assertNull(registry.getDefinition(name, BindingType.class));
    }

    public void testActivateDeactivateImplementationType() throws Exception {
        URI uri = URI.create("test");
        Contribution contribution = new Contribution(uri);
        Resource resource = new Resource(new URL("file://test"), "text/xml");
        QName name = new QName("test", "impltype");
        QNameSymbol symbol = new QNameSymbol(name);
        ImplementationType implementationType = new ImplementationType(name, null, null);
        ResourceElement<QNameSymbol, ImplementationType> element = new ResourceElement<QNameSymbol, ImplementationType>(symbol, implementationType);
        resource.addResourceElement(element);
        contribution.addResource(resource);

        EasyMock.expect(store.find(uri)).andReturn(contribution).atLeastOnce();
        EasyMock.replay(store);

        List<URI> uris = new ArrayList<URI>();
        uris.add(uri);

        registry.activateDefinitions(uris);
        assertNotNull(registry.getDefinition(name, ImplementationType.class));

        registry.deactivateDefinitions(uris);
        assertNull(registry.getDefinition(name, ImplementationType.class));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        store = EasyMock.createMock(MetaDataStore.class);
        registry = new DefaultPolicyRegistry(store);
    }
}
