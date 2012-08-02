package org.fabric3.implementation.spring.runtime.component;

import java.net.URI;

import javax.xml.namespace.QName;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.component.ComponentException;
import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceInitException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.ScopedComponent;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.oasisopen.sca.Constants;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

/**
 * @org.apache.xbean.XBean element="service" rootElement="true"
 * @author palmalcheg
 * 
 */
public class SpringServiceBeanComponent implements ScopedComponent, InitializingBean, DisposableBean, BeanNameAware, ApplicationContextAware {

	private MonitorLevel level;
	private URI classLoaderUri;
	private QName target;
	private Class<?> type;
	private String name;
	private ApplicationContext applicationContext;
	private QName deployable;
	
	public void setTarget(QName target) {
		this.target = target;
	}
	
	public void setType(Class<?> type) {
		this.type = type;
	}

	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public MonitorLevel getLevel() {
		return level;
	}

	public void setLevel(MonitorLevel level) {
		this.level = level;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(target, "No target specified.");
		
		Assert.notNull(name, "No reference name specified.");
		Assert.notNull(applicationContext, "No application context specified.");
		ComponentManager cm = applicationContext.getBean(ComponentManager.class);
		if (cm==null){
			// search in parent context if any
			cm = BeanFactoryUtils.beanOfTypeIncludingAncestors(applicationContext, ComponentManager.class);
		}
		deployable = new QName(Constants.SCA_NS,applicationContext.getDisplayName());
		Assert.notNull(cm, "Component Manager is not found, may be Fabric 3 wasn't fully initialized");
		cm.register(this);
	}

	public void endUpdate() {
	}

	public URI getClassLoaderId() {
		return classLoaderUri;
	}

	public QName getDeployable() {
		return deployable;
	}

	public URI getUri() {
		return URI.create("fabric3://domain/"+name);
	}

	public void setClassLoaderId(URI uri) {
		this.classLoaderUri = uri;
	}

	public void start() throws ComponentException {
	}

	public void startUpdate() {
	}

	public void stop() throws ComponentException {
	}
	
	public Class<?> getObjectType() {
		return type;
	}

	public boolean isSingleton() {
		return false;
	}

	public void destroy() throws Exception {
	}

	public void setBeanName(String name) {
		this.name = name;
	}

	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		this.applicationContext = ctx;
	}

	public ObjectFactory<Object> createObjectFactory() {
		return null;
	}

	public Object getInstance(WorkContext arg0) throws InstanceLifecycleException {
		return target;
	}

	public void releaseInstance(Object arg0, WorkContext arg1) throws InstanceDestructionException {
	}

	public Object createInstance(WorkContext arg0) throws ObjectCreationException {
		return target;
	}

	public boolean isEagerInit() {
		return false;
	}

	public void reinject(Object arg0) throws InstanceLifecycleException {
	}

	public void startInstance(Object arg0, WorkContext arg1) throws InstanceInitException {
	}

	public void stopInstance(Object arg0, WorkContext arg1) throws InstanceDestructionException {
	}

}
