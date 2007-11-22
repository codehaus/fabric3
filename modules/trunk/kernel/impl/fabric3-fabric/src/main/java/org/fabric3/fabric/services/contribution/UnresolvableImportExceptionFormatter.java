/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.fabric.services.contribution;

import java.io.PrintWriter;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.monitor.FormatterHelper;
import org.fabric3.host.monitor.ExceptionFormatter;
import org.fabric3.host.monitor.FormatterRegistry;
import org.fabric3.spi.services.contribution.Import;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class UnresolvableImportExceptionFormatter implements ExceptionFormatter<UnresolvableImportException> {
    private FormatterRegistry registry;

    public UnresolvableImportExceptionFormatter(@Reference FormatterRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public boolean canFormat(Class<?> type) {
        return UnresolvableImportException.class.equals(type);
    }

    public void write(PrintWriter writer, UnresolvableImportException e) {
        writer.append(e.getMessage());
        Import imprt = e.getImport();
        if (imprt != null) {
            writer.append("\nImport: ").append(imprt.toString());
        }
        if (e.getIdentifier() != null) {
            writer.append("\nContribution: ").append(e.getIdentifier());
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
