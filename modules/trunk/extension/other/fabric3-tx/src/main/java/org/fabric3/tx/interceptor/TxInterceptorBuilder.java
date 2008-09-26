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
package org.fabric3.tx.interceptor;

import javax.transaction.TransactionManager;

import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;

/**
 * @version $Revision$ $Date$
 */
public class TxInterceptorBuilder implements InterceptorBuilder<TxInterceptorDefinition, TxInterceptor> {

    private TransactionManager transactionManager;
    private TxMonitor monitor;

    public TxInterceptorBuilder(@Reference TransactionManager transactionManager, @Monitor TxMonitor monitor) {
        this.transactionManager = transactionManager;
        this.monitor = monitor;
    }

    public TxInterceptor build(TxInterceptorDefinition interceptorDefinition) throws BuilderException {
        return new TxInterceptor(transactionManager, interceptorDefinition.getAction(), monitor);
    }


}
