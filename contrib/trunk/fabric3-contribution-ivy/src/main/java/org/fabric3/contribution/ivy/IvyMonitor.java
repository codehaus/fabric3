package org.fabric3.contribution.ivy;

import java.net.URI;

import org.fabric3.api.annotation.monitor.Debug;
import org.fabric3.api.annotation.monitor.Info;
import org.fabric3.api.annotation.monitor.Severe;

public interface IvyMonitor {
	
	@Debug("Debug with : {0}")
	public void debug(String msg);
	
	@Info("IvyContribution : {0}")
	void info(String msg);
	
	@Info("Ivy-Module : {0} installed")
	void profileInstalled(URI profile);
	
	@Severe("IvyMonitor Error :{0}, Exception : {1}")
	void error(String msg,Throwable t);
}
