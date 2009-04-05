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

import org.jaxen.JaxenException;

import org.fabric3.policy.xpath.LogicalModelXPath;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalScaArtifact;

/**
 * @version $Revision$ $Date$
 */
public class PolicyEvaluatorImpl implements PolicyEvaluator {

    @SuppressWarnings({"unchecked"})
    public List<LogicalScaArtifact<?>> evaluate(String xpathExpression, LogicalComponent<?> component) throws PolicyEvaluationException {
        try {
            LogicalModelXPath xpath = new LogicalModelXPath(xpathExpression);
            Object ret = xpath.evaluate(component);
            if (ret instanceof List) {
                return (List<LogicalScaArtifact<?>>) ret;
            }
            throw new PolicyEvaluationException("Invalid select expression: " + xpathExpression);
        } catch (JaxenException e) {
            throw new PolicyEvaluationException(e);
        }
    }

    public boolean doesApply(String appliesToXPath, LogicalComponent<?> target) throws PolicyEvaluationException {
        try {
            LogicalModelXPath xpath = new LogicalModelXPath(appliesToXPath);
            Object selected = xpath.evaluate(target);
            if (selected instanceof Boolean) {
                return (Boolean) selected;
            } else if (selected instanceof List) {
                return !((List) selected).isEmpty();
            }
            return false;
        } catch (JaxenException e) {
            throw new PolicyEvaluationException(e);
        }

    }

    public boolean doesAttach(String attachesToXPath, LogicalComponent<?> target, LogicalComponent<?> context) throws PolicyEvaluationException {
        try {
            LogicalModelXPath xpath = new LogicalModelXPath(attachesToXPath);
            Object selected = xpath.evaluate(context);
            if (selected instanceof List) {
                List<?> list = (List<?>) selected;
                if (list.isEmpty()) {
                    return false;
                }
                for (Object entry : list) {
                    if (entry instanceof LogicalComponent) {
                        if (entry == target) {
                            return true;
                        }
                    } else if (entry instanceof Bindable) {
                        if (((Bindable) entry).getParent() == target) {
                            return true;
                        }
                    } else if (entry instanceof LogicalBinding) {
                        if (((LogicalBinding) entry).getParent().getParent() == target) {
                            return true;
                        }
                    } else if (entry instanceof LogicalOperation) {
                        if (((LogicalOperation) entry).getParent().getParent() == target) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (JaxenException e) {
            throw new PolicyEvaluationException(e);
        }

    }

}