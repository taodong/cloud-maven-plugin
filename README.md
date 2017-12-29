# cloud-maven-plugin
Maven plugin helping on building infrastructure in cloud use cloud build tools: Ansible, Packer, Terraform or Terragrunt. It mainly works on AWS. It should work on other cloud environment though untested.

This plugin contains six executors. Four major executors (ansible, packer, terraform and terragrunt), each runs cloud build tool respectively and is exclusive to the others. Executor "variables" allows user to pass variables into plugin. The last Executor "assemble" is used to assemble file structure. 

## Requirement
* Linux or Mac system only
* Java 8 or later required
* Maven required
* Cloud environment ready in local. For AWS, you need to have awscli installed and configured.
* Python 2.7 and up required
* Pip 2.7 and up required
* Ansible local installation is required if you need to run packer or ansible executor

## Installation
Download source code and run "mvn install" locally. I'll release it to Maven Central repository when the code is stable.

After installation, you can add cloud-maven-plugin configuration into the pom.xml of your project. By default it will create script build.sh under your target folder if you run command "mvn install". The build.sh will contains commands to build your infrastructure in the cloud. The following is a sample usage in pom.xml.
```xml
<build>
       <plugins>
           ...
           <plugin>
               <groupId>com.github.taodong</groupId>
               <artifactId>cloud-maven-plugin</artifactId>
               <version>0.1-SNAPSHOT</version>
               <configuration>
                   <envToolDir>sandbox</envToolDir>
               </configuration>
               <executions>
                   <execution>
                       <id>variables</id>
                       <goals>
                           <goal>variables</goal>
                       </goals>
                   </execution>
                   <execution>
                       <id>assemble</id>
                       <goals>
                           <goal>assemble</goal>
                       </goals>
                   </execution>
                   <execution>
                       <id>packer</id>
                       <goals>
                           <goal>packer</goal>
                       </goals>
                       <configuration>
                           <version>0.12.2</version>
                           <system>Mac</system>
                           <format>amd64</format>
                           <configFiles>
                               <param>config.json</param>
                           </configFiles>
                       </configuration>
                   </execution>
                   <execution>
                       <id>terragrunt</id>
                       <goals>
                           <goal>terragrunt</goal>
                       </goals>
                       <configuration>
                           <version>0.11.0</version>
                           <system>Mac</system>
                           <format>amd64</format>
                       </configuration>
                   </execution>
               </executions>
           </plugin>
       </plugins>
       ...
</build>
```
## Usage
- [Configure plug-in](#Configure plug-in)
- [Import custom variables](#Import custom variables)
### Configure plug-in
There are two required parameters need to be defined either in configuration block or passed through mvn command line. They are envToolDir and cloudExe.
```xml
<configuration>
   <envToolDir>sandbox</envToolDir>
   <cloudExe>packer</cloudExe>
</configuration>
```
All supported parameters are listed below:
- envToolDir (**required**, maven command line variable: cloud.envToolDir): folder to store any file or tool needed for this plug in.
- cloudExe (**required**, maven command line variable: cloud.executor): executor name, allowed values include: ansible, packer, terraform and terragrunt.
- genScriptOnly (default: true, maven command line variable: cloud.genScriptOnly): By default this parameter is set to true which will force plug-in to create build.sh for users to verify and run it themselves. When set to false, instead of create build.sh, maven will build cloud directly. **_due to limitation of JVM, any interactive steps during build will cause a failure if this variable is set to false_**
- buildFolder (maven command line variable: cloud.buildFolder):
- scriptHead (maven command line variable: cloud.scriptHead):
- scriptTail (maven command line variable: cloud.scriptTail):
### Import custom variables
TODO: ...
## License
MIT
