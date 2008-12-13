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
package org.fabric3.contribution.connector;

/**
 * A bridging classloader that filters class and resource loading to a specified set of classes. This is used to enforce the semantics of a
 * JavaContributionWire.
 *
 * @version $Revision$ $Date$
 */
public class JavaContributionWireFilter extends ClassLoader {
    private String[] importedPackage;

    /**
     * Constructor.
     *
     * @param parent          the parent classloader.
     * @param importedPackage the package the wire imports
     */
    public JavaContributionWireFilter(ClassLoader parent, String importedPackage) {
        super(parent);
        this.importedPackage = importedPackage.split("\\.");
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        String[] clazz = name.split("\\.");
        for (int i = 0; i < importedPackage.length; i++) {
            String packageName = importedPackage[i];
            if ("*".equals(packageName)) {
                // wildcard reached, packages match
                break;
            } else if (clazz.length - 1 >= i && !packageName.equals(clazz[i])) {
                throw new ClassNotFoundException(name);
            }

        }
        return super.loadClass(name, resolve);
    }
}
