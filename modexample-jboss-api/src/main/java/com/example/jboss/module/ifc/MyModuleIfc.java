package com.example.jboss.module.ifc;

import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 *
 * Interface defining jboss module service management operations.
 *
 */
public interface MyModuleIfc {
	/**
	 * 
	 * Start a module. Shall not contain any blocking operation.
	 * 
	 * @param context the context which can be used to trigger an asynchronous
	 *                service start
	 * 
	 * @throws StartException
	 */
	public void start(StartContext context) throws StartException;

	/**
	 * 
	 * Stops a module. Shall not contain any blocking operation.
	 * 
	 * @param context
	 *
	 */
	public void stop(StopContext context);

	/**
	 * Returns a string identifier of the CUSTOM Module
	 * 
	 * @return String
	 */
	public String getName();
}
