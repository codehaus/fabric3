/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.fabric.services.expression;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.services.expression.ExpressionEvaluator;
import org.fabric3.spi.services.expression.ExpressionExpansionException;

/**
 * @version $Rev: 3524 $ $Date: 2008-03-31 14:43:51 -0700 (Mon, 31 Mar 2008) $
 */
public class ExpressionExpanderImplTestCase extends TestCase {

    public void testBeginExpansion() throws Exception {
        ExpressionEvaluator evaluator = EasyMock.createMock(ExpressionEvaluator.class);
        EasyMock.expect(evaluator.evaluate("expr1")).andReturn("expression1");
        EasyMock.replay(evaluator);
        Map<Integer, ExpressionEvaluator> evaluators = new HashMap<Integer, ExpressionEvaluator>();
        evaluators.put(0, evaluator);
        ExpressionExpanderImpl expander = new ExpressionExpanderImpl();
        expander.setEvaluators(evaluators);
        String expression = "${expr1} this is a test";
        String result = expander.expand(expression);
        assertEquals("expression1 this is a test", result);
        EasyMock.verify(evaluator);
    }

    public void testEndExpansion() throws Exception {
        ExpressionEvaluator evaluator = EasyMock.createMock(ExpressionEvaluator.class);
        EasyMock.expect(evaluator.evaluate("expr1")).andReturn("expression1");
        EasyMock.replay(evaluator);
        Map<Integer, ExpressionEvaluator> evaluators = new HashMap<Integer, ExpressionEvaluator>();
        evaluators.put(0, evaluator);
        ExpressionExpanderImpl expander = new ExpressionExpanderImpl();
        expander.setEvaluators(evaluators);
        String expression = "this is a ${expr1}";
        String result = expander.expand(expression);
        assertEquals("this is a expression1", result);
        EasyMock.verify(evaluator);
    }

    public void testBeginEndExpansion() throws Exception {
        ExpressionEvaluator evaluator = EasyMock.createMock(ExpressionEvaluator.class);
        EasyMock.expect(evaluator.evaluate("expr1")).andReturn("expression1");
        EasyMock.expect(evaluator.evaluate("expr2")).andReturn("expression2");
        EasyMock.replay(evaluator);
        Map<Integer, ExpressionEvaluator> evaluators = new HashMap<Integer, ExpressionEvaluator>();
        evaluators.put(0, evaluator);
        ExpressionExpanderImpl expander = new ExpressionExpanderImpl();
        expander.setEvaluators(evaluators);
        String expression = "${expr1} this is a ${expr2}";
        String result = expander.expand(expression);
        assertEquals("expression1 this is a expression2", result);
        EasyMock.verify(evaluator);
    }

    public void testMultipleExpansion() throws Exception {
        ExpressionEvaluator evaluator = EasyMock.createMock(ExpressionEvaluator.class);
        EasyMock.expect(evaluator.evaluate("expr1")).andReturn("expression1");
        EasyMock.expect(evaluator.evaluate("expr2")).andReturn("expression2");
        EasyMock.replay(evaluator);
        Map<Integer, ExpressionEvaluator> evaluators = new HashMap<Integer, ExpressionEvaluator>();
        evaluators.put(0, evaluator);
        ExpressionExpanderImpl expander = new ExpressionExpanderImpl();
        expander.setEvaluators(evaluators);
        String expression = "this ${expr1} is a ${expr2} string";
        String result = expander.expand(expression);
        assertEquals("this expression1 is a expression2 string", result);
        EasyMock.verify(evaluator);
    }

    public void testInvalidExpansion() throws Exception {
        ExpressionEvaluator evaluator = EasyMock.createMock(ExpressionEvaluator.class);
        EasyMock.replay(evaluator);
        Map<Integer, ExpressionEvaluator> evaluators = new HashMap<Integer, ExpressionEvaluator>();
        evaluators.put(0, evaluator);
        ExpressionExpanderImpl expander = new ExpressionExpanderImpl();
        expander.setEvaluators(evaluators);
        String expression = "this is a bad ${expr1";
        try {
            expander.expand(expression);
            fail("Invalid expression not caught");
        } catch (ExpressionExpansionException e) {
            // expected
        }
        EasyMock.verify(evaluator);
    }

    public void testNoExpression() throws Exception {
        ExpressionEvaluator evaluator = EasyMock.createMock(ExpressionEvaluator.class);
        EasyMock.expect(evaluator.evaluate("expr1")).andReturn(null);
        EasyMock.replay(evaluator);
        Map<Integer, ExpressionEvaluator> evaluators = new HashMap<Integer, ExpressionEvaluator>();
        evaluators.put(0, evaluator);
        ExpressionExpanderImpl expander = new ExpressionExpanderImpl();
        expander.setEvaluators(evaluators);
        String expression = "this is a ${expr1}";
        try {
            expander.expand(expression);
            fail("ValueNotFoundException for expression not caught");
        } catch (ValueNotFoundException e) {
            // expected
        }
        EasyMock.verify(evaluator);
    }

}