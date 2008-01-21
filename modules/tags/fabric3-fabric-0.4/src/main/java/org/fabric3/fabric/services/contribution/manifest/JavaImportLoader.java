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
package org.fabric3.fabric.services.contribution.manifest;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.Constants;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.fabric.services.contribution.MissingPackageException;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;

/**
 * Processes a <code>import.java</code> element in a contribution manifest
 *
 * @version $Rev$ $Date$
 */
public class JavaImportLoader extends LoaderExtension<JavaImport> {
    private static final QName IMPORT = new QName(Constants.SCA_NS, "import.java");

    /**
     * Constructor specifies the registry to register with.
     *
     * @param registry the LoaderRegistry this loader should register with
     */
    public JavaImportLoader(@Reference LoaderRegistry registry) {
        super(registry);
    }

    public QName getXMLType() {
        return IMPORT;
    }

    public JavaImport load(XMLStreamReader reader, LoaderContext context)
            throws LoaderException, XMLStreamException {
        String packageName = reader.getAttributeValue(null, "package");
        if (packageName == null) {
            throw new MissingPackageException("No package name specified");
        }
        return new JavaImport(packageName);
    }
}
