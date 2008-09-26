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
package org.fabric3.scanner.scanner.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.fabric3.spi.scanner.FileResource;

/**
 * @version $Rev$ $Date$
 */
public class FileResourceTestCase extends TestCase {
    private File file;

    public void testChanged() throws Exception {
        FileResource resource = new FileResource(file);
        resource.reset();
        assertFalse(resource.isChanged());
        writeFile("testtest");
        assertTrue(resource.isChanged());
        writeFile("testtest");
        assertFalse(resource.isChanged());
    }


    protected void setUp() throws Exception {
        super.setUp();
        writeFile("test");
    }


    protected void tearDown() throws Exception {
        super.tearDown();
        if (file.exists()) {
            file.delete();
        }
    }

    private void writeFile(String contents) throws IOException {
        FileOutputStream stream = null;
        try {
            file = new File("fileresourcetest.txt");
            stream = new FileOutputStream(file);
            stream.write(contents.getBytes());
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }
}
