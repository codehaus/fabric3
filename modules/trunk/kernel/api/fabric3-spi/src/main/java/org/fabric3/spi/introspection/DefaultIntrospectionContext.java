/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.spi.introspection;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.host.contribution.ValidationFailure;

/**
 * Default implementation of an IntrospectionContext.
 *
 * @version $Rev$ $Date$
 */
public class DefaultIntrospectionContext implements IntrospectionContext {
    private List<ValidationFailure> errors = new ArrayList<ValidationFailure>();
    private List<ValidationFailure> warnings = new ArrayList<ValidationFailure>();
    private ClassLoader targetClassLoader;
    private URL sourceBase;
    private String targetNamespace;
    private URI contributionUri;
    private TypeMapping typeMapping;

    public DefaultIntrospectionContext() {
        this.typeMapping = new TypeMapping();
    }

    public DefaultIntrospectionContext(URI contributionUri,
                                       ClassLoader classLoader,
                                       URL sourceBase,
                                       String targetNamespace,
                                       TypeMapping typeMapping) {
        this.targetClassLoader = classLoader;
        this.sourceBase = sourceBase;
        this.targetNamespace = targetNamespace;
        this.contributionUri = contributionUri;
        if (typeMapping == null) {
            this.typeMapping = new TypeMapping();
        } else {
            this.typeMapping = typeMapping;
        }
    }

    /**
     * Constructor.
     *
     * @param contributionUri the active contribution URI
     * @param classLoader     the classloader for loading application resources
     */
    public DefaultIntrospectionContext(URI contributionUri, ClassLoader classLoader) {
        this(contributionUri, classLoader, null, null, null);
    }

    /**
     * Constructor.
     *
     * @param contributionUri the active contribution URI
     * @param classLoader     the classloader for loading application resources
     * @param scdlLocation    the location of the SCDL defining this composite
     */
    public DefaultIntrospectionContext(URI contributionUri, ClassLoader classLoader, URL scdlLocation) {
        this(contributionUri, classLoader, scdlLocation, null, null);
    }

    /**
     * Initializes from a parent context, overriding the target namespace.
     *
     * @param parentContext   Parent context.
     * @param targetNamespace Target namespace.
     */
    public DefaultIntrospectionContext(IntrospectionContext parentContext, String targetNamespace) {
        this(parentContext.getContributionUri(),
             parentContext.getTargetClassLoader(),
             parentContext.getSourceBase(),
             targetNamespace,
             parentContext.getTypeMapping());
    }

    /**
     * Initializes from a parent context.
     *
     * @param parentContext Parent context.
     */
    public DefaultIntrospectionContext(IntrospectionContext parentContext) {
        this(parentContext.getContributionUri(),
             parentContext.getTargetClassLoader(),
             parentContext.getSourceBase(),
             parentContext.getTargetNamespace(),
             parentContext.getTypeMapping());
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<ValidationFailure> getErrors() {
        return errors;
    }

    public void addError(ValidationFailure e) {
        errors.add(e);
    }

    public void addErrors(List<ValidationFailure> errors) {
        this.errors.addAll(errors);
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public List<ValidationFailure> getWarnings() {
        return warnings;
    }

    public void addWarning(ValidationFailure e) {
        warnings.add(e);
    }

    public void addWarnings(List<ValidationFailure> warnings) {
        this.warnings.addAll(warnings);
    }

    public ClassLoader getTargetClassLoader() {
        return targetClassLoader;
    }

    public URL getSourceBase() {
        return sourceBase;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    public URI getContributionUri() {
        return contributionUri;
    }

    public TypeMapping getTypeMapping() {
        return typeMapping;
    }
}
