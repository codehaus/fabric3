/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ÒLicenseÓ), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an Òas isÓ basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.fabric.services.contribution.manifest;

/**
 * Represents Java package information specified in a Java import or export contribution manifest declaration.
 *
 * @version $Revision$ $Date$
 */
public class PackageInfo {
    private String name;
    private PackageVersion minVersion;
    private PackageVersion maxVersion;
    private boolean required;
    private String[] packageNames;

    /**
     * Constructor for an import package declaration specifying a version range.
     *
     * @param name       the package name
     * @param minVersion the minimum version
     * @param maxVersion the maximum version
     * @param required   if package resolution is required
     */
    public PackageInfo(String name, PackageVersion minVersion, PackageVersion maxVersion, boolean required) {
        setName(name);
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
        this.required = required;
    }

    /**
     * Constructor for an import or export package declaration specifying an exact version.
     *
     * @param name     the package name
     * @param version  the minimum version
     * @param required if package resolution is required
     */
    public PackageInfo(String name, PackageVersion version, boolean required) {
        setName(name);
        this.minVersion = version;
        this.required = required;
    }

    /**
     * Constructor for an import or export package declaration.
     *
     * @param name the package name
     */
    public PackageInfo(String name) {
        setName(name);
        this.required = true;
    }

    /**
     * Constructor for an import package declaration specifying if it is required.
     *
     * @param name     the package name
     * @param required if package resolution is required
     */
    public PackageInfo(String name, boolean required) {
        setName(name);
        this.required = required;
    }

    /**
     * The package name.
     *
     * @return package name
     */
    public String getName() {
        return name;
    }

    /**
     * The minimum required version. When no maximum version is specified, the minimum version is interpreted as a specific required version.
     *
     * @return the version
     */
    public PackageVersion getMinVersion() {
        return minVersion;
    }

    /**
     * The maximum required version. When no maximum version is specified, the minimum version is interpreted as a specific required version.
     *
     * @return the maximum version or null
     */
    public PackageVersion getMaxVersion() {
        return maxVersion;
    }

    /**
     * Returns true if the package is required.
     *
     * @return true if the package is required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Returns true if this import package matches the specified export package according to OSGi R4 semantics.
     *
     * @param exportPackage the export package
     * @return true if this import package matches the specified export package
     */
    public boolean matches(PackageInfo exportPackage) {
        // match psackage names
        int i = 0;
        for (String packageName : exportPackage.packageNames) {
            if ("*".equals(packageName)) {
                // export wild card found, packages match
                break;
            } else if (packageNames.length - 1 >= i && !packageName.equals(packageNames[i])) {
                return false;
            }
            i++;
            if (packageNames.length - 1 == i && packageNames.length > exportPackage.packageNames.length && !"*".equals(packageNames[i])) {
                return false;
            }
        }
        // The exporting PackageInfo.minVersion is used as the export version. Compare the range against that.
        return !(minVersion != null && minVersion.compareTo(exportPackage.minVersion) > 0)
                && (maxVersion == null || (maxVersion.compareTo(exportPackage.minVersion) >= 0));
    }

    private void setName(String name) {
        this.name = name;
        packageNames = name.split("\\.");
    }


    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (minVersion != null ? minVersion.hashCode() : 0);
        result = 31 * result + (maxVersion != null ? maxVersion.hashCode() : 0);
        result = 31 * result + (required ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Package: " + name);
        if (minVersion != null) {
            builder.append(" Min: ").append(minVersion);
        }
        if (maxVersion != null) {
            builder.append(" Max: ").append(maxVersion);
        }
        builder.append(" Required: ").append(required);
        return builder.toString();
    }
}
