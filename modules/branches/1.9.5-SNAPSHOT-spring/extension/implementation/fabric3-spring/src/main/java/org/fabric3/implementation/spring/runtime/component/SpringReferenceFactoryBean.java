package org.fabric3.implementation.spring.runtime.component;

import java.net.URI;

import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.component.ScopedComponent;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

/**
 * @org.apache.xbean.XBean element="reference"
 * @author palmalcheg
 * 
 */
public class SpringReferenceFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware, InitializingBean, BeanNameAware {
	
	private String name;
	private ApplicationContext applicationContext;
	private ComponentManager componentManager;
	private T scaReference;
	private Class<T> type;	
	
	public void setType(Class<T> type) {
		this.type = type;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		this.applicationContext = ctx;
	}
	
	public void setComponentManager(ComponentManager componentManager) {
		this.componentManager = componentManager;
	}
	
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(type, "No reference type.");
		Assert.notNull(name, "No reference name specified.");
		Assert.notNull(applicationContext, "No application context specified.");
		if (componentManager==null){
			// search in parent context if any
			componentManager = BeanFactoryUtils.beanOfTypeIncludingAncestors(applicationContext, ComponentManager.class);
		}
		Assert.notNull(componentManager, "No Fabric3 Component manager is available.");
		ScopedComponent component = (ScopedComponent) componentManager.getComponent(URI.create(name));
		if (component == null && !name.startsWith("fabric3://domain/")){
			// Find with a 'fabric3://domain/' prefix
			component = (ScopedComponent) componentManager.getComponent(URI.create("fabric3://domain/"+name));
		}
		Assert.notNull(component, String.format("No SCA component found for {%s}:%s ", type.getName() , name));
		this.scaReference = (T) component.getInstance(WorkContextTunnel.getThreadWorkContext());
		Assert.notNull(this.scaReference, String.format("No SCA reference found for {%s}:%s ", type.getName() , name));
	}

	public void setBeanName(String name) {
		this.name = name;
	}

	public T getObject() throws Exception {
		return this.scaReference;
	}

	public Class<?> getObjectType() {
		return type;
	}

	public boolean isSingleton() {
		return false;
	}
}
