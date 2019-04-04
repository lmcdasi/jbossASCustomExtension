package com.example.jboss.module.extension;

import java.util.Locale;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;

import com.example.jboss.module.extension.SubsystemState.MyModule;
import com.example.jboss.module.extension.services.FrameworkBootstrapService;

public class ModuleRemove extends AbstractRemoveStepHandler implements DescriptionProvider {

    static final ModuleRemove INSTANCE = new ModuleRemove();

    private ModuleRemove() {
    }

    public ModelNode getModelDescription(Locale locale) {
        ModelNode node = new ModelNode();
        node.get(ModelDescriptionConstants.OPERATION_NAME).set(ModelDescriptionConstants.REMOVE);
        node.get(ModelDescriptionConstants.DESCRIPTION).set(
            SubsystemProviders.getResourceBundle(locale).getString("module.remove"));
        node.get(ModelDescriptionConstants.REQUEST_PROPERTIES).setEmptyObject();
        node.get(ModelDescriptionConstants.REPLY_PROPERTIES).setEmptyObject();
        return node;
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        String identifier = operation.get(ModelDescriptionConstants.OP_ADDR).asObject().get(CommonAttributes.MODULE).asString();

        SubsystemState stateService = (SubsystemState) context.getServiceRegistry(true).getRequiredService(SubsystemState.SERVICE_NAME).getValue();
        MyModule prevModule = stateService.removeModule(identifier);
        
        /*
         * warning - this portion of code will be called very early by 
         * the as in order to setup the SubsystemState and at that time the
         * FrameworkBootstrapService is not initialised so no add is allowed
         * until framework is started.
         */
        FrameworkBootstrapService fwkBootstrapService = (FrameworkBootstrapService) context.getServiceRegistry(true).getRequiredService(FrameworkBootstrapService.SERVICE_NAME).getValue();
        if (fwkBootstrapService.isFrameworkBootratpServiceStarted()) {
        	fwkBootstrapService.removeMyModule(prevModule, context);
        }
        
        if (context.completeStep() == OperationContext.ResultAction.ROLLBACK) {
            stateService.addModule(prevModule);
        }
    }
}
