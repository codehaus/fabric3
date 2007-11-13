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
package org.fabric3.fabric.loader;

import java.io.PrintWriter;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.monitor.FormatterHelper;
import org.fabric3.host.monitor.ExceptionFormatter;
import org.fabric3.host.monitor.FormatterRegistry;
import org.fabric3.spi.loader.LoaderException;

/**
 * Formats {@link org.fabric3.spi.loader.LoaderException} events
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class LoaderExceptionFormatter<T extends LoaderException> implements ExceptionFormatter<T> {
    private FormatterRegistry registry;

    public LoaderExceptionFormatter(@Reference FormatterRegistry factory) {
        this.registry = factory;
        factory.register(this);
    }

    public boolean canFormat(Class<?> type) {
        return LoaderException.class.equals(type);
    }

    @Destroy
    public void destroy() {
        registry.unregister(this);
    }

    public void write(PrintWriter writer, T e) {
        e.appendBaseMessage(writer);
        if (e.getResourceURI() != null) {
            writer.write("\nSCDL: " + e.getResourceURI());
        }
        if (e.getLine() != LoaderException.UNDEFINED) {
            writer.write("\nLine: " + e.getLine() + "\n");
            writer.write("Column: " + e.getColumn() + "\n");
        } else {
            writer.write("\n");
        }
        writer.append("\n");
        Throwable cause = e.getCause();
        if (cause != null) {
            FormatterHelper.writeStackTrace(writer, e, cause);
            writer.println("Caused by:");
            registry.formatException(writer, cause);
        } else {
            StackTraceElement[] trace = e.getStackTrace();
            for (StackTraceElement aTrace : trace) {
                writer.println("\tat " + aTrace);
            }
        }
    }
}
