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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.fabric.services.contribution.MissingPackageException;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.TypeLoader;

/**
 * Loads an <code>export.java</code> entry in a contribution manifest.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JavaExportLoader implements TypeLoader<JavaExport> {
    //private static final QName EXPORT = new QName(SCA_NS, "export.java");


    public JavaExport load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException, LoaderException {
        String packageName = reader.getAttributeValue(null, "package");
        if (packageName == null) {
            throw new MissingPackageException("No package name specified", reader);
        }
        return new JavaExport(packageName);
    }
}
