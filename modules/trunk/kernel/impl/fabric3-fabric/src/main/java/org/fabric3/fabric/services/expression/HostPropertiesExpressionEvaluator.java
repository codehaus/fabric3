package org.fabric3.fabric.services.expression;

import org.osoa.sca.annotations.Reference;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.services.expression.ExpressionEvaluator;

/**
 * @version $Revision$ $Date$
 */
public class HostPropertiesExpressionEvaluator implements ExpressionEvaluator {
    private HostInfo info;

    public HostPropertiesExpressionEvaluator(@Reference HostInfo info) {
        this.info = info;
    }

    public String evaluate(String expression) {
        return info.getProperty(expression, null);
    }
}
