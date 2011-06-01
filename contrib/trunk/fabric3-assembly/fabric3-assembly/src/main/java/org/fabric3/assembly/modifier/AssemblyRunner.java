package org.fabric3.assembly.modifier;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.exception.RunnerException;
import org.fabric3.assembly.utils.ConfigUtils;
import org.fabric3.assembly.utils.LoggerUtils;
import org.fabric3.assembly.utils.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Michal Capo
 */
public class AssemblyRunner {

    protected AssemblyConfig mConfig;

    private ConcurrentMap<String, Process> mRunningServers = new ConcurrentHashMap<String, Process>();

    private ExecutorService mPool = new ThreadPoolExecutor(
            0,
            100,
            1L,
            TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>()
    );

    public AssemblyRunner(AssemblyConfig pConfig) {
        mConfig = pConfig;

        // run assembly if not processed
        if (!pConfig.isAlreadyProcessed()) {
            pConfig.process();
        }
    }

    public void startServer(final String pServerName) throws IOException {
        List<org.fabric3.assembly.configuration.Runtime> runtimes = ConfigUtils.findRuntimesByServerName(mConfig, pServerName);
        for (Runtime runtime : runtimes) {
            startServer(pServerName, runtime.getRuntimeName());
        }
    }

    public void startServer(final String pServerName, final String pRuntimeName) throws IOException {
        if (StringUtils.isBlank(pRuntimeName)) {
            throw new RunnerException("You specified a 'null' runtime for server ''{0}'' to start.");
        }

        LoggerUtils.log("Starting server ''{0}'', runtime ''{1}''", pServerName, pRuntimeName);

        Server server = ConfigUtils.findServerByName(mConfig, pServerName);

        ProcessBuilder pb = new ProcessBuilder("java", "-Djava.net.preferIPv4Stack=true", "-jar", "server.jar", pRuntimeName);
        pb.directory(new File(server.getServerPath().getAbsoluteFile() + "/bin"));

        final Process p = pb.start();
        mRunningServers.put(pServerName + pRuntimeName, p);

        mPool.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

                String temp;
                while ((temp = reader.readLine()) != null) {
                    System.out.println(temp);
                }

                return null;
            }
        });
        mPool.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                String temp;
                while ((temp = reader.readLine()) != null) {
                    System.out.println(temp);
                }

                return null;
            }
        });

        java.lang.Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                stopServer(pServerName);
            }
        });
    }

    public void stopServer(String pServerName) {
        List<org.fabric3.assembly.configuration.Runtime> runtimes = ConfigUtils.findRuntimesByServerName(mConfig, pServerName);
        for (Runtime runtime : runtimes) {
            stopServer(pServerName, runtime.getRuntimeName());
        }
    }

    public void stopServer(String pServerName, String pRuntimeName) {
        String key = pServerName + pRuntimeName;

        Process p = mRunningServers.get(key);

        if (null != p) {
            LoggerUtils.log("stopping server ''{0}'' with runtime ''{1}''", pServerName, pRuntimeName);
            p.destroy();
            mRunningServers.remove(key);
        }

    }

}
