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
import javax.xml.namespace.QName;

import com.intellij.execution.CantRunException;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.ProjectJdk;
import com.intellij.openapi.roots.ModuleRootManager;

/**
 * @version $Rev$ $Date$
 */
public class F3JUnitCommandLineState extends JavaCommandLineState {
    private URI domainUri;
    private Module module;
    private String junitClass;
    private QName composite;

    public F3JUnitCommandLineState(Module module,
                                   String junitClass,
                                   QName composite,
                                   RunnerSettings runnerSettings,
                                   ConfigurationPerRunnerSettings perRunnerSettings) {
        super(runnerSettings, perRunnerSettings);
        this.junitClass = junitClass;
        this.composite = composite;
        domainUri = URI.create("fabric3://./domain");
        this.module = module;
    }

    public ExecutionResult execute() throws ExecutionException {
        ConsoleView view = TextConsoleBuilderFactory.getInstance().createBuilder(module.getProject()).getConsole();
        ProcessHandler handler = new F3ProcessHandler(domainUri, module, junitClass, composite, view);
        return new DefaultExecutionResult(view, handler);
    }

    protected JavaParameters createJavaParameters() throws ExecutionException {
        ModuleRootManager manager = ModuleRootManager.getInstance(module);
        ProjectJdk jdk = manager.getJdk();
        if (jdk == null) {
            throw CantRunException.noJdkForModule(module);
        }
        JavaParameters params = new JavaParameters();
        params.setJdk(jdk);
        return params;
    }
}