package com.example.jboss.module.extension.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.server.Services;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceListener;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import com.example.jboss.module.extension.ModuleExtension;
import com.example.jboss.module.extension.SubsystemState;
import com.example.jboss.module.extension.SubsystemState.MyModule;
import com.example.jboss.module.ifc.MyModuleIfc;

/**
 * 
 * Service that manages the modules JBOSS startup {@link SingletonProvider}
 * Application can use this method to bootstrap their modules in order to avoid
 * creating a module extension for each module. The modules startup respect the
 * ordering within the XML configuration file matters.
 * 
 */
public class FrameworkBootstrapService implements Service<FrameworkBootstrapService> {
	private static final Logger log = Logger.getLogger(FrameworkBootstrapService.class.getName());

	/**
	 * Service name used to retrieve the singleton instance from the service
	 * registry
	 * 
	 */
	public static ServiceName SERVICE_NAME = Services.JBOSS_AS.append(ModuleExtension.SUBSYSTEM_NAME, "bootstrap");

	/**
	 * Framework instance
	 */
	private final static FrameworkBootstrapService INSTANCE = new FrameworkBootstrapService();

	/**
	 * StartContext
	 */
	private StartContext startupContext = null;

	/**
	 * Services that need access to the
	 * SubsystemState/ServerEnvironment/SocketBinding can use this service to have
	 * it injected.
	 * 
	 * SubsystemState holds the CUSTOM MODULE subsystem configuration data from the XML
	 * configuration file
	 */
	private final InjectedValue<SubsystemState> injectedSubsystemState = new InjectedValue<SubsystemState>();

	/* List of loaded services */
	private final ConcurrentHashMap<ModuleIdentifier, List<MyModuleIfc>> myModules = new ConcurrentHashMap<ModuleIdentifier, List<MyModuleIfc>>();

	/**
	 * Constructor of TCCLSingletonService
	 */
	private FrameworkBootstrapService() {
		log.info("MyModule FrameworkBootstrapService - created");
	}

	public static ServiceController<FrameworkBootstrapService> addService(final ServiceTarget target,
			final ServiceListener<Object>... listeners) {
		ServiceBuilder<FrameworkBootstrapService> builder = target.addService(SERVICE_NAME, INSTANCE);
		builder.addDependency(SubsystemState.SERVICE_NAME, SubsystemState.class, INSTANCE.injectedSubsystemState);
		builder.addListener(listeners);
		return builder.install();
	}

	/**
	 * Method to check if the framework bootstrap service has been started
	 */
	public boolean isFrameworkBootratpServiceStarted() {
		return (startupContext != null);
	}

	/**
	 * Start method - called when as7 starts. The start will occur in parallel with
	 * all other services that as7 starts.
	 * 
	 * @param context the context which can be used to trigger an asynchronous
	 * service start
	 */
	public void start(StartContext context) throws StartException {
		log.info("MyModule FrameworkBootstrapService - starting.");
		this.startupContext = context;

		/* For every module defined add the service within the as7 msc */
		for (MyModule myModule : this.getMyModules()) {
			try {
				startModule(myModule);
			} catch (ModuleLoadException e) {
				e.printStackTrace();
				throw new StartException("MyModule FrameworkBootstrapService - unable to start module: "
						+ myModule.getIdentifier().getName());
			}
		}

		log.info("MyModule FrameworkBootstrapService - started.");
	}

	/**
	 * Stop method. The modules will be stopped in the same order that they were
	 * started
	 * 
	 * @param context the context which can be used to trigger an asynchronous
	 *                service stop
	 */
	public void stop(StopContext context) {
		Iterator<List<MyModuleIfc>> myIter = myModules.values().iterator();
		while (myIter.hasNext()) {
			List<MyModuleIfc> listOfServices = (List<MyModuleIfc>) myIter.next();

			Iterator<MyModuleIfc> services = listOfServices.iterator();
			while (services.hasNext()) {
				MyModuleIfc currentService = services.next();
				currentService.stop(context);
			}
		}

		log.info("FrameworkBootstrapService - stopped");
	}

	/**
	 * Returns the singleton service object reference when using as7 ifc's.
	 */
	public FrameworkBootstrapService getValue() throws IllegalStateException, IllegalArgumentException {
		// A check to ensure that we always access the same reference - singleton
		log.fine("FrameworkBootstrapService - getValue: " + Integer.toHexString(System.identityHashCode(this)));
		return this;
	}

	/**
	 * Add a module to the current list via the jboss-admin.sh kickstart it without
	 * requiring an AS restart.
	 * 
	 */
	public void addCustomModule(MyModule myModule, OperationContext context) {
		log.info("FrameworkBootstrapService - addCustomModule: " + myModule.getIdentifier().getName());

		// Set the current thread class loader to the original class loader
		// in order for the ServiceLoader to be propagated properly down the
		// module tree.
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

		try {
			/** start the newly added modules */
			startModule(myModule);
			/** Otherwise trigger a rollback */
		} catch (StartException e) {
			context.setRollbackOnly();
			e.printStackTrace();
		} catch (ModuleLoadException e) {
			context.setRollbackOnly();
			e.printStackTrace();
		}

		// restore back the class loader
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
	}

	/*
	 * Remove a module from the current list via the jboss-admin.sh by stopping it
	 * first
	 * 
	 */
	public void removeMyModule(MyModule myModule, OperationContext context) {
		ModuleIdentifier moduleIdentifier = myModule.getIdentifier();

		// get the current list of services that are loaded
		List<MyModuleIfc> listOfServices = myModules.get(moduleIdentifier);

		// stop the services associated with the modules
		Iterator<MyModuleIfc> services = listOfServices.iterator();
		while (services.hasNext()) {
			MyModuleIfc currentService = services.next();
			currentService.stop(null);
		}

		// remove the module from the list
		myModules.remove(moduleIdentifier);
	}

	/**
	 * Get a list of Modules that were set in the XML config file
	 */
	public List<MyModule> getMyModules() throws IllegalStateException, IllegalArgumentException {
		return this.injectedSubsystemState.getValue().getModules();
	}

	/**
	 * Start a Module
	 */
	private void startModule(MyModule myModule) throws StartException, ModuleLoadException {
		long beginModule = System.currentTimeMillis();

		// Already added to the Subsystem state - kickstart the module
		ModuleIdentifier moduleIdentifier = myModule.getIdentifier();
		List<MyModuleIfc> existingServices = myModules.get(moduleIdentifier);
		if (existingServices == null)
			existingServices = new ArrayList<MyModuleIfc>();

		// Load the module via the jboss-modules service
		Module module = Module.getCallerModuleLoader().loadModule(moduleIdentifier);

		ServiceLoader<?> serviceLoader = module.loadService(MyModuleIfc.class);
		Iterator<?> serviceIter = serviceLoader.iterator();
		while (serviceIter.hasNext()) {
			MyModuleIfc myService = (MyModuleIfc) serviceIter.next();
			existingServices.add(myService);

			long beginService = System.currentTimeMillis();

			myService.start(startupContext);

			long endService = System.currentTimeMillis();

			log.fine("FrameworkBootstrapService - service: " + myService.getName() + " started in: "
					+ (endService - beginService));
		}

		long endModule = System.currentTimeMillis();
		log.fine("FrameworkBootstrapService - module: " + moduleIdentifier.getName() + " started in: "
				+ (endModule - beginModule));

		/** Add new module only if the full list of services have been loaded ok */
		myModules.put(moduleIdentifier, existingServices);

		return;
	}
}
