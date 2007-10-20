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
public class  String2ShortTestCase extends BaseTransformTest {

	/**
	 * Test of converting String to Short
	 */
	public void testShortTransform() {
		final String ANY_SHORT = "153";
		final String xml = "<string_to_short>" + ANY_SHORT + "</string_to_short>";
		try {
			final short convertedShort = getStringToShort().transform(getNode(xml), null);
			assertNotNull(convertedShort);
            assertEquals(153, convertedShort);
		} catch (TransformationException te) {
			fail("Transform exception should not occur " + te);
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}
	
	/**
	 * Test failure of converting String to Short
	 */
	public void testShortTransformFailure() {
		final String INVALID_SHORT = "153908765";
		final String xml = "<string_to_short>" + INVALID_SHORT + "</string_to_short>";
		try {
			getStringToShort().transform(getNode(xml), null);
			fail("Should not reach here something wrong in [ String2Short ] code");
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
	private String2Short getStringToShort() {
		return new String2Short();
	}
}
