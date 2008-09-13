package org.fabric3.jsr237;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.fabric3.host.work.DefaultPausableWork;
import org.fabric3.host.work.PausableWork;
import org.fabric3.host.work.WorkScheduler;
import org.fabric3.management.WorkSchedulerMBean;
import org.osoa.sca.annotations.Property;

/**
 * Thread pool based implementation of the work scheduler.
 *
 */
public class ThreadPoolWorkScheduler implements WorkScheduler, WorkSchedulerMBean {

    private final ThreadPoolExecutor executor;
    private final Set<DefaultPausableWork> workInProgress = new CopyOnWriteArraySet<DefaultPausableWork>();
    private final AtomicBoolean paused = new AtomicBoolean();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

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
		
		Lock lock = readWriteLock.readLock();
		lock.lock();
		try {
	        Runnable runnable = new DecoratingWork(work);
	        executor.submit(runnable);
		} finally {
			lock.unlock();
		}
        
	}
	
	private class DecoratingWork implements Runnable {

		private DefaultPausableWork work;
		
		public DecoratingWork(DefaultPausableWork work) {
			this.work = work;
		}
		
		public void run() {

			if (paused.get()) {
				work.pause();
			}
			workInProgress.add(work);
			
			try {
				work.run();
			} finally {
				workInProgress.remove(work);
			}
			
		}
		
	}
	
	// ------------------ Management operations
	public int getActiveCount() {
		return executor.getActiveCount();
	}

	public int getPoolSize() {
		return executor.getCorePoolSize();
	}

	public void pause() {
		
		if (paused.get()) {
			return;
		}
		
		Lock lock = readWriteLock.writeLock();
		lock.lock();
		try {
			paused.set(true);
			for (PausableWork pausableWork : workInProgress) {
				pausableWork.pause();
			}
		} finally {
			lock.unlock();
		}
		
	}

	public void setPoolSize(int poolSize) {
		executor.setCorePoolSize(poolSize);
	}

	public void start() {
		
		if (!paused.get()) {
			return;
		}
		
		Lock lock = readWriteLock.writeLock();
		lock.lock();
		try {
			paused.set(false);
			for (PausableWork pausableWork : workInProgress) {
				pausableWork.start();
			}
		} finally {
			lock.unlock();
		}
		
	}

	public void stop() {
		
		Lock lock = readWriteLock.writeLock();
		lock.lock();
		try {
			for (PausableWork pausableWork : workInProgress) {
				pausableWork.stop();
			}
			executor.shutdown();
		} finally {
			lock.unlock();
		}
		
	}

	public Status getStatus() {
		return paused.get() ? Status.PAUSED : Status.STARTED;
	}

}
