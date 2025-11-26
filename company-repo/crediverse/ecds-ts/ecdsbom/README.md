
+
+
++
++# The ECDS BOM
The ECDS BOM is used to control the versions of packaghes used cross the build scripts. A Bill Of Materials file is a maven artifact.  Even though we are using gradle, we use Maven repositories (at least until gradle 7.1.1 is stable, as it has a better mechanism).  The BOM is just a sing;le place to declare package details.  Then in the actual build files, you don't need to specify a version number for the various libraries.  The main benefit is to ensure one set of libraries are used throughout the project, so avoiding conflicts between library versions.  This also gives us a single location to maintain that common version information.

See https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#bill-of-materials-bom-poms for detailed information about the maven BOM.

## Prerequisites
A working copy of maven must be installed (`sudo apt install maven`)
Add the following details to your `~/.m2/settings.xml` file (create the file / directory if it doesn't already exist):

``xml
<?xml version="1.0" encoding="UTF-8*" standalone="no"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
          <id>gitlab-maven</id>
          <configuration>
            <httpHeaders>
              <property>
                <name>Deploy-Token</name>
                <value>PUT_YOUR_GITLAB_DEPLOY_TOKEN_HERE</value>
              </property>
            </httpHeaders>
          </configuration>
        </server>
    </servers>
</settings>
```
Be sure to replace `PUT_YOUR_GITLAB_DEPLOY_TOKEN_HERE` with your actual deploy token. If you can't get a valid deploy token anywhere else, you can create your own here https://gitlab.com/groups/csys/products/ecds/-/settings/repository with at least `read_package_registry` and `write_package_registry` scopes.**

## Updating the BOM after a change
Edit the pom.xml and change the version tag to reflect the new version, eg.
```xml
<version>1.0.0.RELEASE</version>```
Run the command
```
mvn deploy
```
The BOM should be uploaded to the gitlab package registry and be visible here https://gitlab.com/csys/products/ecds/ecds-ts/-/packages. If you click on the package name in the registry, you'll see details of how to use it in both Gradle and Maven (select from the right menu).

## Using the BOM in a gradle build
NB. This is already configured in the Crediverse TS.
```json
plugins {
  id "java"
  id 'maven-publish'
  id "io.spring.dependency-management" version "1.0.11.RELEASE"
}

apply plugin: 'io.spring.dependency-management'

repositories {
  maven {
    url "https://gitlab.com/api/v4/projects/12085055/packages/maven"
    name "Gitlab ECDS Maven Repository"
  }
}

publishing {
    publications {
        library(MavenPublication) {
            from components.java
        }
    }
    repositories {
        maven {
            url "https://gitlab.example.com/api/v4/projects/12085055/packages/maven"
            /*credentials(HttpHeaderCredentials) {
                name = csys.crediverse.deploy.username
                value = csys.crediverse.deploy.token // the variable resides in ~/.gradle/gradle.properties
            }
            authentication {
                header(HttpHeaderAuthentication)
            }*/
        }
    }
}

dependencyManagement {
	imports {
		mavenBom "ga.csys.c4u.tooling:c4u-bom:1.0.0.RELEASE"
	}
}
```


