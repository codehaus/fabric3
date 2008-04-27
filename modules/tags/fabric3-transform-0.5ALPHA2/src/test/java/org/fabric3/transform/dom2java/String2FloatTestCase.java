/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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

package org.fabric3.transform.dom2java;

import org.fabric3.transform.TransformationException;

/**
 * Tests String to Float Transform
 */
public class  String2FloatTestCase extends BaseTransformTest {

	/**
	 * Test of converting String to Float
	 */
	public void testFloatTransform() {
		final String ANY_FLOAT_NUMBER = "99.00";
		final String xml = "<string_to_float>" + ANY_FLOAT_NUMBER + "</string_to_float>";
		try {
			final double convertedFloat = getStringToFloat().transform(getNode(xml), null);
			assertNotNull(convertedFloat);
            assertEquals(99.00, convertedFloat);
		} catch (TransformationException te) {
			fail("Transform exception should not occur " + te);
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}
	
	/**
	 * Test failure of converting String to Float
	 */
	public void testFloatTransformFailure() {
	    final String NON_FLOAT = "NON FLOAT";
		final String xml = "<string_to_float>" + NON_FLOAT + "</string_to_float>";
		try {
			getStringToFloat().transform(getNode(xml), null);
			fail("Should not reach here something wrong in [ String2Float ] code");
		} catch (TransformationException te) {
			assertNotNull(te);
			assertTrue(NumberFormatException.class.isAssignableFrom(te.getCause().getClass()));
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}

	/**
	 * @return
	 */
	private String2Float getStringToFloat() {
		return new String2Float();
	}
}
