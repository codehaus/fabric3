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
package org.fabric3.spi.services.contribution;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Constructs a classpath based on the contents of an archive. Implementations introspect archives and place any
 * required artifacts on the classpath definition. For example, a jar processor may place libraries found in
 * /META-INF/lib on the classpath.
 *
 * @version $Rev$ $Date$
 */
public interface ClasspathProcessor {

    /**
     * Returns true if the processor can introspect the given archive
     *
     * @param url the location of the archive
     * @return true if the processor can introspect the archive
     */
    public boolean canProcess(URL url);

    /**
     * Constructs the classpath by introspecting the archive
     *
     * @param url the location of the archive
     * @return the classpath
     * @throws IOException if an error occurs during introspection
     */
    public List<URL> process(URL url) throws IOException;

}
