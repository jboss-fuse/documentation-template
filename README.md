This project prepares template which can be used to simplify creation of README file in fabric8 quickstarts

# Documentation template

Main template file is `README-template.adoc`, which contains text for the whole README file. 

You can customize the final README.adoc by providing several child pages (only 1 of them is mandatory):
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
 
 Please start these children adoc files with a blank line. (This will make chapter separation in a result page correct.)
 
 # How to use
 
Prepare all required adoc files (suggested location is `src/main/doc`)
 
This artifact has to be added among dependencies as a zip type:
 
 ```xml
        <dependency>
            <groupId>io.fabric8.quickstarts</groupId>
            <artifactId>documentation-template</artifactId>
            <version>1.0-SNAPSHOT</version>
            <type>zip</type>
        </dependency>
 ```

Use assembly plugin to extract the dependency (into target/doc/template):

```xml
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
                <executions>
                    <execution>
                        <id>unpack-doc-template</id>
                        <phase>package</phase>
                        <goals>
                            <goal>unpack-dependencies</goal>
                        </goals>
                        <configuration>
                            <includeGroupIds>io.fabric8.quickstarts</includeGroupIds>
                            <includeArtifactIds>documentation-template</includeArtifactIds>
                            <outputDirectory>
                                ${project.build.directory}/doc/template
                            </outputDirectory>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
```

Use resource plugin to add custom child adoc files and to resolve properties (into target/doc):

```xml
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-resources-plugin</artifactId>
                <executions>
                    <execution>
                        <id>copy-doc-template-to-target</id>
                        <phase>package</phase>
                        <goals>
                            <goal>copy-resources</goal>
                        </goals>
                        <configuration>
                            <outputDirectory>${basedir}/target/doc</outputDirectory>
                            <resources>
                                <resource>
                                    <directory>${project.build.directory}/doc/template</directory>
                                    <filtering>true</filtering>
                                </resource>
                            </resources>
                            <delimiters>
                                <delimiter>${*}</delimiter>
                            </delimiters>
                        </configuration>
                    </execution>
                    <execution>
                        <id>copy-doc-to-target</id>
                        <phase>package</phase>
                        <goals>
                            <goal>copy-resources</goal>
                        </goals>
                        <configuration>
                            <outputDirectory>${basedir}/target/doc/spring-boot</outputDirectory>
                            <overwrite>true</overwrite>
                            <resources>
                                <resource>
                                    <directory>src/main/doc</directory>
                                </resource>
                            </resources>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
``` 

Finally, template has to be resolved into final file:
```xml
            <plugin>
                <groupId>io.whelk.asciidoc</groupId>
                <artifactId>asciidoc-template-maven-plugin</artifactId>
                <version>${asciidoc.plugin.version}</version>
                <configuration>
                    <templateDirectory>${basedir}/target/doc/spring-boot</templateDirectory>
                    <templateFile>README-template.adoc</templateFile>
                    <outputDirectory>./</outputDirectory>
                    <outputFile>README.adoc</outputFile>
                </configuration>
                <executions>
                    <execution>
                        <id>generate-README.adoc</id>
                        <phase>package</phase>
                        <goals>
                            <goal>build</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```

See usage in existing quickstarts (e.g. https://github.com/fabric8-quickstarts/spring-boot-camel-rest-sql)
 




 



