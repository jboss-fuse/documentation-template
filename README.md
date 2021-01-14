This project contains maven plugin with documentation templates which can be used to simplify creation of README file in fabric8 quickstarts

# Documentation template

Main template files are `karaf-quickstart-template.adoc` and `springboot-quickstart-template.adoc`, which are the templates for the whole README file. 

Templates are created and evaluated via [Freemarker](https://freemarker.apache.org/). 

You can customize the final README.adoc by providing several child pages (only 1 of them is mandatory) in location `src/main/doc`:
 - `integration-testing.adoc` In case that quickstart does not have integration test, please keep blank.
 - `introduction.adoc` Contains title and brief description of the quickstart. **Mandatory**.
 - `introduction-other.adoc` Some quickstarts requires a configuration of another system. This the place to provide a guide.
 - `local-validation.adoc` Another system has to be defined also for local execution.
 - `oc-deploy.adoc` Step with the command to start quickstart - could be different. 
 - `oc-deploy-without-images.adoc` Command for the execution in the scenario without preinstalled images. 
 - `oc-special-configuration.adoc` Place to define possible confguration of another system using `oc`.
 - `validation.adoc` Validation of successful execution of the quickstart.
 - `validation-local.adoc` Validation in local scenario could be different.
 - `validation-see-log.adoc` Some quickstarts are validating messages in log, some are using different approach.
 - `validation-summary.adoc` In some cases, validation contain also veryfication of different system.
 
 If folder `src/main/doc` does not exist, templating mechanism is not executed.  
 
 # Version alignment
 
 Plugin aligns versions of documentation links and fuse images.
 
 Version alignment is executed even if `src/main/doc` does not exist.
 
 # How to use
 
Prepare all required adoc files (required location is `src/main/doc`)
 
You can run maven plugin by following command:
> mvn io.fabric8.quickstarts:documentation-template:VERSION:updateDoc


See usage in existing quickstarts (e.g. https://github.com/fabric8-quickstarts/spring-boot-camel-rest-sql)

 # How to generate README.adoc in a quickstart
 
 Use profile `generate` to generate README.adoc file for specific project. Example of use:
 
 > mvn package -Dquickstart.name=spring-boot-camel -Pgenerate 

Parameter `quickstart.path` contains path to specific project.
Parameter `quickstart.name` contains the neme for the specific project.
(Documentation project parent directory has to be the same as the parent of quickstart directory)




 



