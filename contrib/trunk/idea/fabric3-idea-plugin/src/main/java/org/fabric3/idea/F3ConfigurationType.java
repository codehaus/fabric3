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

import javax.swing.*;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * @version $Rev$ $Date$
 */
public class F3ConfigurationType implements ConfigurationType {
    private ConfigurationFactory factory;
    private Logger logger;

    public F3ConfigurationType() {
        factory = new F3ConfigurationFactory(this);
        logger = Logger.getInstance("fabric3");
    }

    public void initComponent() {
        logger.info("Fabric3 plugin initialized");
    }

    public void disposeComponent() {
        logger.info("Fabric3 plugin shutdown");
    }

    @NotNull
    public String getComponentName() {
        return "F3ConfigurationType";
    }

    public String getDisplayName() {
        return "Fabric3 JUnit Component";
    }

    public String getConfigurationTypeDescription() {
        return "Fabric3 JUnit Component";
    }

    public Icon getIcon() {
        return null;
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{factory};
    }

}