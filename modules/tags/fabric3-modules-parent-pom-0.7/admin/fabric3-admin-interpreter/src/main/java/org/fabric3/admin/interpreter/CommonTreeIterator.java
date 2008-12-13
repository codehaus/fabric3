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
package org.fabric3.admin.interpreter;

import java.util.Iterator;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

/**
 * Convenience class for casting the Antlr Iterator type.
 *
 * @version $Revision$ $Date$
 */
public class CommonTreeIterator implements Iterator<Token> {
    private Iterator<?> delegate;

    public CommonTreeIterator(Iterator<?> delegate) {
        this.delegate = delegate;
    }

    public boolean hasNext() {
        return delegate.hasNext();
    }

    public Token next() {
        return ((CommonTree) delegate.next()).getToken();
    }

    public void remove() {
        delegate.remove();
    }
}
