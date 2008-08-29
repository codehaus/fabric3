package org.fabric3.fabric.services.expression;

import org.fabric3.spi.services.expression.ExpressionExpansionException;

/**
 * Thrown when an expression value cannot be found.
 *
 * @version $Revision$ $Date$
 */
public class ValueNotFoundException extends ExpressionExpansionException {
    private static final long serialVersionUID = 851637435862308601L;

    public ValueNotFoundException(String string) {
        super(string);
    }
}