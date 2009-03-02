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

import org.fabric3.api.annotation.logging.Fine;
import org.fabric3.api.annotation.logging.Warning;

/**
 *
 * @version $Revision$ $Date$
 */
public interface TxMonitor {
    
    @Fine
    void started(int hashCode);
    
    @Fine
    void committed(int hashCode);
    
    @Fine
    void suspended(int hashCode);
    
    @Warning
    void rolledback(int hashCode);
    
    @Fine
    void resumed(int hashCode);
    
    @Fine
    void markedForRollback(int hashCode);
    
    @Fine
    void interceptorInitialized(TxAction txAction);
    
    @Fine
    void joined(int hashCode);

}
