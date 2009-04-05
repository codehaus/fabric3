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
package org.fabric3.policy.tx;

import javax.transaction.Status;
import javax.transaction.TransactionManager;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.Resource;
import org.fabric3.api.annotation.transaction.ManagedTransaction;
import org.fabric3.api.annotation.transaction.PropagatesTransaction;
import org.fabric3.api.annotation.transaction.SuspendsTransaction;

/**
 * @version $Revision$ $Date$
 */
@ManagedTransaction
public class AnnotatedManagedTransactionService implements TransactionalService {
    @Resource(mappedName = "TransactionManager")
    protected TransactionManager tm;

    @Reference
    @SuspendsTransaction
    protected TransactionalService suspendedTransactionService;

    @Reference
    @PropagatesTransaction
    protected TransactionalService propagatesTransactionService;

    public void call() throws Exception {
        if (tm.getTransaction() == null || Status.STATUS_ACTIVE != tm.getTransaction().getStatus()) {
            throw new AssertionError("Transaction not active");
        }
        suspendedTransactionService.call();
        propagatesTransactionService.call();
    }
}
