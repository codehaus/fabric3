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
package org.fabric3.fabric.monitor;

import java.io.PrintWriter;

import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.monitor.FormatterHelper;
import org.fabric3.host.Fabric3Exception;
import org.fabric3.host.Fabric3RuntimeException;
import org.fabric3.host.monitor.ExceptionFormatter;
import org.fabric3.host.monitor.FormatterRegistry;

/**
 * Performs basics formatting of exceptions for JDK logging
 *
 * @version $Rev$ $Date$
 */
public class DefaultExceptionFormatter implements ExceptionFormatter<Throwable> {
    private FormatterRegistry registry;

    public DefaultExceptionFormatter(@Reference FormatterRegistry registry) {
        this.registry = registry;
    }

    public boolean canFormat(Class<?> type) {
        return Throwable.class.isAssignableFrom(type);
    }

    public void write(PrintWriter writer, Throwable exception) {
        writer.append(exception.getClass().getName()).append(": ");
        if (exception instanceof Fabric3Exception) {
            Fabric3Exception e = (Fabric3Exception) exception;
            e.appendBaseMessage(writer);
        } else if (exception instanceof Fabric3RuntimeException) {
            Fabric3RuntimeException e = (Fabric3RuntimeException) exception;
            e.appendBaseMessage(writer);
        } else if (exception.getMessage() != null) {
            writer.write(exception.getMessage());
        }
        writer.append("\n");
        Throwable cause = exception.getCause();
        if (cause != null) {
            FormatterHelper.writeStackTrace(writer, exception, cause);
            writer.println("Caused by:");
            registry.formatException(writer, cause);
        } else {
            StackTraceElement[] trace = exception.getStackTrace();
            for (StackTraceElement aTrace : trace) {
                writer.println("\tat " + aTrace);
            }
        }
    }

}
