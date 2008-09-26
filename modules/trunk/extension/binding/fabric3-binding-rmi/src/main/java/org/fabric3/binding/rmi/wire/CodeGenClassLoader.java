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
package org.fabric3.binding.rmi.wire;

import java.security.CodeSource;
import java.security.SecureClassLoader;

public class CodeGenClassLoader extends SecureClassLoader {
    private static final CodeSource CS =
            CodeGenClassLoader.class.getProtectionDomain().getCodeSource();
    private final String name;

    public CodeGenClassLoader(String name, ClassLoader parent) {
        super(parent);
        this.name = name;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" name: ").append(name);
        return sb.toString();
    }


    public Class defineClass(String name, byte[] bytes) {
        return super.defineClass(name, bytes, 0, bytes.length, CS);
    }

}
