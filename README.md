This project prepares template which can be used to simplify creation of README file in fabric8 quickstarts

# Documentation template

Main template file is `README-template.adoc`.

This file contains text for the whole README file. 

To make it customizable by various quickstart, it uses properties:
 - `quickstart.name` name of the quickstart (e.g. spring-boot-camel-xml)
 
 and it contains several child pages, which are included into mainn README:
 - `introduction.adoc` Contains title and brief description of the quickstart. Should be used.
 - `oc-special-configuration.adoc` In case that quickstart requires special configuration.
 - `oc-deploy.adoc` The special configuration from the previous step could need different deploy command.
 - `validation.adoc` Once quickstart is running, there could be different method to validate its state.
 - `local-validation.adoc` This method could vary for local execution.
 - `integration-testing.adoc` In case that quickstart does not have integration test, please keep blank.
 
 # How to use
 
 Prepare custom child adoc files (e.g. into src/main/doc)  and define property quickstart.name:
 ```xml
         <quickstart.name>spring-boot-camel-xml</quickstart.name>
```
 
This template has to be added as zip dependency:
 
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

See usage in existing quicckstarts (e.g. https://github.com/fabric8-quickstarts/spring-boot-camel-rest-sql)
 




 



