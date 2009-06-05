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
package org.fabric3.contribution.processor;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.xml.XmlIndexer;
import org.fabric3.spi.contribution.xml.XmlIndexerRegistry;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Default impelmentation of an XmlIndexerRegistry.
 *
 * @version $Rev$ $Date$
 */
public class XmlIndexerRegistryImpl implements XmlIndexerRegistry {
    private Map<QName, XmlIndexer> cache = new HashMap<QName, XmlIndexer>();

    public void register(XmlIndexer indexer) {
        cache.put(indexer.getType(), indexer);
    }

    public void unregister(QName name) {
        cache.remove(name);
    }

    public void index(Resource resource, XMLStreamReader reader, IntrospectionContext context) throws InstallException {
        QName name = reader.getName();
        XmlIndexer indexer = cache.get(name);
        if (indexer == null) {
            return;
        }
        indexer.index(resource, reader, context);
    }
}
