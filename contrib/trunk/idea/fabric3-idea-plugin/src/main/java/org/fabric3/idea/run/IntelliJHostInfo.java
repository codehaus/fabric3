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

import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.maven.runtime.MavenHostInfo;

/**
 * Host information supplied by IntelliJ
 *
 * @version $Rev$ $Date$
 */
public interface IntelliJHostInfo extends MavenHostInfo {

    /**
     * Returns URL to the current module output directory.
     *
     * @return a URL to the current module output directory.
     */
    URL getOutputDirectory();

    /**
     * Returns URL to the current module test output directory.
     *
     * @return a URL to the current module test output directory
     */
    URL getTestOutputDirectory();

    /**
     * Returns a list of JUnit component implementations to execute.
     *
     * @return a list of JUnit component implementations to execute
     */
    List<String> getJUnitComponentImplementations();

    /**
     * Returns a list of composite QNames to included in the synthetic test composite.
     *
     * @return a list of composite QNames to included in the synthetic test composite
     */
    List<QName> getIncludedComposites();
}
