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
package org.fabric3.jpa.runtime;

import javax.persistence.spi.PersistenceUnitInfo;

import org.fabric3.jpa.runtime.Fabric3JpaRuntimeException;
import org.fabric3.jpa.runtime.ClasspathPersistenceUnitScanner;
import org.fabric3.jpa.runtime.PersistenceUnitScanner;

import junit.framework.TestCase;

/**
 *
 * @version $Revision$ $Date$
 */
public class ClasspathPersistenceUnitScannerTestCase extends TestCase {
    
    private PersistenceUnitScanner scanner;

    protected void setUp() throws Exception {
        scanner = new ClasspathPersistenceUnitScanner();
    }

    public void testGetPersistenceUnitInfo() {
        PersistenceUnitInfo info = scanner.getPersistenceUnitInfo("test", getClass().getClassLoader());
        assertNotNull(info);
    }

    public void testGetNonExistentPersistenceUnitInfo() {
        try {
            scanner.getPersistenceUnitInfo("test1", getClass().getClassLoader());
            fail("Expected Exception");
        } catch(Fabric3JpaRuntimeException ex) {
        }
    }

}
