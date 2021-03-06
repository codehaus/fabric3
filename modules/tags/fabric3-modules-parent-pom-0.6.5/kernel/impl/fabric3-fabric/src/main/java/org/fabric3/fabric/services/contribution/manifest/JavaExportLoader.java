/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.fabric.services.contribution.manifest;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.fabric.services.contribution.MissingPackage;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.TypeLoader;

/**
 * Loads an <code>export.java</code> entry in a contribution manifest.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JavaExportLoader implements TypeLoader<JavaExport> {
    //private static final QName EXPORT = new QName(SCA_NS, "export.java");


    public JavaExport load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        String packageName = reader.getAttributeValue(null, "package");
        if (packageName == null) {
            MissingPackage failure = new MissingPackage("No package name specified", reader);
            context.addError(failure);
            return null;
        }
        return new JavaExport(packageName);
    }
}
