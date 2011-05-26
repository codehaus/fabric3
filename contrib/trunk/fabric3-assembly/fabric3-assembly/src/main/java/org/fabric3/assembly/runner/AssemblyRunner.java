package org.fabric3.assembly.runner;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.utils.LoggerUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private RunnerHelper mHelper;

    private ConcurrentMap<String, Process> mRunningServers = new ConcurrentHashMap<String, Process>();

    private ExecutorService mPool = new ThreadPoolExecutor(
            0,
            100,
            1L,
            TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>()
    );

    public AssemblyRunner(AssemblyConfig pConfig) {
        mHelper = new RunnerHelper(pConfig);

        // run assembly if not processed
        if (!pConfig.isAlreadyProcessed()) {
            pConfig.process();
        }
    }

    public void startServer(final String pServerName) throws IOException {
        LoggerUtils.log("starting server ''{0}''", pServerName);

        Server server = mHelper.getServerByName(pServerName);

        ProcessBuilder pb = new ProcessBuilder("java", "-jar", "server.jar");
        pb.directory(new File(server.getServerPath().getAbsoluteFile() + "/bin"));

        final Process p = pb.start();
        mRunningServers.put(pServerName, p);

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

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                stopServer(pServerName);
            }
        });
    }

    public void stopServer(String pServerName) {
        Process p = mRunningServers.get(pServerName);

        if (null != p) {
            LoggerUtils.log("stopping server ''{0}''", pServerName);
            p.destroy();
        }

        mRunningServers.remove(pServerName);
    }

}
