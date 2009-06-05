/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.host.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.host.contribution.ContributionSource;

/**
 * Encapsulates configuration needed to boostrap a runtime.
 *
 * @version $Revision$ $Date$
 */
public class BootConfiguration {
    private Fabric3Runtime<?> runtime;
    private Bootstrapper bootstrapper;
    private ClassLoader bootClassLoader;
    private List<ContributionSource> extensionContributions = Collections.emptyList();
    private List<ContributionSource> userContributions = Collections.emptyList();
    private Map<String, String> exportedPackages = new HashMap<String, String>();

    public Fabric3Runtime<?> getRuntime() {
        return runtime;
    }

    public void setRuntime(Fabric3Runtime<?> runtime) {
        this.runtime = runtime;
    }

    public Bootstrapper getBootstrapper() {
        return bootstrapper;
    }

    public void setBootstrapper(Bootstrapper bootstrapper) {
        this.bootstrapper = bootstrapper;
    }

    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }

    public void setBootClassLoader(ClassLoader bootClassLoader) {
        this.bootClassLoader = bootClassLoader;
    }

    public Map<String, String> getExportedPackages() {
        return exportedPackages;
    }

    public void setExportedPackages(Map<String, String> exportedPackages) {
        this.exportedPackages = exportedPackages;
    }

    public List<ContributionSource> getExtensionContributions() {
        return extensionContributions;
    }

    public void setExtensionContributions(List<ContributionSource> extensionContributions) {
        this.extensionContributions = extensionContributions;
    }

    public List<ContributionSource> getUserContributions() {
        return userContributions;
    }

    public void setUserContributions(List<ContributionSource> userContributions) {
        this.userContributions = userContributions;
    }

}
