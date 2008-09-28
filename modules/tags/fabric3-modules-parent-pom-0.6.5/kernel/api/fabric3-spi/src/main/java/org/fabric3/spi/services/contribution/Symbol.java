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
package org.fabric3.spi.services.contribution;

import java.io.Serializable;

/**
 * Encapsulates an addressable key for a symbol space.
 *
 * @version $Rev$ $Date$
 */
public abstract class Symbol<KEY> implements Serializable {
    private static final long serialVersionUID = -1064949020858816670L;
    protected KEY key;

    public Symbol(KEY key) {
        this.key = key;
    }

    public KEY getKey() {
        return key;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Symbol symbol = (Symbol) o;

        return !(key != null ? !key.equals(symbol.key) : symbol.key != null);

    }

    public int hashCode() {
        return (key != null ? key.hashCode() : 0);
    }


}
