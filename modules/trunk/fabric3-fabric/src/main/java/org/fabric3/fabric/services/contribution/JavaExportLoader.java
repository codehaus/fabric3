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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;

/**
 * @version $Rev$ $Date$
 */
public class JavaExportLoader extends LoaderExtension<Object, JavaExport> {
    private static final QName EXPORT = new QName(SCA_NS, "export.java");

    public JavaExportLoader(@Reference LoaderRegistry registry) {
        super(registry);
    }

    public QName getXMLType() {
        return EXPORT;
    }

    public JavaExport load(Object configuration, XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {
        String packageName = reader.getAttributeValue(null, "package");
        if (packageName == null) {
            throw new MissingPackageException("No package name specified");
        }
        return new JavaExport(packageName);
    }
}
