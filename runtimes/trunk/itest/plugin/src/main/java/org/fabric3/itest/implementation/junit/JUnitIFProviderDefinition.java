package org.fabric3.itest.implementation.junit;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import org.fabric3.spi.model.physical.InstanceFactoryProviderDefinition;
import org.fabric3.spi.model.instance.ValueSource;
import org.fabric3.fabric.model.physical.instancefactory.InjectionSiteMapping;

/**
 * JUnit-based instance factory provider definition.
 *
 * @version $Revision$ $Date$
 */
public class JUnitIFProviderDefinition extends InstanceFactoryProviderDefinition {

    // Implementation class
    private String implementationClass;

    // Constructor arguments
    private List<String> constructorArguments = new LinkedList<String>();

    // Init method
    private String initMethod;

    // Destroy method
    private String destroyMethod;

    // Constructor injection sites
    private List<ValueSource> cdiSources = new LinkedList<ValueSource>();

    // Injection sites
    private List<InjectionSiteMapping> injectionSites = new LinkedList<InjectionSiteMapping>();

    // Property sites
    private Map<ValueSource, String> propertyValues = new HashMap<ValueSource, String>();

    /**
     * returns the constructor argument.
     *
     * @return the constructorArguments Fully qualified names of the constructor
     *         atgument types.
     */
    public List<String> getConstructorArguments() {
        return Collections.unmodifiableList(constructorArguments);
    }

    /**
     * Adds a constructor argument type.
     *
     * @param constructorArgument the constructorArguments to set
     */
    public void addConstructorArgument(String constructorArgument) {
        constructorArguments.add(constructorArgument);
    }

    /**
     * Returns constructor injection names.
     *
     * @return the constructorNames Constructor injection names.
     */
    public List<ValueSource> getCdiSources() {
        return Collections.unmodifiableList(cdiSources);
    }

    /**
     * Adds a constructor injection name.
     *
     * @param cdiSource Constructor injection name.
     */
    public void addCdiSource(ValueSource cdiSource) {
        cdiSources.add(cdiSource);
    }

    /**
     * Gets the destroy method.
     *
     * @return Destroy method name.
     */
    public String getDestroyMethod() {
        return destroyMethod;
    }

    /**
     * Sets the destroy method.
     *
     * @param destroyMethod Destroy method name.
     */
    public void setDestroyMethod(String destroyMethod) {
        this.destroyMethod = destroyMethod;
    }

    /**
     * Gets the implementation class.
     *
     * @return Implementation class.
     */
    public String getImplementationClass() {
        return implementationClass;
    }

    /**
     * Sets the implementation class.
     *
     * @param implementationClass Implementation class.
     */
    public void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    /**
     * Gets the init method.
     *
     * @return Init method name.
     */
    public String getInitMethod() {
        return initMethod;
    }

    /**
     * Sets the init method.
     *
     * @param initMethod Init method name.
     */
    public void setInitMethod(String initMethod) {
        this.initMethod = initMethod;
    }

    /**
     * Gets the injection sites.
     *
     * @return Injection sites.
     */
    public List<InjectionSiteMapping> getInjectionSites() {
        return Collections.unmodifiableList(injectionSites);
    }

    /**
     * Adds an injection site.
     *
     * @param injectionSite site.
     */
    public void addInjectionSite(InjectionSiteMapping injectionSite) {
        injectionSites.add(injectionSite);
    }

    /**
     * Returns a read-only view of properties.
     *
     * @return Read-only view of properties.
     */
    public Map<ValueSource, String> getPropertyValues() {
        return Collections.unmodifiableMap(propertyValues);
    }

    /**
     * Adds a property to the definition.
     *
     * @param valueSource Injection source for the property.
     * @param propertValue    String value of the property.
     */
    public void addPropertValue(ValueSource valueSource, String propertValue) {
        propertyValues.put(valueSource, propertValue);
    }

}
