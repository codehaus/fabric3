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
package org.fabric3.spi.services.expression;

/**
 * Evalautes an expression against some information set. For example, an implementation may return the value of a system property. One application of
 * an ExpressionEvaluators is to provide a mechanism to override attrbiutes and values in a composite.
 *
 * @version $Revision$ $Date$
 */
public interface ExpressionEvaluator {

    /**
     * Evaluate an expression, returning the corresponding value or null.
     *
     * @param expression the expression to evaluate
     * @return the evaluated expression or null
     */
    String evaluate(String expression);

}
