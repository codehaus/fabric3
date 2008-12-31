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
package org.fabric3.fabric.services.lcm;

import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.component.Autowire;

/**
 * A non-persistent LogicalComponentStore used by the application domain.
 *
 * @version $Rev$ $Date$
 */
public class ApplicationLogicalComponentStore extends TransientLogicalComponentStore {
    private ApplicationStoreMonitor monitor;
    private String autowireValue;

    @Reference
    public void setHostInfo(@Reference HostInfo info) {
        setDomainUri(info.getDomain());
    }

    @Monitor
    public void setMonitor(ApplicationStoreMonitor monitor) {
        this.monitor = monitor;
    }

    @Property(required = false)
    public void setAutowire(String value) {
        autowireValue = value;
    }

    @Init
    public void init() {
        if (autowireValue == null) {
            return;
        }
        Autowire autowire;
        // can't use Enum.valueOf(..) as INHERITED is not a valid value for the domain composite
        if ("ON".equalsIgnoreCase(autowireValue.trim())) {
            autowire = Autowire.ON;
        } else if ("OFF".equalsIgnoreCase(autowireValue.trim())) {
            autowire = Autowire.OFF;
        } else {
            monitor.invalidAutowireValue(autowireValue);
            autowire = Autowire.OFF;
        }
        setAutowire(autowire);
    }

}
