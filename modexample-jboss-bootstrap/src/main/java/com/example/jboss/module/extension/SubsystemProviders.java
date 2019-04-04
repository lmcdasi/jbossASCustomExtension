package com.example.jboss.module.extension;

/**
 * 
 * Provides operations to read/construct the CUSTOM MODULE subsystem
 * 
 */

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;

public class SubsystemProviders {
	private static Logger log = Logger.getLogger(SubsystemProviders.class.getName());

	/*
	 * Location of the subsystem description that will appear when running the cli
	 */
    static final String RESOURCE_NAME = SubsystemProviders.class.getPackage().getName() + ".LocalDescriptions";

    /** Description of the module subsystem */
    static final DescriptionProvider SUBSYSTEM = new DescriptionProvider() {
        public ModelNode getModelDescription(final Locale locale) {
            return getRootResource(locale);
        }
    };

    /** Description of My module */
    static final DescriptionProvider MY_MODULE_RESOURCE = new DescriptionProvider() {
        public ModelNode getModelDescription(Locale locale) {
        	log.info("SubsystemProviders --> DescriptionProvider: getModelDescription");
            return getModule(locale);
        }
    };

    /** Get's the root resource of the module subsystem */
    static ModelNode getRootResource(Locale locale) {
    	log.info("SubsystemProviders --> getRootResource");
    	
        ResourceBundle bundle = SubsystemProviders.getResourceBundle(locale);

        ModelNode node = new ModelNode();
        node.get(ModelDescriptionConstants.DESCRIPTION).set(bundle.getString(ModuleExtension.SUBSYSTEM_NAME));
        SubsystemAdd.addModelProperties(bundle, node, ModelDescriptionConstants.ATTRIBUTES);

        node.get(ModelDescriptionConstants.CHILDREN, CommonAttributes.MODULE).set(getModule(locale));
        return node;
    }

    /** Returns the module description */
    private static ModelNode getModule(Locale locale) {
    	log.info("SubsystemProviders --> getModule");
    	
        ResourceBundle bundle = SubsystemProviders.getResourceBundle(locale);

        final ModelNode node = new ModelNode();
        node.get(ModelDescriptionConstants.DESCRIPTION).set(bundle.getString("module"));
        ModuleAdd.addModelProperties(bundle, node, ModelDescriptionConstants.ATTRIBUTES);

        return node;
    }

    static ResourceBundle getResourceBundle(Locale locale) {
    	log.info("SubsystemProviders --> getResourceBundle");
    	
        if (locale == null) {
            locale = Locale.getDefault();
        }
        
        return ResourceBundle.getBundle(RESOURCE_NAME, locale);
    }
}
