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

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.fabric3.host.contribution.ContributionSource;

/**
 * Encapsulates configuration needed to boostrap a runtime.
 *
 * @version $Revision$ $Date$
 */
public class BootConfiguration<RUNTIME extends Fabric3Runtime<?>, BOOTSTRAPPER extends Bootstrapper> {
    private RUNTIME runtime;
    private BOOTSTRAPPER bootstrapper;
    private ClassLoader bootClassLoader;
    private List<URL> bootExports;
    private ContributionSource intents;
    private List<ContributionSource> extensions;
    private List<ContributionSource> policies = new LinkedList<ContributionSource>();

    public RUNTIME getRuntime() {
        return runtime;
    }

    public void setRuntime(RUNTIME runtime) {
        this.runtime = runtime;
    }

    public BOOTSTRAPPER getBootstrapper() {
        return bootstrapper;
    }

    public void setBootstrapper(BOOTSTRAPPER bootstrapper) {
        this.bootstrapper = bootstrapper;
    }

    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }

    public void setBootClassLoader(ClassLoader bootClassLoader) {
        this.bootClassLoader = bootClassLoader;
    }

    public List<URL> getBootLibraryExports() {
        return bootExports;
    }

    public void setBootLibraryExports(List<URL> bootExports) {
        this.bootExports = bootExports;
    }

    public ContributionSource getIntents() {
        return intents;
    }

    public void setIntents(ContributionSource intents) {
        this.intents = intents;
    }

    public List<ContributionSource> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<ContributionSource> extensions) {
        this.extensions = extensions;
    }

    public List<ContributionSource> getPolicies() {
        return policies;
    }
    
    public void setPolicies(List<ContributionSource> policies) {
        this.policies = policies;
    }

}
