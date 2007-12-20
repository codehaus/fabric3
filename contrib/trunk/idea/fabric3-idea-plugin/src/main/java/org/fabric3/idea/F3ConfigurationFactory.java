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
package org.fabric3.idea;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;

/**
 * Instantiates a test configuration model for JUnit components.
 *
 * @version $Rev$ $Date$
 */
class F3ConfigurationFactory extends ConfigurationFactory {

    public F3ConfigurationFactory(F3ConfigurationType type) {
        super(type);
    }

    public RunConfiguration createTemplateConfiguration(Project project) {
        return new F3JUnitRunConfiguration(project, this, "");
    }

    public RunConfiguration createConfiguration(String name, RunConfiguration template) {
        F3JUnitRunConfiguration config = (F3JUnitRunConfiguration) template;
        if (config.getModule() == null) {
            final Module[] modules = config.getModules();
            if (modules != null && modules.length > 0) {
                config.setModule(modules[0]);
            }
        }
        return super.createConfiguration(name, config);
    }
}