/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.management;

import org.fabric3.api.annotation.Management;
import org.fabric3.api.annotation.ManagementName;

/**
 * Management interface for the work scheduler.
 *
 */
@Management
@ManagementName("Fabric3 Work Scheduler")
public interface WorkSchedulerMBean {
	
	@ManagementName("Set Maximum Threads")
	void setMaximumSize(@ManagementName("Maximum Threads") int maximumSize);

	@ManagementName("Get Maximum Threads")
	int getMaximumSize();

	@ManagementName("Get Active Threads")
	int getActiveCount();

	@ManagementName("Pause All The Threads")
	void pause();

	@ManagementName("Stop All The Threads")
	void stop();

	@ManagementName("Restart Paused Threads")
	void restart();

	@ManagementName("Start All The Threads")
	void start();

}
