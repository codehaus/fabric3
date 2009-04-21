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
package org.fabric3.spi.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines primitives for use in dynamic classloading.
 *
 * @version $Revision$ $Date$
 */
public class ParamTypes {

    public static final Map<String, Class<?>> PRIMITIVES_TYPES;

    static {
        PRIMITIVES_TYPES = new HashMap<String, Class<?>>();
        PRIMITIVES_TYPES.put("void", Void.class);
        PRIMITIVES_TYPES.put("boolean", Boolean.TYPE);
        PRIMITIVES_TYPES.put("byte", Byte.TYPE);
        PRIMITIVES_TYPES.put("short", Short.TYPE);
        PRIMITIVES_TYPES.put("int", Integer.TYPE);
        PRIMITIVES_TYPES.put("long", Long.TYPE);
        PRIMITIVES_TYPES.put("float", Float.TYPE);
        PRIMITIVES_TYPES.put("double", Double.TYPE);
    }

}
