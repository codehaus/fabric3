package org.fabric3.contribution;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.fabric3.contribution.stub.SCAArtifactStub;
import org.fabric3.contribution.stub.SCAMavenProjectStub;
import org.fabric3.contribution.stub.SCAModelStub;
/* this file name must end with *TestCase.java, *Test.java does not get picked up as documented :(
 * This test is based off of the maven-war-plugin and maven-ejb-plugin test cases
 */
public class Fabric3ContributionMojoTestCase extends AbstractMojoTestCase {

	protected File getTestDirectory(String testName) {
		return new File(getBasedir(),
				"target" + File.separator + "test-classes"  + File.separator + "unit" + File.separator + testName );
	}

	/* configure the mojo for execution */
	protected Fabric3ContributionMojo configureMojo(String testName) throws Exception{
		File testDirectory = getTestDirectory(testName);
		File pomFile =  new File(testDirectory, "pom.xml");
		Fabric3ContributionMojo mojo = (Fabric3ContributionMojo) lookupMojo("package", pomFile);
		assertNotNull(mojo);
		File outputDir = new File(testDirectory, "target");
		setVariableValueToObject(mojo, "outputDirectory", outputDir);
		setVariableValueToObject(mojo, "classesDirectory", new File(outputDir,"classes"));
		setVariableValueToObject(mojo, "jarArchiver", new JarArchiver());
	    Model model = new SCAModelStub();
		SCAMavenProjectStub stub = new SCAMavenProjectStub(model);
		stub.setFile(pomFile);
		SCAArtifactStub artifact=new SCAArtifactStub();
		setVariableValueToObject(mojo, "contributionName", "test");
		artifact.setFile(new File( testDirectory, "test.zip" ));
		stub.setArtifact(artifact);
		setVariableValueToObject(mojo, "project", stub);
		return mojo;
	}
	
	public void testNoClassesDirectory() throws Exception {
		Fabric3ContributionMojo mojo = configureMojo("no-directory");
		try{
		mojo.execute();
		}catch (Exception e){
			assertTrue("exception not mojo exception",e instanceof MojoExecutionException);
			assertTrue(e.getCause() instanceof FileNotFoundException);
			assertTrue(e.getCause().getMessage().indexOf("does not exist")>-1);
			return;
		}
		fail("directory does not exist, should have failed");
	}
	
	
	
	public void testNoFile() throws Exception {
		Fabric3ContributionMojo mojo = configureMojo("no-file");
		try{
		mojo.execute();
		}catch (Exception e){
			assertTrue("exception not mojo exception",e instanceof MojoExecutionException);
			assertTrue(e.getCause() instanceof FileNotFoundException);
			assertTrue(e.getCause().getMessage().indexOf("Missing sca-contribution.xml")>-1);
			return;
		}
		fail("directory does not exist, should have failed");
	}
	
	public void testCorrect() throws Exception {
		Fabric3ContributionMojo mojo = configureMojo("correct");
		try{
		mojo.execute();
		}catch (Exception e){
			e.printStackTrace();
			fail("should have succeeded");
		}
		File testFile = new File (getTestDirectory("correct"), "target" + File.separator + "test.zip");
		assertTrue(testFile.exists());
		
		HashSet jarContent = new HashSet();
		JarFile jarFile = new JarFile( testFile );
        JarEntry entry;
        Enumeration enumeration = jarFile.entries();

        while ( enumeration.hasMoreElements() )
        {
            entry = (JarEntry) enumeration.nextElement();
            jarContent.add( entry.getName() );
        }
        assertTrue( "sca-contribution.xml file not found", jarContent.contains( "META-INF/sca-contribution.xml" ) );
        assertTrue( "content not found", jarContent.contains( "test.properties" ) );
        
	}
	
	public void testDependencies() throws Exception {
		Fabric3ContributionMojo mojo = configureMojo("dependency");
		MavenProject p = mojo.project;
		Set artifacts =p.getArtifacts();
		SCAArtifactStub dep = new SCAArtifactStub();
		dep.setArtifactId("test-dep-1");
		dep.setFile(new File (getTestDirectory("dependency"), "dep-1.jar"));
		dep.setType("jar");
		artifacts.add(dep);
		dep = new SCAArtifactStub();
		dep.setArtifactId("test-dep-2");
		dep.setFile(new File (getTestDirectory("dependency"), "dep-2.jar"));
		dep.setType("sca-contribution");
		artifacts.add(dep);
		try{
		mojo.execute();
		}catch (Exception e){
			e.printStackTrace();
			fail("should have succeeded");
		}
		File testFile = new File (getTestDirectory("dependency"), "target" + File.separator + "test.zip");
		assertTrue(testFile.exists());
		
		HashSet jarContent = new HashSet();
		JarFile jarFile = new JarFile( testFile );
        JarEntry entry;
        Enumeration enumeration = jarFile.entries();

        while ( enumeration.hasMoreElements() )
        {
            entry = (JarEntry) enumeration.nextElement();
            jarContent.add( entry.getName() );
        }
        assertTrue( "sca-contribution.xml file not found", jarContent.contains( "META-INF/sca-contribution.xml" ) );
        assertTrue( "content not found", jarContent.contains( "test.properties" ) );
        assertTrue( "dependency not added", jarContent.contains( "META-INF/lib/dep-1.jar" ) );
        assertFalse( "dependency of type sca-contribution should not have been added", jarContent.contains( "META-INF/lib/dep-2.jar" ) );
	}
	

}
