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
package org.fabric3.policy.infoset;

import java.util.List;

import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Evaluates an XPath policy expression against the logical domain model.
 *
 * @version $Revision$ $Date$
 */
public interface PolicyEvaluator {

    /**
     * Evaluates the XPath expression against the  to the target component, i.e. selects it or one of its children.
     *
     * @param xpathExpression the XPath expression
     * @param target          the target component
     * @return a list of selected nodes, i.e. LogicalComponnet, LogicalService, LogicalReference, LogicalBinding, or LogicalOperation
     * @throws PolicyEvaluationException if there is an exception evaluating the expression
     */
    List<?> evaluate(String xpathExpression, LogicalComponent<?> target) throws PolicyEvaluationException;

    /**
     * Determines if the XPath expression applies to the target component, i.e. selects it or one of its children.
     *
     * @param appliesToXPath the XPath expression
     * @param target         the target component
     * @return true if the expression applies
     * @throws PolicyEvaluationException if there is an exception evaluating the expression
     */
    boolean doesApply(String appliesToXPath, LogicalComponent<?> target) throws PolicyEvaluationException;

    boolean doesAttach(String attachesToXPath, LogicalComponent<?> target, LogicalComponent<?> context) throws PolicyEvaluationException;


}
