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
 */
package org.fabric3.contribution;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.contribution.archive.ClasspathProcessor;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class ClasspathProcessorRegistryImpl implements ClasspathProcessorRegistry {
    private List<ClasspathProcessor> processors = new ArrayList<ClasspathProcessor>();
    // cache of previously processed artifact URLs
    private Map<URL, List<URL>> cache = new HashMap<URL, List<URL>>();

    public void register(ClasspathProcessor processor) {
        processors.add(processor);
    }

    public void unregister(ClasspathProcessor processor) {
        processors.remove(processor);
    }

    public List<URL> process(URL url) throws IOException {
        List<URL> cached = cache.get(url);
        if (cached != null) {
            // artifact has already been processed, reuse it
            return cached;
        }
        for (ClasspathProcessor processor : processors) {
            if (processor.canProcess(url)) {
                List<URL> urls = processor.process(url);
                cache.put(url, urls);
                return urls;

            }
        }
        // artifact does not need to be expanded, just return its base url
        List<URL> urls = new ArrayList<URL>();
        urls.add(url);
        cache.put(url, urls);
        return urls;
    }
}
