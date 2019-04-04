package com.example.jboss.module.extension.processors;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.filter.PathFilters;
import org.jboss.msc.service.ServiceController;

import com.example.jboss.module.extension.SubsystemState.MyModule;
import com.example.jboss.module.extension.services.FrameworkBootstrapService;

/**
 * 
 * Deployment processor which adds a module dependencies for modules needed for
 * deployments.
 *
 */
public class ModuleDependencyProcessor implements DeploymentUnitProcessor {
	private static Logger log = Logger.getLogger(ModuleDependencyProcessor.class.getName());

	/**
	 * Add dependencies for modules required for war or any other type of
	 * deployments
	 *
	 */
	public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
		final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
		final ModuleSpecification moduleSpecification = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);

		final ModuleLoader moduleLoader = Module.getBootModuleLoader();

		// Get the list of modules that need to be added to the deployment dependency
		ServiceController<?> myService = deploymentUnit.getServiceRegistry()
				.getService(FrameworkBootstrapService.SERVICE_NAME);
		FrameworkBootstrapService fwkBootstrap = (FrameworkBootstrapService) myService.getValue();

		List<MyModule> myModuleList = fwkBootstrap.getMyModules();
		Iterator<MyModule> myModuleIter = myModuleList.iterator();
		/* For every module defined add a dependency to the deployed application */
		while (myModuleIter.hasNext()) {
			MyModule myModule = myModuleIter.next();
			ModuleIdentifier mModuleIdentifier = myModule.getIdentifier();

			log.info("ModulDependencyProcessor -- > addDependency: " + mModuleIdentifier.getName());

			ModuleDependency dep = new ModuleDependency(moduleLoader, mModuleIdentifier, false, false, false, true);
			dep.addImportFilter(PathFilters.getMetaInfFilter(), true);
			dep.addExportFilter(PathFilters.getMetaInfFilter(), true);
			moduleSpecification.addSystemDependency(dep);
		}
	}

	/**
	 * 
	 * Remove dependencies for modules required for war or any other type of
	 * deployments
	 *
	 */
	public void undeploy(DeploymentUnit context) {

	}
}
