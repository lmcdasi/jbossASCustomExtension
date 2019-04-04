For EAP6:

   -- create ${JBOSS_HOME}/modules/system/layers/base/com/example/jboss/module/extension/main
   		Note: no versioning in this sample
   		
   -- copy the module.xml & the jar file of this project into the above directory 
   
   -- overwrite the standalone.xml with the sample standalone-ha.xml in order to activate the custom service(s). 
   
In this way all EAR/WAR's that are deployed in jboss will have access to this custom service(s). The advantage is that the service is started & ready before the EAR/WAR are deployed. Then the services can be maintained by versioning and on the fly once jboss started.

Note: 

-- This example is set to test two 'services/modules'. The code can start one or multiples services - tough when multiple is used OO is kind of lost ...

-- The standalone.xml file contains the config setup of the new custom extension & the modules (services) that this extension will start/stop

        <subsystem xmlns="urn:com.example.jboss.module.extension:1.0">
            <modules>
                <module identifier="com.example.jboss.module"/>
            </modules>
        </subsystem>
        
-- The module.xml defines the modules this extension requires.

        <module name="com.example.jboss.module.api"/>
        <module name="com.example.jboss.module" services="export"/>

   The services 'export' keyword is required - this will make the new service to be visible.
        