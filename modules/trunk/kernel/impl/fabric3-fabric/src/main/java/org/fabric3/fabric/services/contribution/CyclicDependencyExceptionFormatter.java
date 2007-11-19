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
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.util.graph.Cycle;
import org.fabric3.fabric.util.graph.Vertex;
import org.fabric3.host.monitor.ExceptionFormatter;
import org.fabric3.host.monitor.FormatterRegistry;
import org.fabric3.spi.services.contribution.Contribution;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class CyclicDependencyExceptionFormatter implements ExceptionFormatter<CyclicDependencyException> {
    private FormatterRegistry registry;

    public CyclicDependencyExceptionFormatter(@Reference FormatterRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public boolean canFormat(Class<?> type) {
        return CyclicDependencyException.class.equals(type);
    }

    public void write(PrintWriter writer, CyclicDependencyException e) {
        writer.append(e.getMessage());
        for (Cycle<Contribution> cycle : e.getCycles()) {
            List<Vertex<Contribution>> originPath = cycle.getOriginPath();
            writer.append("\nCycle:");
            boolean first = true;
            for (Vertex<Contribution> vertex : originPath) {
                if (first) {
                    writer.append(vertex.getEntity().getUri().toString()).append(" ");
                } else {
                    writer.append("---->").append(vertex.getEntity().getUri().toString()).append(" ");
                }
                first = false;
            }
        }
        writer.append("\n");
        StackTraceElement[] trace = e.getStackTrace();
        for (StackTraceElement aTrace : trace) {
            writer.println("\tat " + aTrace);
        }
    }
}