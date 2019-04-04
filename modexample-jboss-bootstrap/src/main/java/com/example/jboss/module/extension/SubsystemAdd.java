package com.example.jboss.module.extension;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import java.util.logging.Logger;

import com.example.jboss.module.extension.SubsystemState.Activation;
import com.example.jboss.module.extension.processors.ModuleDependencyProcessor;
import com.example.jboss.module.extension.services.FrameworkBootstrapService;

public class SubsystemAdd extends AbstractBoottimeAddStepHandler implements DescriptionProvider {
	private static Logger log = Logger.getLogger(SubsystemAdd.class.getName());

    static final SubsystemAdd INSTANCE = new SubsystemAdd();
    
    static final Activation DEFAULT_ACTIVATION = Activation.EAGER;
    
    private SubsystemAdd() {
    }

    protected void populateModel(ModelNode operation, ModelNode subModel) {
    	log.info("SubsystemAdd --> populateModel");
    	
        if (operation.has(CommonAttributes.MODULE)) {
            subModel.get(CommonAttributes.MODULE).set(operation.get(CommonAttributes.MODULE));
        }
    }

    protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) {
    	log.info("Activating CUSTOM Subsystem");
    	
    	context.addStep(new AbstractDeploymentChainStep() {
            protected void execute(DeploymentProcessorTarget processorTarget) {
            	// Add deployment dependency on the WAR/RAR/SAR applications
            	processorTarget.addDeploymentProcessor(ModuleExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, Phase.DEPENDENCIES_MODULE, new ModuleDependencyProcessor());            
            }
        }, OperationContext.Stage.RUNTIME);

    	ServiceTarget serviceTarget = context.getServiceTarget();
    	
    	// Add the Subsystem State service - it will hold CUSTOM MODULE subsystem config.
    	newControllers.add(SubsystemState.addService(serviceTarget));
        
        // Add the CUSTOM service - all modules will be handled via the CUSTOM service
    	newControllers.add(FrameworkBootstrapService.addService(serviceTarget, verificationHandler));
    }
    
    protected boolean requiresRuntimeVerification() {
        return false;
    }
    
    protected static void addModelProperties(ResourceBundle bundle, ModelNode node, String propType) {
    	log.info("SubsystemAdd --> addModelProperties");
    	
        node.get(propType, CommonAttributes.ACTIVATION, ModelDescriptionConstants.DESCRIPTION)
            .set(bundle.getString("activation"));
        node.get(propType, CommonAttributes.ACTIVATION, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        node.get(propType, CommonAttributes.ACTIVATION, ModelDescriptionConstants.DEFAULT)
            .set(DEFAULT_ACTIVATION.toString());
    }
    
    /*
     * (non-Javadoc)
     * @see org.jboss.as.controller.descriptions.DescriptionProvider#getModelDescription(java.util.Locale)
     */
    public ModelNode getModelDescription(Locale locale) {
        ResourceBundle resourceBundle = SubsystemProviders.getResourceBundle(locale);

        ModelNode node = new ModelNode();
        node.get(ModelDescriptionConstants.OPERATION_NAME).set(ModelDescriptionConstants.ADD);
        node.get(ModelDescriptionConstants.DESCRIPTION).set(resourceBundle.getString(ModuleExtension.SUBSYSTEM_NAME + ".add"));
        addModelProperties(resourceBundle, node, ModelDescriptionConstants.REQUEST_PROPERTIES);
        node.get(ModelDescriptionConstants.REPLY_PROPERTIES).setEmptyObject();
        return node;
    }
}
