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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
    private Map<QName, Integer> bindingOrder;
    private BindingProviderComparator COMPARATOR = new BindingProviderComparator();

    @Property
    public void setScaBindingOrder(List<QName> order) {
        this.bindingOrder = new HashMap<QName, Integer>(order.size());
        for (int i = 0; i < order.size(); i++) {
            QName name = order.get(i);
            bindingOrder.put(name, i);
        }
    }

    public void order(List<BindingProvider> providers) {
        if (bindingOrder == null || providers.isEmpty()) {
            return;
        }
        Collections.sort(providers, COMPARATOR);
    }


    private class BindingProviderComparator implements Comparator<BindingProvider> {
        public int compare(BindingProvider one, BindingProvider two) {
            Integer posOne = bindingOrder.get(one.getType());
            if (posOne == null) {
                posOne = -1;
            }
            Integer posTwo = bindingOrder.get(two.getType());
            if (posTwo == null) {
                posTwo = -1;
            }
            return posOne - posTwo;
        }
    }
}
