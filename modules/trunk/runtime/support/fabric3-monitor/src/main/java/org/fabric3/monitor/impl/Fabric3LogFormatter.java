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
package org.fabric3.monitor.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * @version $Revision$ $Date$
 */

public class Fabric3LogFormatter extends Formatter {

    private static final DateFormat FORMAT = new SimpleDateFormat("yyyy.MM.dd|h:mm:ss");
    private static final String SEPARATOR = System.getProperty("line.separator");

    public String format(LogRecord record) {
        String message = formatMessage(record);
        StringBuilder output = new StringBuilder()
                .append("[")
                .append(record.getLevel().getLocalizedName()).append("|")
                .append(Thread.currentThread().getName()).append("|")
                .append(FORMAT.format(new Date(record.getMillis())))
                .append("] ")
                .append(message)
                .append(SEPARATOR);
        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                output.append(sw.toString());
            } catch (Exception ex) {
                // ignore
            }
        }

        return output.toString();
    }

}