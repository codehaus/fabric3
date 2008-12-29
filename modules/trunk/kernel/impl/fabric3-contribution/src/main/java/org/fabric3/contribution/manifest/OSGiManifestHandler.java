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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.manifest.JarManifestHandler;
import org.fabric3.spi.contribution.manifest.JavaExport;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageInfo;
import org.fabric3.spi.contribution.manifest.PackageVersion;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Parses OSGi manifest headers and adds the metadta to the SCA contribution manifest.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class OSGiManifestHandler implements JarManifestHandler {
    private static final String IMPORT_HEADER = "Import-Package";
    private static final String EXPORT_HEADER = "Export-Package";
    private static final String VERSION = "version=";
    private static final String RESOLUTION = "resolution:=";

    public void processManifest(ContributionManifest contributionManifest, Manifest jarManifest, IntrospectionContext context) {
        Attributes attributes = jarManifest.getMainAttributes();
        for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
            if (entry.getKey().toString().equalsIgnoreCase(IMPORT_HEADER)) {
                List<JavaImport> imports = parseImportHeader(entry.getValue().toString(), context);
                if (imports == null) {
                    // error
                    return;
                }
                for (JavaImport imprt : imports) {
                    contributionManifest.addImport(imprt);
                }
            } else if (entry.getKey().toString().equalsIgnoreCase(EXPORT_HEADER)) {
                List<JavaExport> exports = parseExportHeader(entry.getValue().toString(), context);
                if (exports == null) {
                    // error
                    return;
                }
                for (JavaExport export : exports) {
                    contributionManifest.addExport(export);
                }

            }
        }
    }

    private List<JavaImport> parseImportHeader(String header, IntrospectionContext context) {
        OSGiManifestEntryParser parser = new OSGiManifestEntryParser(header);
        List<JavaImport> imports = new ArrayList<JavaImport>();
        PackageInfo info = null;
        while (true) {
            OSGiManifestEntryParser.EventType type = parser.next();
            switch (type) {
            case BEGIN:
                break;
            case PATH:
                info = new PackageInfo(parser.getText());
                break;
            case PARAMETER:
                String text = parser.getText();
                if (text.startsWith(VERSION)) {
                    if (!parseVersion(text, info, context)) {
                        return null;
                    }
                } else if (text.startsWith(RESOLUTION)) {
                    String val = text.substring(RESOLUTION.length());
                    info.setRequired("required".equalsIgnoreCase(val));
                }

                break;
            case END_CLAUSE:
                if (info != null) {
                    JavaImport imprt = new JavaImport(info);
                    imports.add(imprt);
                }
                break;
            case END:
                return imports;
            }
        }
    }

    private List<JavaExport> parseExportHeader(String header, IntrospectionContext context) {
        OSGiManifestEntryParser parser = new OSGiManifestEntryParser(header);
        List<JavaExport> imports = new ArrayList<JavaExport>();
        PackageInfo info = null;
        while (true) {
            OSGiManifestEntryParser.EventType type = parser.next();
            switch (type) {
            case BEGIN:
                break;
            case PATH:
                info = new PackageInfo(parser.getText());
                break;
            case PARAMETER:
                String text = parser.getText();
                if (text.startsWith(VERSION)) {
                    if (!parseVersion(text, info, context)) {
                        return null;
                    }
                }
                break;
            case END_CLAUSE:
                JavaExport export = new JavaExport(info);
                imports.add(export);
                break;
            case END:
                return imports;
            }
        }
    }

    private boolean parseVersion(String text, PackageInfo info, IntrospectionContext context) {
        String val = text.substring(VERSION.length());
        if (val.startsWith("\"[")) {
            info.setMinInclusive(true);
            val = val.substring(2);
        } else if (val.startsWith("\"(")) {
            info.setMinInclusive(false);
            val = val.substring(2);
        } else if (val.startsWith("\"")) {
            // strip quote
            val = val.substring(1);
        }
        if (val.endsWith("]\"")) {
            info.setMaxInclusive(true);
            val = val.substring(0, val.length() - 2);
        } else if (val.endsWith(")\"")) {
            info.setMaxInclusive(false);
            val = val.substring(0, val.length() - 2);
        } else if (val.endsWith("\"")) {
            // strip quote
            val = val.substring(0, val.length() - 1);
        }
        String[] versions = val.split(",");
        try {
            PackageVersion packageVersion = new PackageVersion(versions[0]);
            info.setMinVersion(packageVersion);
            if (versions.length == 2) {
                packageVersion = new PackageVersion(versions[1]);
                info.setMaxVersion(packageVersion);
            }
        } catch (IllegalArgumentException e) {
            InvalidOSGiManifest failure = new InvalidOSGiManifest("Invalid version " + versions[0] + " in: " + text, e);
            context.addError(failure);
            return false;
        }
        return true;
    }


}
