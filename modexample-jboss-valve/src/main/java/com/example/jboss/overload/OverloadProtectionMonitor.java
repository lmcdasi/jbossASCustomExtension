package com.example.jboss.overload;

import java.util.logging.Logger;

import com.example.jboss.overload.OverloadProtectionHandler.OverloadProtectionStatus;

/**
 * This class is responsible for managing the OLP monitoring.
 * 
 */
public class OverloadProtectionMonitor {

	private static Logger logger = Logger.getLogger(OverloadProtectionMonitor.class
			.getName());
	
	private static final OverloadProtectionMonitor managerInstance = new OverloadProtectionMonitor();

	//... cpuMonitor;
	//... memMonitor;
	
	public static OverloadProtectionMonitor getInstance() {
		return managerInstance;
	}

	/**
	 * Singleton's private constructor
	 */
	private OverloadProtectionMonitor() {
		doCreate();
		doStart();
		doPoststart();
	}

	/**
	 * Creates the monitors needed in the monitoring activity:
	 * 
	 */

	public void doCreate() {
		//... new cpuMonitor
		//... new memMonitor
	}

	/**
	 * Starts the monitoring observers with default configuration
	 */
	public void doStart() {
		//... start cpuObserver
		//... start memObserver
		
	}

	public void doPoststart() {
		// TODO - any post start init
	}

	void shutdownCpuObserver() {
		//... start cpuObserver
	}

	void shutdownMemoryObserver() {
		//... shutdown memObserver
	}
	
	public void doShutdown() {
		shutdownCpuObserver();
		shutdownMemoryObserver();
	}

	public boolean isOverloadReached() {
		return isOverloadReached(true);
	}

	public boolean isOverloadReached(boolean isOR) {
		//if (isOR) {
		//	return cpuObserver.isAlarmRaised() || memObserver.isAlarmRaised();
		//} else {
		//	return cpuObserver.isAlarmRaised() && memObserver.isAlarmRaised();
		//}
		return false;
	}

	public boolean isMaximumOverloadReached() {
		return isMaximumOverloadReached(true);
	}

	public boolean isMaximumOverloadReached(boolean isOR) {
		// return alarm when maximum memory used
		return false;
	}

	public int getRetryAfterInterval() {
		// TODO - make it configurable
		return DefaultOLPMonitoringConfig.retryAfterInterval;
	}

	public boolean isEnabled() {
		// TODO - implement a way to enable/disable the OLP service
		return false;
	}

	public OverloadProtectionStatus getStatus(boolean isOR) {
		// TODO - make it configurable based on service enable/disable.
		if (true) {
			return OverloadProtectionStatus.DISABLED;
		}
		if (isMaximumOverloadReached(isOR)) {
			return OverloadProtectionStatus.BLOCKED;
		}
		if (isOverloadReached(isOR)) {
			return OverloadProtectionStatus.OVERLOADED;
		}
		return OverloadProtectionStatus.OK;
	}

	public OverloadProtectionStatus getStatus() {
		// TODO - implement status
		return getStatus(true);
	}
}
