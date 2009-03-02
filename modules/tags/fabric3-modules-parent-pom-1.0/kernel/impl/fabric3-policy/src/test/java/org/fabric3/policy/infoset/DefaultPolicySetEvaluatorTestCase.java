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

import junit.framework.TestCase;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public class DefaultPolicySetEvaluatorTestCase extends TestCase {
    
    private PolicyInfosetBuilder policyInfosetBuilder = new DefaultPolicyInfosetBuilder();
    private PolicySetEvaluator policySetEvaluator = new DefaultPolicySetEvaluator();

    public void testDoesApplyForBinding() {
        
        Element bindableElement = policyInfosetBuilder.buildInfoSet(DefaultPolicyInfosetBuilderTestCase.getTestBinding());
        String expression = "@name='testReference' and ../@name='testComponent' and $Operation='testOperation'";
        assertTrue(policySetEvaluator.doesApply(bindableElement, expression, "testOperation"));
        
    }

    public void testDoesApplyForBindingSimple() {
        
        Element bindableElement = policyInfosetBuilder.buildInfoSet(DefaultPolicyInfosetBuilderTestCase.getTestBinding());
        String expression = "local-name() = 'reference'";
        assertTrue(policySetEvaluator.doesApply(bindableElement, expression, "testOperation"));
        
    }

    public void testDoesApplyForBindingFalse() {
        
        Element bindableElement = policyInfosetBuilder.buildInfoSet(DefaultPolicyInfosetBuilderTestCase.getTestBinding());
        String expression = "@name='testService' and ../@name='testComponent' and $Operation='testOperation'";
        assertFalse(policySetEvaluator.doesApply(bindableElement, expression, "testOperation1"));
        
    }

    public void testDoesApplyForImplementation() {
        
        Element componentElement = policyInfosetBuilder.buildInfoSet(DefaultPolicyInfosetBuilderTestCase.getTestComponent());
        String expression = "@name='testComponent'";
        assertTrue(policySetEvaluator.doesApply(componentElement, expression, "testOperation"));
        
    }

    public void testDoesApplyForImplementationFalse() {
        
        Element componentElement = policyInfosetBuilder.buildInfoSet(DefaultPolicyInfosetBuilderTestCase.getTestComponent());
        String expression = "@name='testComponent' and $Operation='testOperation'";
        assertFalse(policySetEvaluator.doesApply(componentElement, expression, "testOperation1"));
        
    }

}
