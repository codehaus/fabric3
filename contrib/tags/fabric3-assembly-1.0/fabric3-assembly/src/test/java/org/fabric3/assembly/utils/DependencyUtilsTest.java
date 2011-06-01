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
        String stringDependency = "group:artifact";
        Dependency dependency = DependencyUtils.parseDependency(stringDependency);
        assertEquals("group", dependency.getGroup());
        assertEquals("artifact", dependency.getArtifact());
        assertEquals(null, dependency.getVersion());
        assertEquals(null, dependency.getClassifier());
        assertEquals("jar", dependency.getType());
        assertEquals(stringDependency, DependencyUtils.convertToString(dependency));

        stringDependency = "group:artifact:1.0-SNAPSHOT";
        dependency = DependencyUtils.parseDependency(stringDependency);
        assertEquals("group", dependency.getGroup());
        assertEquals("artifact", dependency.getArtifact());
        assertEquals("1.0-SNAPSHOT", dependency.getVersion().toString());
        assertEquals(null, dependency.getClassifier());
        assertEquals("jar", dependency.getType());
        assertEquals(stringDependency, DependencyUtils.convertToString(dependency));

        stringDependency = "group:artifact:classifier@type";
        dependency = DependencyUtils.parseDependency(stringDependency);
        assertEquals("group", dependency.getGroup());
        assertEquals("artifact", dependency.getArtifact());
        assertEquals(null, dependency.getVersion());
        assertEquals("classifier", dependency.getClassifier());
        assertEquals("type", dependency.getType());
        assertEquals(stringDependency, DependencyUtils.convertToString(dependency));

        stringDependency = "group:artifact:1.0-SNAPSHOT:classifier@type";
        dependency = DependencyUtils.parseDependency(stringDependency);
        assertEquals("group", dependency.getGroup());
        assertEquals("artifact", dependency.getArtifact());
        assertEquals("1.0-SNAPSHOT", dependency.getVersion().toString());
        assertEquals("classifier", dependency.getClassifier());
        assertEquals("type", dependency.getType());
        assertEquals(stringDependency, DependencyUtils.convertToString(dependency));
    }
}
