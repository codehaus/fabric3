package org.fabric3.fabric.binding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        Map<QName, BindingProvider> providers = new HashMap<QName, BindingProvider>();
        BindingProvider bazProvider = EasyMock.createMock(BindingProvider.class);
        EasyMock.replay(bazProvider);
        BindingProvider barProvider = EasyMock.createMock(BindingProvider.class);
        EasyMock.replay(barProvider);
        providers.put(baz, bazProvider);
        providers.put(bar, barProvider);

        assertEquals(barProvider, strategy.select(providers));

    }

    public void testNoConfiguredOrderSelection() throws Exception {
        ConfigurableBindingSelectionStrategy strategy = new ConfigurableBindingSelectionStrategy();
        List<QName> order = new ArrayList<QName>();

        strategy.setScaBindingOrder(order);

        QName bar = new QName("foo", "bar");
        QName baz = new QName("foo", "baz");

        Map<QName, BindingProvider> providers = new HashMap<QName, BindingProvider>();
        BindingProvider bazProvider = EasyMock.createMock(BindingProvider.class);
        EasyMock.replay(bazProvider);
        BindingProvider barProvider = EasyMock.createMock(BindingProvider.class);
        EasyMock.replay(barProvider);
        providers.put(baz, bazProvider);
        providers.put(bar, barProvider);

        assertNotNull(strategy.select(providers));

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

        Map<QName, BindingProvider> providers = new HashMap<QName, BindingProvider>();
        BindingProvider bazProvider = EasyMock.createMock(BindingProvider.class);
        EasyMock.replay(bazProvider);
        BindingProvider barProvider = EasyMock.createMock(BindingProvider.class);
        EasyMock.replay(barProvider);
        providers.put(baz, bazProvider);
        providers.put(bar, barProvider);

        assertEquals(barProvider, strategy.select(providers));

    }

}
