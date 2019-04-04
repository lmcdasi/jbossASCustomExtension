package com.example.jboss.module.extension;

import static org.jboss.as.controller.parsing.ParseUtils.missingRequired;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoAttributes;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoContent;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoNamespaceAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.common.CommonDescriptions;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import com.example.jboss.module.extension.SubsystemState.MyModule;

public class ModuleExtension implements Extension {

    private static final Logger log = Logger.getLogger(ModuleExtension.class.getName());

    public static final String SUBSYSTEM_NAME = "mymodule";

    /* Add major/minor version with EAP6.0 GA */
    private static final int MANAGEMENT_API_MAJOR_VERSION = 1;
    private static final int MANAGEMENT_API_MINOR_VERSION = 0;

    private static final ModuleSubsystemParser parser = new ModuleSubsystemParser();

    /** {@inheritDoc} */
    public void initialize(final ExtensionContext context) {
        log.info("ModuleExtension --> Activating extension");
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, MANAGEMENT_API_MAJOR_VERSION, MANAGEMENT_API_MINOR_VERSION);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(SubsystemProviders.SUBSYSTEM);
        registration.registerOperationHandler(ModelDescriptionConstants.ADD, SubsystemAdd.INSTANCE, SubsystemAdd.INSTANCE, false);
        registration.registerOperationHandler(ModelDescriptionConstants.DESCRIBE, ModuleSubsystemDescribeHandler.INSTANCE, ModuleSubsystemDescribeHandler.INSTANCE, false, OperationEntry.EntryType.PRIVATE);
        
        //TODO: Configuration - add module conf
        //TODO: Properties - add module props
        
        // Pre loaded modules
        log.info("ModuleExtension --> Activating the preloaded modules");
        ManagementResourceRegistration modules = registration.registerSubModel(PathElement.pathElement(CommonAttributes.MODULE), SubsystemProviders.MY_MODULE_RESOURCE);
        modules.registerOperationHandler(ModelDescriptionConstants.ADD, ModuleAdd.INSTANCE, ModuleAdd.INSTANCE, false);
        modules.registerOperationHandler(ModelDescriptionConstants.REMOVE, ModuleRemove.INSTANCE, ModuleRemove.INSTANCE, false);

        // Registers the XML CUSTOM MODULE parsing class within AS7
        subsystem.registerXMLElementWriter(parser);        
    }

    /** {@inheritDoc} */
    public void initializeParsers(final ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(ModuleExtension.SUBSYSTEM_NAME,Namespace.CURRENT.getUriString(), parser);
    }

    /** Parser class - implements the read/write as7 XML ifc's */
    static class ModuleSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

        /** {@inheritDoc} */
        public void readElement(final XMLExtendedStreamReader reader, final List<ModelNode> list) throws XMLStreamException {
        	log.info("ModuleSubsystemParser --> readElement");
        	final ModelNode address = new ModelNode();	// represents the custom module model - root level.
            address.add(ModelDescriptionConstants.SUBSYSTEM, SUBSYSTEM_NAME);
            address.protect();
        	
        	final ModelNode addSubsystemOp = new ModelNode();
            addSubsystemOp.get(ModelDescriptionConstants.OP).set(ModelDescriptionConstants.ADD);
            addSubsystemOp.get(ModelDescriptionConstants.OP_ADDR).set(address);
            
            // at this stage one can set the activation from the xml file - in our case is EAGER.
            //parseActivationAttribute(reader, addSubsystemOp);
            
            list.add(addSubsystemOp);
            
            // Add all modules defines into standalone.xml file. They will be part of
            // the subsytem state within the TCCL service
            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                switch (Namespace.forUri(reader.getNamespaceURI())) {
                    case MODULE_1_0: {
                        final Element element = Element.forName(reader.getLocalName());
                        switch (element) {
                            case MODULES: {
                                list.addAll(parseModulesElement(reader, address));
                                break;
                            }
                            default:
                                throw unexpectedElement(reader);
                        }
                        break;
                    }
                    default:
                        throw unexpectedElement(reader);
                }
            }
        }
        
        /** Parse the module tag in order to build the defined modules */
        List<ModelNode> parseModulesElement(XMLExtendedStreamReader reader, ModelNode address) throws XMLStreamException {
        	log.info("ModuleSubsystemParser --> parseModulesElement");
        	
        	List<ModelNode> nodes = new ArrayList<ModelNode>();
            // Handle attributes
            requireNoAttributes(reader);

            // Handle elements
            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                switch (Namespace.forUri(reader.getNamespaceURI())) {
                    case MODULE_1_0: {
                        final Element element = Element.forName(reader.getLocalName());
                        if (element == Element.MODULE) {
                            String identifier = null;
                            String start = null;
                            final int count = reader.getAttributeCount();
                            for (int i = 0; i < count; i++) {
                                requireNoNamespaceAttribute(reader, i);
                                final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
                                switch (attribute) {
                                    case IDENTIFIER: {
                                        identifier = reader.getAttributeValue(i);
                                        break;
                                    }
                                    case STARTLEVEL: {
                                        start = reader.getAttributeValue(i);
                                        break;
                                    }
                                    default:
                                        throw unexpectedAttribute(reader, i);
                                }
                            }
                            if (identifier == null)
                                throw missingRequired(reader, Collections.singleton(Attribute.IDENTIFIER));
                            
                            ModelNode moduleNode = new ModelNode();
                            moduleNode.get(ModelDescriptionConstants.OP).set(ModelDescriptionConstants.ADD);
                            moduleNode.get(ModelDescriptionConstants.OP_ADDR).set(address).add(CommonAttributes.MODULE, identifier);
 
                            // Due to OperationHandler issues at boot time - set the modules when reading
                            // the xml file rather than doing at boot time -> runtime
                            final Integer startLevel = (start != null ? new Integer(start) : null);
                            MyModule currentModule = new MyModule(ModuleIdentifier.fromString(identifier), startLevel);
                            SubsystemState.INSTANCE.addModule(currentModule);
                            log.info("ModuleSubsystemParser --> Added module: " + currentModule.getIdentifier().getName());
                            
                            if (start != null) {
                            	moduleNode.get(CommonAttributes.STARTLEVEL).set(start);
                            }
                            
                            nodes.add(moduleNode);

                            requireNoContent(reader);
                        } else {
                            throw unexpectedElement(reader);
                        }
                        break;
                    }
                    default:
                        throw unexpectedElement(reader);
                }
            }

            return nodes;
        }

        /** {@inheritDoc} */
        public void writeContent(final XMLExtendedStreamWriter streamWriter, final SubsystemMarshallingContext context) throws XMLStreamException {
        	log.info("ModuleSubsystemParser --> writeContent");
        	
        	context.startSubsystemElement(Namespace.CURRENT.getUriString(), false);
            ModelNode node = context.getModelNode();
            
            if (has(node, CommonAttributes.MODULE)) {
                ModelNode modules = node.get(CommonAttributes.MODULE);
                streamWriter.writeStartElement(Element.MODULES.getLocalName());
                Set<String> keys = modules.keys();
                for (String current : keys) {
                    ModelNode currentModule = modules.get(current);
                    streamWriter.writeEmptyElement(Element.MODULE.getLocalName());
                    streamWriter.writeAttribute(Attribute.IDENTIFIER.getLocalName(), current);
                    if (has(currentModule, CommonAttributes.STARTLEVEL)) {
                        writeAttribute(streamWriter, Attribute.STARTLEVEL, currentModule.require(CommonAttributes.STARTLEVEL));
                    }
                }
                streamWriter.writeEndElement();
            }
            
            streamWriter.writeEndElement();
        }
        
        private boolean has(ModelNode node, String name) {
            return node.has(name) && node.get(name).isDefined();
        }
        
        private void writeAttribute(final XMLExtendedStreamWriter writer, final Attribute attr, final ModelNode value) throws XMLStreamException {
            writer.writeAttribute(attr.getLocalName(), value.asString());
        }
    }

    /** 
     * Handles the module subsystem model definition 
     */
    private static class ModuleSubsystemDescribeHandler implements OperationStepHandler, DescriptionProvider {
        static final ModuleSubsystemDescribeHandler INSTANCE = new ModuleSubsystemDescribeHandler();

        public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
            final ModelNode model = context.readResource(PathAddress.EMPTY_ADDRESS).getModel();

            PathAddress rootAddress = PathAddress.pathAddress(PathAddress.pathAddress(operation.require(ModelDescriptionConstants.OP_ADDR)).getLastElement());

            final ModelNode subsystem = new ModelNode();
            subsystem.get(ModelDescriptionConstants.OP).set(ModelDescriptionConstants.ADD);
            subsystem.get(ModelDescriptionConstants.OP_ADDR).set(rootAddress.toModelNode());
            
            // support EAGER activation
            //if (model.has(CommonAttributes.ACTIVATION)) {
            //    subsystem.get(CommonAttributes.ACTIVATION).set(model.get(CommonAttributes.ACTIVATION));
            //}
            
            // Add subsystem
            ModelNode result = context.getResult();
            result.add(subsystem);
            
            if (model.has(CommonAttributes.MODULE)) {
                for (Property prop : model.get(CommonAttributes.MODULE).asPropertyList()) {
                    ModelNode address = rootAddress.toModelNode();
                    address.add(CommonAttributes.MODULE, prop.getName());
                    // Add modules defined in the XML file in the CUSTOM MODULE subsystem
                    result.add(ModuleAdd.getAddOperation(address, prop.getValue()));
                }
            }
            
            context.stepCompleted();
        }

        /** Provides a description of the module subsystem model */
        public ModelNode getModelDescription(Locale locale) {
            return CommonDescriptions.getSubsystemDescribeOperation(locale);
        }
    }
}
