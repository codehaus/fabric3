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
package org.fabric3.idea.run;

import java.net.URI;
import java.util.Collection;

import org.apache.maven.surefire.report.PojoStackTraceWriter;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.ReporterManager;
import org.apache.maven.surefire.report.StackTraceWriter;
import org.apache.maven.surefire.testset.SurefireTestSet;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.fabric3.maven.runtime.MavenEmbeddedRuntime;
import org.fabric3.scdl.Operation;

/**
 * @version $Rev$ $Date$
 */
public class SCATestSet implements SurefireTestSet {
    private final MavenEmbeddedRuntime runtime;
    private final String name;
    private final URI contextId;
    private final Collection<? extends Operation<?>> operations;

    public SCATestSet(MavenEmbeddedRuntime runtime,
                      String name,
                      URI contextId,
                      Collection<? extends Operation<?>> operations) {
        this.runtime = runtime;
        this.name = name;
        this.contextId = contextId;
        this.operations = operations;
    }

    public void execute(ReporterManager reporterManager, ClassLoader classLoader) throws TestSetFailedException {
        for (Operation<?> operation : operations) {
            String operationName = operation.getName();
            reporterManager.testStarting(new ReportEntry(this, operationName, name));
            try {
                runtime.executeTest(contextId, name, operation);
                reporterManager.testSucceeded(new ReportEntry(this, operationName, name));
            } catch (TestSetFailedException e) {
                StackTraceWriter stw = new PojoStackTraceWriter(name, operationName, e.getCause());
                reporterManager.testFailed(new ReportEntry(this, operationName, name, stw));
                throw e;
            }
        }
    }

    public int getTestCount() {
        return operations.size();
    }

    public String getName() {
        return name;
    }

    public Class getTestClass() {
        throw new UnsupportedOperationException();
    }
}
