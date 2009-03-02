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
 */
package org.fabric3.itest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

public class PolicyFileHelper {
    
    public static List<URL> getPolicyUrls(MavenProject project, Set<URL> moduleDependencies, String[] policyLocations) throws MojoExecutionException {
        
        try {
            List<URL> policyUrls = new LinkedList<URL>();
            if (policyLocations != null) {
                
                Set<URL> classpath = new HashSet<URL>();
                classpath.addAll(moduleDependencies);
                
                File targetDir = new File(project.getBasedir(), "target");
                File classesDir = new File(targetDir, "classes");
                File testClassesDir = new File(targetDir, "test-classes");
                
                classpath.add(classesDir.toURL());
                classpath.add(testClassesDir.toURL());
                
                ClassLoader classLoader = new URLClassLoader(classpath.toArray(new URL[classpath.size()]));
                
                for (String policyLocation : policyLocations) {
                    policyUrls.add(classLoader.getResource(policyLocation));
                }
            }
            return policyUrls;
        } catch (MalformedURLException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

}
