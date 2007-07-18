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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.spi.services.contribution.ClasspathProcessor;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;

/**
 * @version $Rev$ $Date$
 */
public class ClasspathProcessorRegistryImpl implements ClasspathProcessorRegistry {
    private List<ClasspathProcessor> processors = new ArrayList<ClasspathProcessor>();

    public void register(ClasspathProcessor processor) {
        processors.add(processor);
    }

    public void unregister(ClasspathProcessor processor) {
        processors.remove(processor);
    }

    public List<URL> process(File file) throws IOException {
        for (ClasspathProcessor processor : processors) {
            if (processor.canProcess(file)) {
                return processor.process(file);
            }
        }
        // artifact does not need to be expanded, just return its base url
        List<URL> classpath = new ArrayList<URL>();
        classpath.add(file.toURI().toURL());
        return classpath;
    }
}
