package com.example.jboss.module.extension;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.registry.Resource;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.modules.ModuleIdentifier;

import com.example.jboss.module.extension.SubsystemState.MyModule;
import com.example.jboss.module.extension.services.FrameworkBootstrapService;

public class ModuleAdd implements OperationStepHandler, DescriptionProvider {
	static final ModuleAdd INSTANCE = new ModuleAdd();

	private static final Logger log = Logger.getLogger(ModuleAdd.class.getName());

	private ModuleAdd() {
	}

	public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
		log.info("ModuleAdd --> execute add");

		Resource resource = context.createResource(PathAddress.EMPTY_ADDRESS);
		ModelNode model = resource.getModel();

		ModelNode slNode = null;
		if (operation.has(CommonAttributes.STARTLEVEL)) {
			slNode = operation.get(CommonAttributes.STARTLEVEL);
			model.get(CommonAttributes.STARTLEVEL).set(slNode);
		}

		final Integer startLevel = (slNode != null ? slNode.asInt() : null);

		if (context.getProcessType().isServer()) {
			context.addStep(new OperationStepHandler() {
				public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
					String identifier = operation.get(ModelDescriptionConstants.OP_ADDR).asObject()
							.get(CommonAttributes.MODULE).asString();
					MyModule module = new MyModule(ModuleIdentifier.fromString(identifier), startLevel);
					SubsystemState stateService = (SubsystemState) context.getServiceRegistry(true)
							.getRequiredService(SubsystemState.SERVICE_NAME).getValue();

					/*
					 * Add & Start the module only if it is not there already
					 */
					if (!stateService.isModuleExisting(identifier)) {
						stateService.addModule(module);

						/*
						 * warning - this portion of code will be called very early by the AS in order
						 * to setup the SubsystemState and at that time the FrameworkBootstrapService is
						 * not initialized so no add is allowed until framework is started.
						 */
						FrameworkBootstrapService fwkBootstrapService = (FrameworkBootstrapService) context
								.getServiceRegistry(true).getRequiredService(FrameworkBootstrapService.SERVICE_NAME)
								.getValue();
						if (fwkBootstrapService.isFrameworkBootratpServiceStarted()) {
							fwkBootstrapService.addCustomModule(module, context);
						}
					}

					if (context.completeStep() == OperationContext.ResultAction.ROLLBACK) {
						stateService.removeModule(identifier);
					}
				}
			}, OperationContext.Stage.RUNTIME);
		}
		context.stepCompleted();
	}

	public ModelNode getModelDescription(Locale locale) {
		log.info("ModuleAdd --> getModelDescription");

		ResourceBundle resourceBundle = SubsystemProviders.getResourceBundle(locale);

		ModelNode node = new ModelNode();
		node.get(ModelDescriptionConstants.OPERATION_NAME).set(ModelDescriptionConstants.ADD);
		node.get(ModelDescriptionConstants.DESCRIPTION).set(resourceBundle.getString("module.add"));
		addModelProperties(resourceBundle, node, ModelDescriptionConstants.REQUEST_PROPERTIES);
		node.get(ModelDescriptionConstants.REPLY_PROPERTIES).setEmptyObject();
		return node;
	}

	static void addModelProperties(ResourceBundle bundle, ModelNode node, String propType) {
		log.info("ModuleAdd --> addModelProperties");

		node.get(propType, CommonAttributes.STARTLEVEL, ModelDescriptionConstants.DESCRIPTION)
				.set(bundle.getString("module.startlevel"));
		node.get(propType, CommonAttributes.STARTLEVEL, ModelDescriptionConstants.TYPE).set(ModelType.INT);
		node.get(propType, CommonAttributes.STARTLEVEL, ModelDescriptionConstants.REQUIRED).set(false);
	}

	/**
	 * Create an "add" operation using the existing model
	 */
	static ModelNode getAddOperation(ModelNode address, ModelNode existing) {
		log.info("ModuleAdd --> getAddOperation");

		ModelNode op = Util.getEmptyOperation(ModelDescriptionConstants.ADD, address);
		if (existing.hasDefined(CommonAttributes.STARTLEVEL)) {
			op.get(CommonAttributes.STARTLEVEL).set(existing.get(CommonAttributes.STARTLEVEL));
		}

		return op;
	}
}
