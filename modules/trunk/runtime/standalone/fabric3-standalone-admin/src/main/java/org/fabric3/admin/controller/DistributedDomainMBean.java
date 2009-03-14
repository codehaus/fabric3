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
package org.fabric3.admin.controller;

import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.management.domain.DomainMBean;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.services.lcm.LogicalComponentManager;

/**
 * @version $Revision$ $Date$
 */
public class DistributedDomainMBean extends AbstractDomainMBean implements DomainMBean {

    public DistributedDomainMBean(@Reference(name = "domain") Domain domain,
                           @Reference MetaDataStore store,
                           @Reference LogicalComponentManager lcm,
                           @Reference HostInfo info,
                           @Monitor DomainMBeanMonitor monitor) {
        super(domain, store, lcm, info, monitor);
    }

}
