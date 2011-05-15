package org.fabric3.assembly.utils;

import org.fabric3.assembly.dependency.Dependency;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author Michal Capo
 */
public class DependencyUtilsTest {

    @Test
    public void testConvertFromToString() throws Exception {
        Dependency dependency = DependencyUtils.convertToDependency("org:art:1.0-SNAPSHOT");
        assertEquals("org", dependency.getGroup());
        assertEquals("art", dependency.getArtifact());
        assertEquals("1.0-SNAPSHOT", dependency.getVersion().toString());
        assertEquals("org:art:1.0-SNAPSHOT", DependencyUtils.convertToString(dependency));

        dependency = DependencyUtils.convertToDependency("org:art:class:type:1.0-SNAPSHOT");
        assertEquals("org", dependency.getGroup());
        assertEquals("art", dependency.getArtifact());
        assertEquals("class", dependency.getClassifier());
        assertEquals("type", dependency.getType());
        assertEquals("1.0-SNAPSHOT", dependency.getVersion().toString());
        assertEquals("org:art:class:type:1.0-SNAPSHOT", DependencyUtils.convertToString(dependency));
    }
}
