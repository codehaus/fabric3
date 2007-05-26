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
package org.fabric3.fabric.component;

import java.io.PrintWriter;
import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.host.Fabric3Exception;
import org.fabric3.host.Fabric3RuntimeException;
import org.fabric3.host.monitor.ExceptionFormatter;
import org.fabric3.host.monitor.FormatterRegistry;
import org.fabric3.spi.component.GroupInitializationException;

/**
 * Performs basics formatting of exceptions for JDK logging
 *
 * @version $Rev$ $Date$
 */
public class GroupInitializationExceptionFormatter implements ExceptionFormatter<GroupInitializationException> {
    private FormatterRegistry registry;

    public GroupInitializationExceptionFormatter(@Reference FormatterRegistry registry) {
        this.registry = registry;
    }

    public boolean canFormat(Class<?> type) {
        return GroupInitializationException.class.isAssignableFrom(type);
    }

    public void write(PrintWriter writer, GroupInitializationException exception) {
        writer.append(exception.getClass().getName()).append(": ");
        exception.appendBaseMessage(writer);
        writer.print("\n");
        StackTraceElement[] trace = exception.getStackTrace();
        for (StackTraceElement aTrace : trace) {
            writer.println("\tat " + aTrace);
        }
        List<Exception> exceptions = exception.getCauses();
        for (Exception cause : exceptions) {
            writer.println("-----------------------------------------------------------------------------------------");
            if (cause instanceof Fabric3Exception) {
                Fabric3Exception f3ex = (Fabric3Exception) cause;
                writer.println(f3ex.getIdentifier() + " caused:");
            } else if (cause instanceof Fabric3RuntimeException) {
                Fabric3RuntimeException f3ex = (Fabric3RuntimeException) cause;
                writer.println(f3ex.getIdentifier() + " caused:"+f3ex.getMessage());
            } else {
                writer.println("Caused by:");
            }
            registry.formatException(writer, cause);
            writer.println("-----------------------------------------------------------------------------------------");
        }
    }

}
