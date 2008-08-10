package org.fabric3.jsr237;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fabric3.host.work.DefaultPausableWork;
import org.fabric3.host.work.WorkScheduler;
import org.osoa.sca.annotations.Property;

/**
 * Thread pool based implementation of the work scheduler.
 *
 */
public class ThreadPoolWorkScheduler implements WorkScheduler {

    private final ThreadPoolExecutor executor;
    private final Set<DefaultPausableWork> workInProgress = new CopyOnWriteArraySet<DefaultPausableWork>();
    private final AtomicBoolean paused = new AtomicBoolean();

    /**
     * Initializes the thread-pool. Supports unbounded work with a fixed pool size. If all the workers 
     * are busy, work gets queued.
     *
     * @param threadPoolSize Thread-pool size.
     */
    public ThreadPoolWorkScheduler(@Property(name = "poolSize") int poolSize,
    							   @Property(name = "pauseOnStart") boolean pauseOnStart) {
        executor = new ThreadPoolExecutor(poolSize, poolSize, Long.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        paused.set(pauseOnStart);
    }

	public <T extends DefaultPausableWork> void scheduleWork(T work) {

		if (paused.get()) {
			work.pause();
		}
		
        Runnable runnable = new DecoratingWork(work);
        executor.submit(runnable);
        
	}
	
	private class DecoratingWork implements Runnable {

		private DefaultPausableWork work;
		
		public DecoratingWork(DefaultPausableWork work) {
			this.work = work;
		}
		
		public void run() {
			workInProgress.add(work);
			try {
				work.run();
			} finally {
				workInProgress.remove(work);
			}
		}
		
	}

}
