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
- [Configure plug-in](#config-plug-in)
- [Assemble project](#assemble-project)
- [Run Ansible scripts](#run-ansible-scripts)
- [Run Packer scripts](#run-packer-scripts)
- [Run Terraform scripts](#run-terraform-scripts)
- [Run Terragrunt scripts](#run-terragrunt-scripts)
- [Import custom variables](#import-custom-variables)
### Configure plug-in
There are two required parameters need to be defined either in configuration block or passed through mvn command line. They are envToolDir and cloudExe.
_Sample Configuration:_
```xml
<configuration>
   <envToolDir>sandbox</envToolDir>
   <cloudExe>packer</cloudExe>
</configuration>
```
All supported parameters include:
- envToolDir (**required**, maven command line variable: cloud.envToolDir): folder to store any file or tool needed for this plug in.
- cloudExe (**required**, maven command line variable: cloud.executor): executor name, allowed values include: ansible, packer, terraform and terragrunt.
- genScriptOnly (default: true, maven command line variable: cloud.genScriptOnly): By default this parameter is set to true which will force plug-in to create build.sh for users to verify and run it themselves. When set to false, instead of create build.sh, maven will build cloud directly. **_due to limitation of JVM, any interactive steps during build will cause a failure if this variable is set to false_**
- buildFolder (default: target, maven command line variable: cloud.buildFolder): a folder with given name will be created under target as output folder. Use target if this parameter has no value.
- scriptHead (default: null, maven command line variable: cloud.scriptHead): used only when genScriptOnly is true. The content of this file will be inserted into build.sh first.
- scriptTail (default: null, maven command line variable: cloud.scriptTail): used only when genScriptOnly is true. The content of this file will be inserted into build.sh at the end.
### Assemble project
Executor assemble is used to assemble output files. It's able to copy files from different folders into one single output folder under target. A typical usage is to pick several Ansible roles from your shared folder and add them into your output folder.
The following example will copy all files those file extension isn't .md under src/sample, src/shared/roles/java and src/shared/roles/python into target folder. 
_Sample Configuration:_
```xml
<execution>
   <id>assemble</id>
   <goals>
    <goal>assemble</goal>
   </goals>
   <configuration>
    <cloudRoot>src/sample</cloudRoot>
        <extra>
            <param>src/shared/roles/java</param>
            <param>src/shared/roles/python</param>
        </extra>
        <ignoreFiles>
            <param>*.md</param>
        </ignoreFiles>
    </configuration>
</execution>
```
All supported parameters include:
- cloudRoot (default: src, maven command line variable: cloud.assemble.root): main folder which source files are stored and need to be copied into output folder.
- extra (default: null, maven command line variable: cloud.assemble.extra): list of folders which need to be copied into output folder.
- ignoreFiles (default: null, maven command line variable: cloud.assemble.ignoreFiles): files to be ignored when copied. It's able to ignore three types of files: file name starts with a given prefix (e.g. readme*), file name ends with suffix (e.g. *.md) and exact file name without any wild card (readme.md).
### Run Ansible scripts
Executor ansible is use to build cloud infrastructure using Ansible. It will run ansible installed in local machine when cloud.executor is set to ansible.
_Sample Configuration:_
```xml
<execution>
   <id>ansible</id>
   <goals>
       <goal>ansible</goal>
   </goals>
   <configuration>
       <exec>ansible-playbook</exec>
       <playbookFile>main.yml</playbookFile>
   </configuration>
</execution>
```
All supported parameters include:
- exec (default: ansible-playbook, maven command line variable: cloud.ansible.exe): which command to run using Ansible, the value can be either ansible-playbook or ansible.
- arguments (default: null, maven command line variable: cloud.ansible.arguments): command arguments to append after ansible/ansible-playbook command
- playbookFile (default: null, maven command line variable: cloud.ansible.playbookFile): used when cloud.ansible.exe is set to ansible-playbook. The plugin will run the any match files using first matched rule. Please refer [Misc](#misc) section for details of first matched rule.
- timeout (default: 900, maven command line variable: cloud.ansible.commandTimeOut): used when cloud.genScriptOnly is false. Timeout in seconds for maven build.
### Run Packer scripts
Executor packer is used to build cloud infrastructure using Packer. This executor will execute packer command only if cloud.executor is set to packer.
_Sample Configuration:_
```xml
<execution>
   <id>packer</id>
   <goals>
       <goal>packer</goal>
   </goals>
   <configuration>
       <version>SYSTEM</version>
       <configFiles>
           <param>config.json</param>
       </configFiles>
   </configuration>
</execution>
```
All supported parameters include:
- configFiles (**required**, maven command line variable: cloud.packer.configFiles): a list of JSON file names you want the packer to run. The file should be a JSON file without path. The plugin will try to find all available matches under output folder using first matached rule. 
- version (default: SYSTEM, maven command line variable: cloud.packer.version): version of packer to run. If the value is "SYSTEM", the plugin will use packer installed in local system. Otherwise, the plugin will try to find the packer executable in cloud.envToolDir folder which matches both system and version parameter. If no matched version found, the plugin will try to download it from Hashicorp website.
- system (default: null, maven command line variable: cloud.packer.system): used when cloud.packer.version is not "SYSTEM". It tells this plugin which operation system the packer should run. Available values include: Mac, FreeBSD, OpenBSD and Windows. 
- format (default: null, maven command line variable: cloud.packer.systemFormat): used when cloud.packer.version is not "SYSTEM". It tells this plugin the disk format of target system. Available values include: 386, amd64, arm and arm64.
- arguments (default: null, maven command line variable: cloud.packer.arguments): command arguments to append after packer command
- timeout (default: 900, maven command line variable: cloud.packer.commandTimeOut): used when cloud.genScriptOnly is false. Timeout in seconds for maven build.
### Run Terraform scripts
Executor terraform is used to build cloud infrastructure using Terraform. This executor will execute terraform command only if cloud.executor is set to terraform.
_Sample Configuration:_
```xml
<execution>
   <id>terragrunt</id>
   <goals>
       <goal>terraform</goal>
   </goals>
   <configuration>
       <version>SYSTEM</version>
       <arguments>apply</arguments>
   </configuration>
</execution>
```
All supported parameters include:
- version (default: SYSTEM, maven command line variable: cloud.terraform.version): version of terraform to run. If the value is "SYSTEM", the plugin will use terraform installed in local system. Otherwise, the plugin will try to find the terragrunt executable in cloud.envToolDir folder which matches both system and version parameter. If no matched version found, the plugin will try to download it from Hashicorp website.
- modules (default: null, maven command line variable: cloud.terraform.modules): if set value, instead of run terraform under target/output folder, this plugin will try to run terraform in any sub-folder with matches the name given by this parameter
- system (default: null, maven command line variable: cloud.terraform.system): used when cloud.terraform.version is not "SYSTEM". It tells this plugin which operation system the terragrunt should run. Available values include: Mac, FreeBSD, OpenBSD and Windows. 
- format (default: null, maven command line variable: cloud.terraform.systemFormat): used when cloud.terraform.version is not "SYSTEM". It tells this plugin the disk format of target system. Available values include: 386, amd64, arm and arm64.
- arguments (default: null, maven command line variable: cloud.terraform.arguments): command arguments to append after terraform command, such as plan, apply, or destroy etc.
- timeout (default: 900, maven command line variable: cloud.terraform.commandTimeOut): used when cloud.genScriptOnly is false. Timeout in seconds for maven build.
### Run Terragrunt scripts
Executor terragrunt is used to build cloud infrastructure using Terragrunt. This executor will execute terragrunt command only if cloud.executor is set to terraform.
_Sample Configuration:_
```xml
<execution>
   <id>terragrunt</id>
   <goals>
       <goal>terragrunt</goal>
   </goals>
   <configuration>
       <version>SYSTEM</version>
       <arguments>apply</arguments>
   </configuration>
</execution>
```
All supported parameters include:
- version (default: SYSTEM, maven command line variable: cloud.terragrunt.version): version of terragrunt to run. If the value is "SYSTEM", the plugin will use terragrunt installed in local system. Otherwise, the plugin will try to find the terragrunt executable in cloud.envToolDir folder which matches both system and version parameter. If no matched version found, the plugin will try to download it from Hashicorp website.
- modules (default: null, maven command line variable: cloud.terragrunt.modules): if set value, instead of run terragrunt under target/output folder, this plugin will try to run terragrunt in any sub-folder with matches the name given by this parameter
- system (default: null, maven command line variable: cloud.terragrunt.system): used when cloud.terragrunt.version is not "SYSTEM". It tells this plugin which operation system the terragrunt should run. Available values include: Mac, FreeBSD, OpenBSD and Windows. 
- format (default: null, maven command line variable: cloud.terragrunt.systemFormat): used when cloud.terragrunt.version is not "SYSTEM". It tells this plugin the disk format of target system. Available values include: 386, amd64, arm and arm64.
- arguments (default: null, maven command line variable: cloud.terragrunt.arguments): command arguments to append after terragrunt command, such as apply, refresh, or destroy etc.
- timeout (default: 900, maven command line variable: cloud.terragrunt.commandTimeOut): used when cloud.genScriptOnly is false. Timeout in seconds for maven build.
### Import custom variables
Variables can be passed into plugin and applied during build process by using variable executor. All variables need to be defined in a json file. Please refer to [Variable value look up](#variable-value-look-up) section for specification.
_Sample Configuration:_
```xml
<execution>
   <id>variables</id>
   <goals>
       <goal>variables</goal>
   </goals>
   <configuration>
      <configFile>cloud.json</configFile>
      <lookupFolder>src</lookupFolder>
   </configuration>
</execution>
```
All supported parameters include:
- configFile (default: null, maven command line variable: cloud.variable.config): a json file with all cloud variables defined.
- lookupFolder (default: src, maven command line variable: cloud.variable.lookupFolder): the folder to locate the configFile, by default the code will try to find the configFile in src folder.
## Variable value look up
All variables pass into plugin are defined in JSON file and passed in through variable executor. This plugin has too built in value finder which support value look up in properties file or credstash table. The later is AWS only.
- [Define cloud variable in JSON](#define-cloud-variable-in-json)
- [Look up value in properties file](#look-up-value-in-properties-file)
- [Look up value through Credstash](#look-up-value-through-credstash)
### Define cloud variable in JSON
The variable JSON file should contains a JSON array, each element of this array contains all variables which values stored in the same source. When processing this JSON file, if not value found, the build process will fail immediately if that variable is marked as required. Otherwise the variable will be ignored during the remaining build.
_Sample Could Variable JSON:_
```json
[
  {
    "source": "aws.properties",
    "format": "properties",
    "variables": [
      {
        "name": "aws.username",
        "required": true,
        "toolVars": {
          "packer": "user"
        }
      },
      {
        "name": "aws.region",
        "toolVars": {
          "packer": "region",
          "terragrunt": "region"
        }
      }
    ]
  },
  {
    "format": "credstash",
    "variables": [
      {
        "name": "test-key-ssh_pub-1701",
        "toolVars": {
          "packer": "ssh_public_key"
        }
      }
    ],
    "sourceConfig": {
      "region": "us-east-1",
      "table": "credential-store-tao-dev"
    }
  }
]
```
A variable element is defined as following:
- name: variable name in source
- required: is the variable is required
- toolVars: variable name to pass into different build tools, current allow for property names inside: ansible, packer, terraform and terragrunt.
### Look up value in properties file
To look up values in a properties file and pass them into the plugin, you need to define the following two properties in JSON
- source: the properties file name, the plugin will try to locate this file inside cloud.variable.lookupFolder
- format: has to be "properties" for properties file value look up
In the previous example, if you have the following properties in file aws.properties:
```properties
aws.username=tao.dong
aws.region=us-east-1
```
The plugin will pass "user=tao.dong,region=us-east-1" to packer and "region=us-east-1" to terragrunt respectively.
### Look up value through Credstash
Credstash is tool to store sensitive data in AWS. This value finder functions only when building cloud on AWS. Also to use it, you have to install and configure Credstash locally, the plugin only calls credstash command in local environment to retrieve values. The properties needed for credstash are:
- format: has to be "credstash" for credstash look up
- sourceConfig: optional and only needed you are looking for values in a credstash table other than your local default one. The properties os sourceConfig include:
    - region: AWS region
    - table: Dynamo DB table name
    - key: Credstash key 
    - noline: Don't append newline to returned value
## Misc
#### First Matched rule
when matching files, the plugin will apply the first matched rule. The search will traversal into sub-folders recursively until it reaches a folder a matched file is found, then all the sub-folders of that folder will be ignored.
```
src
 |
 +-- sample_1
 |  |
 |  +-- abc.yml
 |    
 +-- sample_2
 |  |  
 |  +-- main.yml
 |    
 +-- sample_3
 |  |  
 |  +-- main.yml
 |  +-- sub_folder_1
 |     |
 |     +-- main.yml

```
For the file structure above, if we look for main.yml, the files returned after applying first matched rule are src/sample_2/main.yml and src/sample_3/main.yml
## License
MIT
