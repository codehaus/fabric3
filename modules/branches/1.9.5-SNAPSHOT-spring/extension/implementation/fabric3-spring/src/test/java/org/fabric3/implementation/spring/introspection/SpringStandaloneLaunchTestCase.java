package org.fabric3.implementation.spring.introspection;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.net.URI;

import junit.framework.TestCase;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.ScopedComponent;
import org.fabric3.spi.invocation.WorkContext;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericApplicationContext;

/**
 * 
 * Testing various usecases of Spring bootstraping  
 * 
 * @author palmalcheg
 *
 */
public class SpringStandaloneLaunchTestCase extends TestCase {

	public void testContextFabric3HostBootstrap() throws Exception {
		
		ComponentManager mockedCm = createMock( ComponentManager.class );
		ScopedComponent testBeanScopedComponent = createMock( ScopedComponent.class );
		
		Object[] mocks = new Object[] {  mockedCm , testBeanScopedComponent};
		
		TestBean testBean = new TestBean();
		testBean.setProperty1("bean from f3");
		
		expect(testBeanScopedComponent.getInstance((WorkContext) anyObject())).andReturn(testBean);
		expect(mockedCm.getComponent(URI.create("fabric3://runtime/"+testBean.getClass().getSimpleName()))).andReturn(null);
		expect(mockedCm.getComponent(URI.create("fabric3://domain/TestReference"))).andReturn(testBeanScopedComponent);
		
		mockedCm.register((Component) anyObject());
		expectLastCall().atLeastOnce();
		
		replay(mocks);
		
		GenericApplicationContext genCtx = new GenericApplicationContext();
		genCtx.registerBeanDefinition(
				"f3instance",
				BeanDefinitionBuilder.rootBeanDefinition(ProxyFactoryBean.class)
				        .addPropertyValue("target", mockedCm)
						.addPropertyValue("interfaces", new Class[] { ComponentManager.class })
						.getBeanDefinition());
		genCtx.refresh();
		
		ComponentManager wrappedSpringProxyCM = genCtx.getBean(ComponentManager.class);
		
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext( new String [] { "classpath:spring-context-with-sca.xml" } , genCtx);
		ctx.start();
		
		ComponentManager runtimeCMBean = ctx.getBean(ComponentManager.class);
		
		assertTrue(wrappedSpringProxyCM == runtimeCMBean);
		
		TestBean bean = (TestBean) ctx.getBean("TestReference");
		assertNotNull(bean);
		assertTrue(bean.v == testBean.v);	
		
		verify(mocks);

	}

}
