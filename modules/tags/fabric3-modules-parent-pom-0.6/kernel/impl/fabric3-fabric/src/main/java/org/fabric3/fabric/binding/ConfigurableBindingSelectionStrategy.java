package org.fabric3.fabric.binding;

import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Property;

import org.fabric3.spi.binding.BindingProvider;
import org.fabric3.spi.binding.BindingSelectionStrategy;

/**
 * A BindingSelectionStrategy that makes a selection based on an ordered list of bindings. This list is provided via a property which can be sourced
 * from a runtime domain level property specified in systemConfig.xml.
 *
 * @version $Revision$ $Date$
 */
public class ConfigurableBindingSelectionStrategy implements BindingSelectionStrategy {
    private List<QName> scaBindingOrder;

    @Property
    public void setScaBindingOrder(List<QName> scaBindingOrder) {
        this.scaBindingOrder = scaBindingOrder;
    }

    public BindingProvider select(Map<QName, BindingProvider> providers) {
        if (scaBindingOrder == null) {
            return providers.values().iterator().next();
        }
        BindingProvider provider = null;
        for (QName name : scaBindingOrder) {
            provider = providers.get(name);
            if (provider != null) {
                break;
            }
        }
        if (provider == null) {
            // none of the ordered bindings were found, default to the first one
            // TODO support exclusions
            return providers.values().iterator().next();
        }
        return provider;
    }
}
