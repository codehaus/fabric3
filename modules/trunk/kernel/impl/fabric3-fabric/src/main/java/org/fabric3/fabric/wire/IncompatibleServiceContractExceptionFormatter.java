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
package org.fabric3.fabric.wire;

import java.io.PrintWriter;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.monitor.ExceptionFormatter;
import org.fabric3.host.monitor.FormatterRegistry;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.extension.monitor.FormatterHelper;

/**
 * Formats {@link IncompatibleServiceContractException} for JDK logging
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class IncompatibleServiceContractExceptionFormatter implements ExceptionFormatter<IncompatibleServiceContractException> {
    private FormatterRegistry registry;

    public IncompatibleServiceContractExceptionFormatter(@Reference FormatterRegistry factory) {
        this.registry = factory;
        factory.register(this);
    }

    public boolean canFormat(Class<?> type) {
        return IncompatibleServiceContractException.class.isAssignableFrom(type);
    }

    @Destroy
    public void destroy() {
        registry.unregister(this);
    }

    public void write(PrintWriter writer, IncompatibleServiceContractException e) {
        e.appendBaseMessage(writer);
        ServiceContract<?> source = e.getSource();
        String sourceContractName = null;
        if (source != null) {
            sourceContractName = source.getInterfaceName();
        }
        Operation<?> sourceOperation = e.getSourceOperation();
        String sourceOpName = null;
        if (sourceOperation != null) {
            sourceOpName = sourceOperation.getName();
        }
        if (sourceOpName == null) {
            writer.write("\nSource Contract: " + sourceContractName);
        } else {
            writer.write("\nSource Contract: " + sourceContractName + "/" + sourceOpName);
        }
        ServiceContract<?> target = e.getTarget();
        String targetContractName = null;
        if (target != null) {
            targetContractName = target.getInterfaceName();
        }
        Operation<?> targetOperation = e.getTargetOperation();
        String targetOpName = null;
        if (targetOperation != null) {
            targetOpName = targetOperation.getName();
        }
        if (targetOpName == null) {
            writer.write("\nTarget Contract: " + targetContractName + "\n");
        } else {
            writer.write("\nTarget Contract: " + targetContractName + "/" + targetOpName + "\n");

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
