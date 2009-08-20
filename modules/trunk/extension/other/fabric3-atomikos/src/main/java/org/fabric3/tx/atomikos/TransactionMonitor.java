package org.fabric3.tx.atomikos;

import org.fabric3.api.annotation.logging.Fine;
import org.fabric3.api.annotation.logging.Info;

/**
 * @version $Rev$ $Date$
 */
public interface TransactionMonitor {

    @Info
    public void extensionStarted();

    @Fine
    public void recoveryCompleted();
}
