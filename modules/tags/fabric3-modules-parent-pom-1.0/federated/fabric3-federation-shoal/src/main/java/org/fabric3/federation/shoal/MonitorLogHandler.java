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
package org.fabric3.federation.shoal;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Diverts Shoal logging to the Fabric3 monitor framework.
 *
 * @version $Revision$ $Date$
 */
public class MonitorLogHandler extends Handler {
    private FederationServiceMonitor monitor;

    public MonitorLogHandler(FederationServiceMonitor monitor) {
        this.monitor = monitor;
    }

    public void publish(LogRecord record) {
        Level level = record.getLevel();
        if (Level.CONFIG.equals(level)) {
            monitor.onConfig(record.getMessage());
        } else if (Level.FINE.equals(level)) {
            monitor.onFine(record.getMessage());
        } else if (Level.FINER.equals(level)) {
            monitor.onFiner(record.getMessage());
        } else if (Level.FINEST.equals(level)) {
            monitor.onFinest(record.getMessage());
        } else if (Level.INFO.equals(level)) {
            monitor.onInfo(record.getMessage());
        } else if (Level.SEVERE.equals(level)) {
            monitor.onSevere(record.getMessage());
        } else if (Level.WARNING.equals(level)) {
            monitor.onWarning(record.getMessage());
        }
    }

    public void flush() {

    }

    public void close() throws SecurityException {

    }
}
