package org.fabric3.runtime.development;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.host.runtime.StartException;
import org.fabric3.runtime.development.host.DevelopmentHostInfoImpl;
import org.fabric3.runtime.development.host.DevelopmentRuntime;

/**
 * Client API for instantiating a local Fabric3 development domain environment. Usage is as follows:
 * <pre>
 * Domain domain = new Domain();
 * domain.activate(url);
 * MyService service = domain.connectTo(MyService.class, compositeUri, "MyComponent");
 * //...
 * domain.stop();
 *  </pre>
 * In the above example, <code>domain.activate(url)</code> transiently contributes and activates the composite in the
 * domain. As this operation is transient, the composite will be removed when <code>domain.stop</code> is called.
 * <p/>
 * This API is intended to be used within a development environment for prototyping. To setup, peform the following
 * steps:
 * <pre>
 * <ul>
 * <li> Download and install the Fabric3 development distribution. Set the system property
 * <code>fabric3.dev.home</code> to point to the distribution.
 * <li> Set the IDE classpath to include the jars in the /lib directory of the distribution. These jars include the
 * SCA,
 * Fabric3, and development runtime APIs.
 * </ul> Instantiate a Domain according to the usage outlined above.
 * </pre>
 * Note that instantiating a Domain and activating a composite will bootstrap a Fabric3 runtime in a child classloader
 * of the application classloader. This will ensure Fabric3 implementation classes are isolated from the application
 * classpath.
 * <p/>
 * The development domain also supports mocking composite references. For example, the following will mock a composite
 * reference, "myReference", that implements the <code>SomeReference</code> interface:
 * <pre>
 * SomeReference mock = .... // create the mock object
 * domain.registerMockReference("myReference", SomeReference.class, mock);
 * domain.activate(url);
 * // perform a test
 * // verify the mock
 * </pre>
 * Note that frameworks such as EasyMock (http://www.easymock.org/) may be used to create and verify mock objects.
 *
 * @version $Rev$ $Date$
 */
public class Domain {
    public static final String FABRIC3_DEV_HOME = "fabric3.dev.home";
    public static final String SYSTEM_SCDL = "/system/system.composite";
    public static final URI DOMAIN_URI = URI.create("fabric3://./domain");

    private DevelopmentRuntime runtime;
    private RuntimeLifecycleCoordinator<DevelopmentRuntime, Bootstrapper> coordinator;
    private String extensionsDirectory;

    public void setExtensionsDirectory(String extensionsDirectory) {
        this.extensionsDirectory = extensionsDirectory;
    }

    public void activate(URL compositeFile) {
        if (runtime == null) {
            bootRuntime();
        }
        runtime.activate(compositeFile);
    }

    public <T> T connectTo(Class<T> interfaze, String componentUri) {
        if (runtime == null) {
            throw new IllegalStateException("No composite is activated");
        }
        return runtime.connectTo(interfaze, componentUri);
    }

    public <T> void registerMockReference(String name, Class<T> interfaze, T mock) {
        if (runtime == null) {
            bootRuntime();
        }
        runtime.registerMockReference(name, interfaze, mock);
    }

    public void stop() {
        try {
            Future<Void> future = coordinator.shutdown();
            future.get();
        } catch (ShutdownException e) {
            throw new RuntimeShutdownException(e);
        } catch (ExecutionException e) {
            throw new RuntimeShutdownException(e);
        } catch (InterruptedException e) {
            throw new RuntimeShutdownException(e);
        }
        runtime = null;
    }

    @SuppressWarnings({"unchecked"})
    private void bootRuntime() {
        String home = System.getProperty(FABRIC3_DEV_HOME);
        File baseDir;
        if (home == null) {
            home = calculateHome();
            baseDir = new File(home).getParentFile();
            if (baseDir == null || !baseDir.exists()) {
                throw new InvalidFabric3HomeException("Fabric3 home system property not set", FABRIC3_DEV_HOME);
            }
        } else {
            baseDir = new File(home);
        }
        if (!baseDir.exists()) {
            throw new InvalidFabric3HomeException("Fabric3 home system directory does not exist", home);
        }
        File libDir = new File(baseDir, "boot");
        if (!libDir.exists()) {
            throw new InvalidFabric3HomeException("Invalid Fabric3 installation: boot directory not found", home);
        }
        File[] libraries = libDir.listFiles();
        URL[] urls = new URL[libraries.length];
        for (int i = 0; i < libraries.length; i++) {
            try {
                urls[i] = libraries[i].toURI().toURL();
            } catch (MalformedURLException e) {
                throw new AssertionError(e);
            }
        }
        try {
            ClassLoader cl = new URLClassLoader(urls, getClass().getClassLoader());
            getClass().getClassLoader().loadClass("org.osoa.sca.ServiceUnavailableException");
            URL systemSCDL = new File(baseDir, SYSTEM_SCDL).toURI().toURL();
            Class<?> bootstrapperClass = cl.loadClass("org.fabric3.fabric.runtime.ScdlBootstrapperImpl");
            ScdlBootstrapper bootstrapper = (ScdlBootstrapper) bootstrapperClass.newInstance();
            bootstrapper.setScdlLocation(systemSCDL);
            Class<?> runtimeClass = cl.loadClass("org.fabric3.runtime.development.host.DevelopmentRuntimeImpl");
            runtime = (DevelopmentRuntime) runtimeClass.newInstance();
            URL baseDirUrl = baseDir.toURI().toURL();
            File dir;
            if (extensionsDirectory == null) {
                dir = new File(baseDir, "extensions");
            } else {
                dir = new File(extensionsDirectory);
            }
            runtime.setHostInfo(new DevelopmentHostInfoImpl(DOMAIN_URI, baseDirUrl, dir));
            runtime.setHostClassLoader(cl);
            Class<?> coordinatorClass =
                    cl.loadClass("org.fabric3.runtime.development.host.DevelopmentCoordinator");
            coordinator =
                    (RuntimeLifecycleCoordinator<DevelopmentRuntime, Bootstrapper>) coordinatorClass.newInstance();
            coordinator.bootPrimordial(runtime, bootstrapper, cl, cl);
            coordinator.initialize();
            Future<Void> future = coordinator.joinDomain(-1);
            future.get();
            future = coordinator.recover();
            future.get();
            future = coordinator.start();
            future.get();
        } catch (InstantiationException e) {
            throw new InvalidConfigurationException("Error instantiating runtime classes are missing", e);
        } catch (IllegalAccessException e) {
            throw new InvalidConfigurationException("Invalid configuration", e);
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigurationException("Runtime classes are missing", e);
        } catch (InitializationException e) {
            throw new InvalidConfigurationException("Error initializing runtime", e);
        } catch (MalformedURLException e) {
            throw new InvalidConfigurationException("Error initializing runtime", e);
        } catch (StartException e) {
            throw new InvalidConfigurationException("Error initializing runtime", e);
        } catch (ExecutionException e) {
            throw new InvalidConfigurationException("Error initializing runtime", e);
        } catch (InterruptedException e) {
            throw new InvalidConfigurationException("Error initializing runtime", e);
        }

    }

    /**
     * Used if no {@link #FABRIC3_DEV_HOME} is specified, calculates the location of the runtime installation relative
     * to the jar containing the Domain class.
     *
     * @return the directory containing the runtime installation
     */
    private String calculateHome() {
        String home;
        String path = Domain.class.getResource("Domain.class").toString();
        path = path.substring(0, path.indexOf("!"));
        path = path.substring(0, path.lastIndexOf("/"));
        home = path.substring(path.lastIndexOf(":") + 1);
        return home;
    }

}
