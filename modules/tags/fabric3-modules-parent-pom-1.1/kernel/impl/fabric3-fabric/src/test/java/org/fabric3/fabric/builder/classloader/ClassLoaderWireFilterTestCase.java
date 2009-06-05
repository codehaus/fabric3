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
package org.fabric3.fabric.builder.classloader;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class ClassLoaderWireFilterTestCase extends TestCase {

    public void testNoFilterPackage() throws Exception {
        String packge = getClass().getPackage().getName();
        ClassLoaderWireFilter filter = new ClassLoaderWireFilter(getClass().getClassLoader(), packge);
        // verify class can be loaded
        filter.loadClass(getClass().getName());
    }

    public void testNoFilterWildCardPackage() throws Exception {
        Package name = getClass().getPackage();
        String packge = name.getName().substring(0, name.getName().lastIndexOf(".")) + ".*";
        ClassLoaderWireFilter filter = new ClassLoaderWireFilter(getClass().getClassLoader(), packge);
        // verify class can be loaded
        filter.loadClass(getClass().getName());
    }

    public void testFilterPackage() {
        ClassLoaderWireFilter filter = new ClassLoaderWireFilter(getClass().getClassLoader(), "foo.bar");
        // verify class is not loaded
        try {
            filter.loadClass(getClass().getName());
            fail("Class should not be visible");
        } catch (ClassNotFoundException e) {
            // expected
        }
    }

}
