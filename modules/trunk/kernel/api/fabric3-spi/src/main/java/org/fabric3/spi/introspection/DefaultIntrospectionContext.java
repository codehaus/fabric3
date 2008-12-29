/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
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
    }

    public DefaultIntrospectionContext(ClassLoader targetClassLoader,
                                       URL sourceBase,
                                       String targetNamespace,
                                       URI contributionUri,
                                       TypeMapping typeMapping) {
        this.targetClassLoader = targetClassLoader;
        this.sourceBase = sourceBase;
        this.targetNamespace = targetNamespace;
        this.contributionUri = contributionUri;
        this.typeMapping = typeMapping;
    }

    public DefaultIntrospectionContext(URI contributionUri, ClassLoader classLoader, String targetNamespace) {
        this(classLoader, null, targetNamespace, contributionUri, null);
    }

    /**
     * Constructor defining properties of this context.
     *
     * @param classLoader     the classloader for loading application resources
     * @param contributionUri the active contribution URI
     * @param scdlLocation    the location of the SCDL defining this composite
     */
    public DefaultIntrospectionContext(ClassLoader classLoader, URI contributionUri, URL scdlLocation) {
        this(classLoader, scdlLocation, null, contributionUri, null);
    }

    /**
     * Initializes from a parent context.
     *
     * @param parentContext   Parent context.
     * @param targetNamespace Target namespace.
     */
    public DefaultIntrospectionContext(IntrospectionContext parentContext, String targetNamespace) {
        this(parentContext.getTargetClassLoader(),
             parentContext.getSourceBase(),
             targetNamespace,
             parentContext.getContributionUri(),
             parentContext.getTypeMapping());
    }

    /**
     * Initializes from a parent context.
     *
     * @param parentContext Parent context.
     * @param typeMapping   mapping of formal types
     */
    public DefaultIntrospectionContext(IntrospectionContext parentContext, TypeMapping typeMapping) {
        this(parentContext.getTargetClassLoader(),
             parentContext.getSourceBase(),
             parentContext.getTargetNamespace(),
             parentContext.getContributionUri(),
             typeMapping);
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

    public URI getContributionUri() {
        return contributionUri;
    }

    public TypeMapping getTypeMapping() {
        return typeMapping;
    }
}
