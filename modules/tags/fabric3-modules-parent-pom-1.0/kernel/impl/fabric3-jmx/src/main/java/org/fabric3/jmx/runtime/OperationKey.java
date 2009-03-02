/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.jmx.runtime;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @version $Rev$ $Date$
 */
public class OperationKey {
    private final String name;
    private final String[] params;
    private final int hashCode;

    public OperationKey(String name, String[] params) {
        this.name = name;
        this.params = params;
        hashCode = 31 * this.name.hashCode() + Arrays.hashCode(this.params);
    }

    public OperationKey(Method method) {
        this.name = method.getName();
        Class<?>[] paramTypes = method.getParameterTypes();
        this.params = new String[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            params[i] = paramTypes[i].getName();
        }
        hashCode = 31 * this.name.hashCode() + Arrays.hashCode(this.params);
    }

    public String toString() {
        StringBuilder sig = new StringBuilder();
        sig.append(name).append('(');
        if (params.length > 0) {
            sig.append(params[0]);
            for (int i = 1; i < params.length; i++) {
                sig.append(',').append(params[i]);
            }
        }
        sig.append(')');
        return sig.toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OperationKey that = (OperationKey) o;

        return name.equals(that.name) && Arrays.equals(params, that.params);

    }

    public int hashCode() {
        return hashCode;
    }
}
