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
package org.fabric3.fabric.builder;

import java.io.PrintWriter;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.services.formatter.ExceptionFormatter;
import org.fabric3.spi.services.formatter.FormatterRegistry;
import org.fabric3.fabric.services.formatter.FormatterHelper;
import org.fabric3.spi.builder.WiringException;

/**
 * Formats {@link WiringException}s
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class WiringExceptionFormatter implements ExceptionFormatter<WiringException> {
    private FormatterRegistry registry;

    public WiringExceptionFormatter(@Reference FormatterRegistry registry) {
        this.registry = registry;
    }

    public Class<WiringException> getType() {
        return WiringException.class;
    }

    @Init
    public void init() {
        registry.register(WiringException.class, this);
    }

    @Destroy
    public void destroy() {
        registry.unregister(WiringException.class);
    }

    public void write(PrintWriter writer, WiringException e) {
        e.appendBaseMessage(writer);
        if (e.getSourceUri() != null) {
            writer.write("\nSource : " + e.getSourceUri());
        }
        if (e.getTargetUri() != null) {
            writer.write("\nTarget : " + e.getTargetUri());
        }
        writer.append("\n");
        Throwable cause = e.getCause();
        if (cause != null) {
            FormatterHelper.writeStackTrace(writer, e, cause);
            writer.println("Caused by:");
            ExceptionFormatter<? super Throwable> formatter = getFormatter(cause.getClass());
            formatter.write(writer, cause);
        } else {
            StackTraceElement[] trace = e.getStackTrace();
            for (StackTraceElement aTrace : trace) {
                writer.println("\tat " + aTrace);
            }
        }
    }

    private <T extends Throwable> ExceptionFormatter<? super T> getFormatter(Class<? extends T> type) {
        return registry.getFormatter(type);
    }
}
