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
package org.fabric3.host.work;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default implementation of the pausable work.
 * 
 * @Revision $Date$
 *
 */
public abstract class DefaultPausableWork implements PausableWork {
	
	private AtomicBoolean active = new AtomicBoolean(true);
	private AtomicBoolean paused = new AtomicBoolean(false);
	private boolean daemon;
	
	/**
	 * Non-daemon constructor.
	 */
	public DefaultPausableWork() {
	}
	
	/**
	 * Allows to set whether the work is daemon or not.
	 * 
	 * @param daemon Whether this worker is a daemon or not.
	 */
	public DefaultPausableWork(boolean daemon) {
		this.daemon = daemon;
	}
	
	/**
	 * Pauses the job.
	 */
	public final void pause() {
		paused.set(true);
	}
	
	/**
	 * Restarts the job.
	 */
	public final void restart() {
		paused.set(false);
	}
	
	/**
	 * Terminates the job.
	 */
	public final void stop() {
		active.set(false);
	}
	
	/**
	 * Runs the job.
	 */
	public final void run() {		
		
		if (daemon) {
			while (active.get()) {
				if (paused.get()) {
					continue;
				}
				execute();
			}
		} else {
			while (paused.get()) {
			}
			execute();
		}
		
	}
	
	/**
	 * Executes the job.
	 */
	protected abstract void execute();

}
