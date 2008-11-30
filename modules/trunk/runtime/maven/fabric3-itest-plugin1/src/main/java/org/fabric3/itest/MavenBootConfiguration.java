package org.fabric3.itest;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;

public class MavenBootConfiguration {
    private URL systemScdl;
    private URL intentsLocation;
    private String managementDomain;
    private Properties properties;
    private File outputDirectory;
    private String systemConfigDir;
    private String systemConfig;
    private ClassLoader bootClassLoader;
    private ClassLoader hostClassLoader;
    private Set<URL> moduleDependencies;
    private List<Dependency> extensionDependencies;
    private Log log;
    private ExtensionHelper extensionHelper;
    
    public List<Dependency> getExtensionDependencies() {
        return extensionDependencies;
    }

    public void setExtensionDependencies(List<Dependency> extensionDependencies) {
        this.extensionDependencies = extensionDependencies;
    }

    public URL getSystemScdl() {
        return systemScdl;
    }

    public void setSystemScdl(URL systemScdl) {
        this.systemScdl = systemScdl;
    }

    public URL getIntentsLocation() {
        return intentsLocation;
    }

    public void setIntentsLocation(URL intentsLocation) {
        this.intentsLocation = intentsLocation;
    }

    public String getManagementDomain() {
        return managementDomain;
    }

    public void setManagementDomain(String managementDomain) {
        this.managementDomain = managementDomain;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getSystemConfigDir() {
        return systemConfigDir;
    }

    public void setSystemConfigDir(String systemConfigDir) {
        this.systemConfigDir = systemConfigDir;
    }

    public String getSystemConfig() {
        return systemConfig;
    }

    public void setSystemConfig(String systemConfig) {
        this.systemConfig = systemConfig;
    }

    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }

    public void setBootClassLoader(ClassLoader bootClassLoader) {
        this.bootClassLoader = bootClassLoader;
    }

    public ClassLoader getHostClassLoader() {
        return hostClassLoader;
    }

    public void setHostClassLoader(ClassLoader hostClassLoader) {
        this.hostClassLoader = hostClassLoader;
    }

    public Set<URL> getModuleDependencies() {
        return moduleDependencies;
    }

    public void setModuleDependencies(Set<URL> moduleDependencies) {
        this.moduleDependencies = moduleDependencies;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public ExtensionHelper getExtensionHelper() {
        return extensionHelper;
    }

    public void setExtensionHelper(ExtensionHelper extensionHelper) {
        this.extensionHelper = extensionHelper;
    }
}
