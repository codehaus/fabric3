package org.fabric3.tests.spring.standalone;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class SpringStandaloneTestCase extends TestCase {	
	
	public void tes1tStandloneStartBootstrap() {
		new ClassPathXmlApplicationContext("classpath:external.application.context.xml");
	}
	
	public void test() {
	}

}
