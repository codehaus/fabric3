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
package org.fabric3.fabric.binding;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.binding.BindingProvider;

/**
 * @version $Revision$ $Date$
 */
public class ConfigurableBindingSelectionStrategyTestCase extends TestCase {

    public void testSelectionOrder() throws Exception {
        ConfigurableBindingSelectionStrategy strategy = new ConfigurableBindingSelectionStrategy();
        List<QName> order = new ArrayList<QName>();
        QName bar = new QName("foo", "bar");
        order.add(bar);
        QName baz = new QName("foo", "baz");
        order.add(baz);
        strategy.setScaBindingOrder(order);

        BindingProvider bazProvider = EasyMock.createMock(BindingProvider.class);
        EasyMock.expect(bazProvider.getType()).andReturn(baz);
        EasyMock.replay(bazProvider);
        BindingProvider barProvider = EasyMock.createMock(BindingProvider.class);
        EasyMock.expect(barProvider.getType()).andReturn(bar);
        EasyMock.replay(barProvider);

        List<BindingProvider> providers = new ArrayList<BindingProvider>();
        providers.add(bazProvider);
        providers.add(barProvider);
        strategy.order(providers);
        assertEquals(barProvider, providers.get(0));
        assertEquals(bazProvider, providers.get(1));

    }

    public void testNoConfiguredOrderSelection() throws Exception {
        ConfigurableBindingSelectionStrategy strategy = new ConfigurableBindingSelectionStrategy();
        QName bar = new QName("foo", "bar");
        QName baz = new QName("foo", "baz");

        BindingProvider bazProvider = EasyMock.createMock(BindingProvider.class);
        EasyMock.expect(bazProvider.getType()).andReturn(baz);
        EasyMock.replay(bazProvider);
        BindingProvider barProvider = EasyMock.createMock(BindingProvider.class);
        EasyMock.expect(barProvider.getType()).andReturn(bar);
        EasyMock.replay(barProvider);

        List<BindingProvider> providers = new ArrayList<BindingProvider>();
        providers.add(bazProvider);
        providers.add(barProvider);
        strategy.order(providers);
        assertEquals(bazProvider, providers.get(0));
        assertEquals(barProvider, providers.get(1));
    }

    public void testBadConfigurationSelectionOrder() throws Exception {
        ConfigurableBindingSelectionStrategy strategy = new ConfigurableBindingSelectionStrategy();
        List<QName> order = new ArrayList<QName>();
        QName nonExistent = new QName("foo", "nonExistent");
        order.add(nonExistent);
        QName bar = new QName("foo", "bar");
        order.add(bar);
        strategy.setScaBindingOrder(order);

        QName baz = new QName("foo", "baz");

        List<BindingProvider> providers = new ArrayList<BindingProvider>();
        BindingProvider bazProvider = EasyMock.createMock(BindingProvider.class);
        EasyMock.expect(bazProvider.getType()).andReturn(baz);
        EasyMock.replay(bazProvider);
        BindingProvider barProvider = EasyMock.createMock(BindingProvider.class);
        EasyMock.expect(barProvider.getType()).andReturn(bar);
        EasyMock.replay(barProvider);
        providers.add(bazProvider);
        providers.add(barProvider);

        strategy.order(providers);
        assertEquals(bazProvider, providers.get(0));
        assertEquals(barProvider, providers.get(1));
    }

}
