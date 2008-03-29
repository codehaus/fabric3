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

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class MavenImportTestCase extends TestCase {

    public void testParse1() {
        MavenImport imprt = new MavenImport();
        imprt.setVersion("4.0.2");
        assertEquals("4",imprt.getMajorVersion());
        assertEquals("0",imprt.getMinorVersion());
        assertEquals("2",imprt.getRevision());
    }

    public void testParse2() {
        MavenImport imprt = new MavenImport();
        imprt.setVersion("4.0");
        assertEquals("4",imprt.getMajorVersion());
        assertEquals("0",imprt.getMinorVersion());
    }

    public void testParse3() {
        MavenImport imprt = new MavenImport();
        imprt.setVersion("4");
        assertEquals("4",imprt.getMajorVersion());
    }

    public void testParse4() {
        MavenImport imprt = new MavenImport();
        imprt.setVersion("4.0.2-SNAPSHOT");
        assertEquals("4",imprt.getMajorVersion());
        assertEquals("0",imprt.getMinorVersion());
        assertEquals("2",imprt.getRevision());
    }
    public void testParse5() {
        MavenImport imprt = new MavenImport();
        imprt.setVersion("4.0-SNAPSHOT");
        assertEquals("4",imprt.getMajorVersion());
        assertEquals("0",imprt.getMinorVersion());
    }

}
