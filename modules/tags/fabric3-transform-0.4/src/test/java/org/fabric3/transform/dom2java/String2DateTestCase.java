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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.fabric3.spi.transform.TransformationException;

/**
 * Tests String to Date Transform
 */
public class String2DateTestCase extends BaseTransformTest {

	/**
	 * Test of converting String to Date
	 */
	public void testDateTransform() {
		final String MID_JAN = "20/01/2007";
		final String xml = "<string_to_date>" + MID_JAN + "</string_to_date>";
		try {
			Date date = getStringToDate().transform(getNode(xml), null);
			assertNotNull(date);
            assertEquals(MID_JAN, DateToString(date));
		} catch (TransformationException te) {
			fail("Transform exception should not occur " + te);
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}
	
	/**
	 * Test failure of converting String to Date
	 */
	public void testDateTransformFailure() {
		final String WRONG_DATE = "20/2007/01";
		final String xml = "<string_to_date>" + WRONG_DATE + "</string_to_date>";
		try {
			getStringToDate().transform(getNode(xml), null);
			fail("Should not reach here something wrong in [ String2Date ] code");
		} catch (TransformationException te) {
			assertNotNull(te);
			assertTrue(ParseException.class.isAssignableFrom(te.getCause().getClass()));
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}

	/**
	 * @return
	 */
	private String2Date getStringToDate() {
		return new String2Date();
	}

	/**
	 * @param date
	 * @return String from Date Object
	 */
	private String DateToString(final Date date) {
		final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		final String convertedDate = sdf.format(date);
		return convertedDate;
	}
}
