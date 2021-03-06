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

import java.net.MalformedURLException;
import java.net.URL;

import org.fabric3.transform.TransformationException;


/**
 * Tests String to URL Transformation
 */
public class  String2URLTestCase extends BaseTransformTest {

	/**
	 * Test for successful transformation from String to URL 
	 */
	public void testURLTransformSuccess() {
		final String urlContent = "ftp://testf3.org";
		final String xml = "<string_to_url>" + urlContent + "</string_to_url>";
		
		try {
			final URL transformedURL = getStringToURL().transform(getNode(xml), null);
			assertNotNull(transformedURL);
		} catch (TransformationException te) {
			fail("TransformationException : - Should Not Occur" + te);
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}
	
	/**
	 * Test for unsuccessful Conversion from String URL
	 */
	public void testURLTransformationSuccess() {
		final String erroredURL = "failedURL";
		final String xml = "<string_to_urlerror>" + erroredURL + "</string_to_urlerror>";
		
		try {
			getStringToURL().transform(getNode(xml), null);
			fail("Should not convert to URL");
		} catch (TransformationException te) {
			assertNotNull(te);
			MalformedURLException.class.isAssignableFrom(te.getCause().getClass());
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}
	
	/**
	 * @return StringToURL
	 */
	private String2URL getStringToURL() {
		return new String2URL();
	}
}
