/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.jpa.runtime;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.fabric3.jpa.runtime.JpaConstants.CLASS;
import static org.fabric3.jpa.runtime.JpaConstants.EXCLUDE_UNLISTED_CLASSES;
import static org.fabric3.jpa.runtime.JpaConstants.JAR_FILE;
import static org.fabric3.jpa.runtime.JpaConstants.JTA_DATA_SOURCE;
import static org.fabric3.jpa.runtime.JpaConstants.MAPPING_FILE;
import static org.fabric3.jpa.runtime.JpaConstants.NON_JTA_DATA_SOURCE;
import static org.fabric3.jpa.runtime.JpaConstants.PROPERTY;
import static org.fabric3.jpa.runtime.JpaConstants.PROPERTY_NAME;
import static org.fabric3.jpa.runtime.JpaConstants.PROPERTY_VALUE;
import static org.fabric3.jpa.runtime.JpaConstants.PROVIDER;
import static org.fabric3.jpa.runtime.JpaConstants.TRANSACTION_TYPE;

/**
 * Encpasulates the information in the persistence.xml file.

 * This class is expected to be interogated by the provider only 
 * during the creation of the entity manager factory. Hence none 
 * of the values are cached, rather every time a property is queried 
 * the underlying DOM is interogated.
 *
 */
class PersistenceUnitInfoImpl implements PersistenceUnitInfo {

    /** Persistence DOM */
    private Node persistenceDom;

    /** Classloader */
    private ClassLoader classLoader;

    /** Root Url */
    private URL rootUrl;

    /** XPath API */
    private XPath xpath = XPathFactory.newInstance().newXPath();
    
    /** The name of the persistence unit wrapped by an instance of this type **/
    private String unitName;

    /**
     * Static factory method to be used instead of a directly called constructor
     * @param unitName
     * @param persistenceDom
     * @param classLoader
     * @param rootUrl
     * @return
     */
    public static PersistenceUnitInfoImpl getInstance(String unitName, Node persistenceDom, ClassLoader classLoader, URL rootUrl) {
    	PersistenceUnitInfoImpl matchedUnit = null;		
		List<String> persistenceUnitNames = getPersistenceUnitNames(persistenceDom);
		if(persistenceUnitNames.contains(unitName)) {
			matchedUnit = new PersistenceUnitInfoImpl(unitName, persistenceDom, classLoader, rootUrl);			
		}		

		return matchedUnit;    	
    }    
    
    /**
     * Initializes the properties.
     * @param unitName 
     * @param persistenceDom
     * @param classLoader
     * @param rootUrl
     */
    private PersistenceUnitInfoImpl(String unitName, Node persistenceDom, ClassLoader classLoader, URL rootUrl) {

        this.persistenceDom = persistenceDom;
        this.classLoader = classLoader;
        this.rootUrl = rootUrl;
        this.unitName = unitName;        
    }

    public void addTransformer(ClassTransformer classTransformer) {
    }

    public boolean excludeUnlistedClasses() {
        return getBooleanValue(persistenceDom, EXCLUDE_UNLISTED_CLASSES);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public List<URL> getJarFileUrls() {

        List<String> jarFiles = getMultipleValues(persistenceDom, JAR_FILE);

        try {
            List<URL> jarUrls = new LinkedList<URL>();
            for (String jarFile : jarFiles) {
                jarUrls.add(new URL(jarFile));
            }
            return jarUrls;
        } catch (MalformedURLException ex) {
            throw new Fabric3JpaRuntimeException(ex);
        }

    }

    public DataSource getJtaDataSource() {
        return null;
    }

    public List<String> getManagedClassNames() {
        return getMultipleValues(persistenceDom, CLASS);
    }

    public List<String> getMappingFileNames() {
        return getMultipleValues(persistenceDom, MAPPING_FILE);
    }

    public ClassLoader getNewTempClassLoader() {
        return null;
    }

    public DataSource getNonJtaDataSource() {
        return null;
    }

    public String getPersistenceProviderClassName() {
        return getSingleValue(persistenceDom, PROVIDER);
    }

    public String getPersistenceUnitName() {
        return unitName;
    }

    public URL getPersistenceUnitRootUrl() {
        return rootUrl;
    }

    public Properties getProperties() {
        return getProperties(persistenceDom);
    }

    public PersistenceUnitTransactionType getTransactionType() {

        String transactionType = getSingleValue(persistenceDom, TRANSACTION_TYPE);
        return "JTA".equals(transactionType) ? PersistenceUnitTransactionType.JTA : PersistenceUnitTransactionType.RESOURCE_LOCAL;

    }
    
    /**
     * @return Datasource name.
     */
    public String getDataSourceName() {
        
        String dataSourceName = getSingleValue(persistenceDom, JTA_DATA_SOURCE);
        if(dataSourceName == null) {
            dataSourceName = getSingleValue(persistenceDom, NON_JTA_DATA_SOURCE);
        }
        return dataSourceName;
        
    }

    /*
     * Extracts additional properties.
     */
    private Properties getProperties(Node root) {

        try {
        	String namedNodeExpression = getNamedNodeExpression(PROPERTY);
            NodeList nodeList = (NodeList) xpath.evaluate(namedNodeExpression, root, XPathConstants.NODESET);
            Properties data = new Properties();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element property = (Element) nodeList.item(i);
                data.put(property.getAttribute(PROPERTY_NAME), property.getAttribute(PROPERTY_VALUE));
            }
            return data;
        } catch (XPathExpressionException ex) {
            throw new Fabric3JpaRuntimeException(ex);
        }

    }

    /*
     * Gets multiple values for the specified expression.
     */
    private List<String> getMultipleValues(Node context, String expression) {

        try {
        	String namedNodeExpression = getNamedNodeExpression(expression);
            NodeList nodeList = (NodeList) xpath.evaluate(namedNodeExpression, context, XPathConstants.NODESET);
            List<String> data = new LinkedList<String>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                data.add(nodeList.item(i).getTextContent());
            }
            return data;
        } catch (XPathExpressionException ex) {
            throw new Fabric3JpaRuntimeException(ex);
        }

    }

    /*
     * Gets single value for the specified expression.
     */
    private String getSingleValue(Node context, String expression) {

        try {
        	String namedNodeExpression = getNamedNodeExpression(expression);
            String val = xpath.evaluate(namedNodeExpression, context);
            return "".equals(val) ? null : val;
        } catch (XPathExpressionException ex) {
            throw new Fabric3JpaRuntimeException(ex);
        }

    }

    /*
     * Gets single value for the specified expression.
     */
    private boolean getBooleanValue(Node context, String expression) {
        return Boolean.valueOf(getSingleValue(context, expression));
    }

    /**
     * Gets the xpath expression which provides the required value within a persistence unit of the given name 
     * @param expression the xpath expression for use within the named persistence unit
     * @return the input expression targeted for the named persistence unit
     */
    private String getNamedNodeExpression(String expression) {
		return MessageFormat.format(JpaConstants.NAMED_UNIT, unitName) + expression;
    }

    /**
     * gets the names of all of the persistence unit elements within the input node
     * @param searchBaseNode probably the base node of a persistence.xml document
     * @return a list of all persistence unit names
     */
	private static List<String> getPersistenceUnitNames(Node searchBaseNode) {    	
    	List<String> unitNames = new LinkedList<String>();
        try {
        	XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xpath.evaluate(JpaConstants.ANY_UNIT + JpaConstants.NAME, searchBaseNode, XPathConstants.NODESET);            
            for (int i = 0; i < nodeList.getLength(); i++) {
            	unitNames.add(nodeList.item(i).getTextContent());
            }            
        } catch (XPathExpressionException ex) {
            throw new Fabric3JpaRuntimeException(ex);
        }
        
        return unitNames;
	}	    

}
