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

import org.fabric3.spi.transform.TransformationException;

/**
 * Tests String to Integer Transform
 */
public class  String2IntegerTestCase extends BaseTransformTest {

	/**
	 * Test of converting String to Integer
	 */
	public void testIntegerTransform() {
		final String ANY_NUMBER = "99";
		final String xml = "<string_to_integer>" + ANY_NUMBER + "</string_to_integer>";
		try {
			final int convertedInt = getStringToInteger().transform(getNode(xml), null);
			assertNotNull(convertedInt);
            assertEquals(99, convertedInt);
		} catch (TransformationException te) {
			fail("Transform exception should not occur " + te);
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}
	
	/**
	 * Test failure of converting String to Integer
	 */
	public void testIntegerTransformFailure() {
	    final String NON_INTEGER = "1009876548888899";
		final String xml = "<string_to_integer>" + NON_INTEGER + "</string_to_integer>";
		try {
			getStringToInteger().transform(getNode(xml), null);
			fail("Should not reach here something wrong in [ String2Integer ] code");
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
	private String2Integer getStringToInteger() {
		return new String2Integer();
	}
}
