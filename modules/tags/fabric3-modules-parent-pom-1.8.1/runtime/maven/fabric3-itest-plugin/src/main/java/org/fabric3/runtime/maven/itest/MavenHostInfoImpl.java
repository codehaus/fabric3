/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.runtime.maven.itest;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.fabric3.host.RuntimeMode;
import org.fabric3.runtime.maven.MavenHostInfo;

/**
 * @version $Rev$ $Date$
 */
public class MavenHostInfoImpl implements MavenHostInfo {
    private final URI domain;
    private final Set<URL> dependencyUrls;
    private final File tempDir;
    private File baseDir;

    public MavenHostInfoImpl(URI domain, Set<URL> dependencyUrls, File baseDir, File tempDir) {
        this.domain = domain;
        this.dependencyUrls = dependencyUrls;
        this.baseDir = baseDir;
        this.tempDir = tempDir;
    }

    public String getRuntimeName() {
        return "maven";
    }

    public File getBaseDir() {
        return baseDir;
    }

    public File getTempDir() {
        return tempDir;
    }

    public File getDataDir() {
        // use the temp directory as the data dir
        return tempDir;
    }

    public File getConfigDirectory() {
        return null;
    }

    public File getUserRepositoryDirectory() {
        return null;
    }

    public File getRuntimeRepositoryDirectory() {
        return null;
    }

    public File getExtensionsRepositoryDirectory() {
        return null;
    }

    public List<File> getDeployDirectories() {
        return null;
    }

    public boolean supportsClassLoaderIsolation() {
        return true;
    }

    public RuntimeMode getRuntimeMode() {
        return RuntimeMode.VM;
    }

    public URI getDomain() {
        return domain;
    }

    public Set<URL> getDependencyUrls() {
        return dependencyUrls;
    }
}
