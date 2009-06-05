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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.xml.XmlResourceElementLoader;
import org.fabric3.spi.contribution.xml.XmlResourceElementLoaderRegistry;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Default impelmentation of an XmlIndexerRegistry.
 *
 * @version $Rev$ $Date$
 */
public class XmlResourceElementLoaderRegistryImpl implements XmlResourceElementLoaderRegistry {
    private Map<QName, XmlResourceElementLoader> cache = new HashMap<QName, XmlResourceElementLoader>();

    public void register(XmlResourceElementLoader loader) {
        cache.put(loader.getType(), loader);
    }

    public void unregister(QName name) {
        cache.remove(name);
    }

    @SuppressWarnings({"unchecked"})
    public void load(XMLStreamReader reader, URI contributionUri, Resource resource, IntrospectionContext context,  ClassLoader loader)
            throws InstallException, XMLStreamException {
        try {
            QName name = reader.getName();
            XmlResourceElementLoader elementLoader = cache.get(name);
            if (elementLoader == null) {
                return;
            }
            elementLoader.load(reader, contributionUri, resource, context, loader);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }

    }
}