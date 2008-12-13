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
package org.fabric3.contribution.manifest;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.TypeLoader;

/**
 * Processes a <code>import.java</code> element in a contribution manifest
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JavaImportLoader implements TypeLoader<JavaImport> {

    public JavaImport load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        String statement = reader.getAttributeValue(null, "package");
        if (statement == null) {
            MissingPackage failure = new MissingPackage("No package name specified", reader);
            context.addError(failure);
            return null;
        }

        String requiredAttr = reader.getAttributeValue(null, "required");
        boolean required = requiredAttr == null || Boolean.parseBoolean(requiredAttr);

        PackageInfo info;
        String version = reader.getAttributeValue(null, "version");
        String minVersion = reader.getAttributeValue(null, "min");
        if (version != null) {
            info = parseVersion(statement, version, required, reader, context);
        } else if (minVersion != null) {
            info = parseRange(statement, minVersion, required, reader, context);
        } else {
            info = new PackageInfo(statement, required);
        }
        if (info == null) {
            // validation error
            return null;
        }
        return new JavaImport(info);
    }

    private PackageInfo parseVersion(String statement,
                                     String version,
                                     boolean required,
                                     XMLStreamReader reader,
                                     IntrospectionContext context) {
        try {
            String minInclusiveAttr = reader.getAttributeValue(null, "minInclusive");
            boolean minInclusive = minInclusiveAttr == null || Boolean.parseBoolean(minInclusiveAttr);
            PackageVersion packageVersion = new PackageVersion(version);
            return new PackageInfo(statement, packageVersion, minInclusive, required);
        } catch (IllegalArgumentException e) {
            InvalidValue failure = new InvalidValue("Invalid import package version", version, reader, e);
            context.addError(failure);
            return null;
        }
    }

    private PackageInfo parseRange(String statement,
                                   String minVersion,
                                   boolean required,
                                   XMLStreamReader reader,
                                   IntrospectionContext context) {
        String minInclusiveAttr = reader.getAttributeValue(null, "minInclusive");
        boolean minInclusive = minInclusiveAttr == null || Boolean.parseBoolean(minInclusiveAttr);
        String maxVersion = reader.getAttributeValue(null, "max");
        PackageVersion minimum;
        PackageVersion maximum = null;
        try {
            minimum = new PackageVersion(minVersion);
        } catch (IllegalArgumentException e) {
            InvalidValue failure = new InvalidValue("Invalid minimum package version", minVersion, reader, e);
            context.addError(failure);
            return null;
        }
        if (maxVersion != null) {
            try {
                maximum = new PackageVersion(maxVersion);
            } catch (IllegalArgumentException e) {
                InvalidValue failure = new InvalidValue("Invalid maximum package version", maxVersion, reader, e);
                context.addError(failure);
                return null;
            }
        }
        String maxInclusiveAttr = reader.getAttributeValue(null, "maxInclusive");
        boolean maxInclusive = maxInclusiveAttr == null || Boolean.parseBoolean(maxInclusiveAttr);
        return new PackageInfo(statement, minimum, minInclusive, maximum, maxInclusive, required);
    }
}
