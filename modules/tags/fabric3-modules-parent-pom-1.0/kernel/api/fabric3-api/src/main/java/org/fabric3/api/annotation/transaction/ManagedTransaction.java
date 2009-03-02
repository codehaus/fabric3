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
package org.fabric3.api.annotation.transaction;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

import static org.oasisopen.sca.Constants.SCA_PREFIX;
import org.oasisopen.sca.annotation.Intent;
import org.oasisopen.sca.annotation.Qualifier;

/**
 * Used to specify the managed transaction intent on a component implementation.
 *
 * @version $Revision$ $Date$
 */
@Target({TYPE})
@Retention(RUNTIME)
@Inherited
@Intent(ManagedTransaction.MANAGED_TRANSACTION)
public @interface ManagedTransaction {

    String MANAGED_TRANSACTION = SCA_PREFIX + "managedTransaction";
    String MANAGED_TRANSACTION_GLOBAL = MANAGED_TRANSACTION + ".global";
    String MANAGED_TRANSACTION_LOCAL = MANAGED_TRANSACTION + ".local";

    /**
     * Returns the list of transaction qualifiers or an empty string.
     *
     * @return the list of transaction qualifiers or an empty string
     */
    @Qualifier
    public abstract String value() default "";
}
