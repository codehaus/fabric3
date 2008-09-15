/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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