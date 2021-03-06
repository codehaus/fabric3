/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.fabric.generator.policy;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.stream.Source;
import org.fabric3.host.stream.UrlSource;
import org.fabric3.model.type.definitions.BindingType;
import org.fabric3.model.type.definitions.ImplementationType;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.IntentType;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.model.type.definitions.Qualifier;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;

/**
 * @version $Rev$ $Date$
 */
public class DefaultPolicyRegistryTestCase extends TestCase {
    private DefaultPolicyRegistry registry;
    private MetaDataStore store;

    public void testActivateDeactivatePolicySet() throws Exception {
        URI uri = URI.create("test");
        Contribution contribution = new Contribution(uri);
        URL url = new URL("file://test");
        Source source = new UrlSource(url);
        Resource resource = new Resource(contribution, source, "text/xml");
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
        URL url = new URL("file://test");
        Source source = new UrlSource(url);
        Resource resource = new Resource(contribution, source, "text/xml");
        QName name = new QName("test", "intent");
        QNameSymbol symbol = new QNameSymbol(name);
        Intent intent = new Intent(name, null, null, Collections.<Qualifier>emptySet(), false, null, IntentType.INTERACTION, false);
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
        URL url = new URL("file://test");
        Source source = new UrlSource(url);
        Resource resource = new Resource(contribution, source, "text/xml");
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
        URL url = new URL("file://test");
        Source source = new UrlSource(url);
        Resource resource = new Resource(contribution, source, "text/xml");
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
