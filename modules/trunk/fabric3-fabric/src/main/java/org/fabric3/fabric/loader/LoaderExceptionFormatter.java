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

import org.fabric3.spi.loader.LoaderException;

import org.fabric3.host.monitor.ExceptionFormatter;
import org.fabric3.host.monitor.FormatterRegistry;

/**
 * Formats {@link org.fabric3.spi.loader.LoaderException} events
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class LoaderExceptionFormatter<T extends LoaderException> implements ExceptionFormatter<T> {
    private FormatterRegistry factory;

    public LoaderExceptionFormatter(@Reference FormatterRegistry factory) {
        this.factory = factory;
        factory.register(this);
    }

    public boolean canFormat(Class<?> type) {
        return LoaderException.class.isAssignableFrom(type);
    }

    @Destroy
    public void destroy() {
        factory.unregister(this);
    }

    public PrintWriter write(PrintWriter writer, T e) {
        e.appendBaseMessage(writer);
        if (e.getLine() != LoaderException.UNDEFINED) {
            writer.write("\nLine: " + e.getLine() + "\n");
            writer.write("Column: " + e.getColumn() + "\n");
        } else {
            writer.write("\n");
        }
        return writer;
    }
}
