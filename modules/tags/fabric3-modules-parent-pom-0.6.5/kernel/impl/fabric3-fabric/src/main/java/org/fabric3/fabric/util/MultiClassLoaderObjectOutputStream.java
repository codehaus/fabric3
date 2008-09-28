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