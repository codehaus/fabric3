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
package org.fabric3.jpa.runtime;

import javax.transaction.TransactionManager;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.ObjectCreationException;

/**
 * Creates StatefulEntityManagerProxy intances.
 *
 * @version $Revision$ $Date$
 */
public class StatefulEntityManagerProxyFactory implements ObjectFactory<StatefulEntityManagerProxy> {
    private String unitName;
    private EntityManagerService service;
    private TransactionManager tm;
    private boolean extended;

    public StatefulEntityManagerProxyFactory(String unitName, boolean extended, EntityManagerService service, TransactionManager tm) {
        this.service = service;
        this.tm = tm;
        this.extended = extended;
        this.unitName = unitName;
    }

    public StatefulEntityManagerProxy getInstance() throws ObjectCreationException {
        return new StatefulEntityManagerProxy(unitName, extended, service, tm);
    }
}
