package org.fabric3.fabric.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.fabric3.spi.classloader.MultiParentClassLoader;

/**
 * A specialized ObjectOutputStream that serializes objects with classloader information so they may be deserialized by {@link
 * MultiClassLoaderObjectInputStream}.
 *
 * @version $Revision$ $Date$
 */
public class MultiClassLoaderObjectOutputStream extends ObjectOutputStream {
    public MultiClassLoaderObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    protected void annotateClass(Class<?> cl) throws IOException {
        if (cl.getClassLoader() instanceof MultiParentClassLoader) {
            // write the classloader id
            String id = ((MultiParentClassLoader) cl.getClassLoader()).getName().toString();
            this.writeByte(id.length());
            writeBytes(id);
        } else {
            // use normal classloader resolution
            this.writeByte(-1);
        }
    }

}