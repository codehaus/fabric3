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
package org.fabric3.featureset;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * 
 * Mojo for generating a feature set from a set of requested extensions. A feature set can be built by composing a number of other feature sets, 
 * and or including a set of explicitly requested extensions. A feature set is published as maven artifact with the extension .xml. This can be later 
 * referenced by the itest and webapp plugins, instead of explictly referencing all the extensions included in the feature set. User applications are 
 * expected to have a separate maven module to build the feature set, and then the installed artifact will be reused from the other modules that use 
 * the itest and webapp plugins.
 * 
 * An example usage of the feature set plugin is shown below,
 * 
 * <pre>
 *    &lt;plugin&gt;
 *       &lt;groupId&gt;org.codehaus.fabric3&lt;/groupId&gt;
 *       &lt;artifactId&gt;fabric3-feature-set-plugin&lt;/artifactId&gt;
 *       &lt;extensions&gt;true&lt;/extensions&gt;
 *       &lt;configuration&gt;
 *          &lt;extensions&gt;
 *             &lt;dependency&gt;
 *                &lt;groupId&gt;org.mycompanyf&lt;/groupId&gt;
 *                &lt;artifactId&gt;mycompany-extension&lt;/artifactId&gt;
 *             &lt;/dependency&gt;
 *          &lt;/extensions&gt;
 *          &lt;includes&gt;
 *             &lt;dependency&gt;
 *                &lt;groupId&gt;org.codehaus.fabric3&lt;/groupId&gt;
 *                &lt;artifactId&gt;fabric3-hibernate-feature-set&lt;/artifactId&gt;
 *             &lt;/dependency&gt;
 *          &lt;/includes&gt;
 *       &lt;/configuration&gt;
 *     &lt;/plugin&gt;
 * </pre>
 *
 * @version $Revision$ $Date$
 */
public class Fabric3FeatureSetMojo extends AbstractMojo {

    /**
     *
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    protected MavenProject project;
    
    /**
     * @parameter
     */
    protected Dependency[] extensions;
    
    /**
     * @parameter
     */
    protected Dependency[] includes;
    
    /**
     * Generates the feature set files.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        // TODO Auto-generated method stub
        
    }

}
