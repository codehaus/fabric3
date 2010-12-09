package org.fabric3.contribution.ivy.util;

import java.util.List;

import org.apache.ivy.util.AbstractMessageLogger;
import org.apache.ivy.util.Message;
import org.apache.ivy.util.MessageLogger;
import org.fabric3.contribution.ivy.IvyMonitor;

public class IvyLogger extends AbstractMessageLogger {

	private IvyMonitor monitor;

	public IvyLogger(IvyMonitor monitor) {
		this.monitor = monitor;
	}

	public void setMonitor(IvyMonitor monitor) {
		this.monitor = monitor;
	}

	public void log(String msg, int level) {
		switch (level) {
		case Message.MSG_ERR:
		case Message.MSG_WARN:
			monitor.error(msg, null);
			break;
		case Message.MSG_DEBUG:
		case Message.MSG_VERBOSE:
		case Message.MSG_INFO:
			monitor.debug(msg);
		default:
			break;
		}
	}

	public void rawlog(String msg, int level) {
		log(msg, level);
	}

	@Override
	protected void doProgress() {
	}

	@Override
	protected void doEndProgress(String msg) {
	}

}
