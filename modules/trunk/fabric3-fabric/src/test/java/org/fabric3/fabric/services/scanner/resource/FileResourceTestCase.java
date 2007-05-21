package org.fabric3.fabric.services.scanner.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

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
