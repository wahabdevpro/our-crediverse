#! /bin/bash
mvn deploy:deploy-file -DgroupId=org.snmp4j -DartifactId=snmp4j -Dversion=2.2.4 -Dpackaging=jar -Dfile=./snmp4j-2.2.4.jar -DrepositoryId=gitlab-maven -Durl=https://gitlab.com/api/v4/projects/12085055/packages/maven

mvn deploy:deploy-file -DgroupId=org.snmp4j -DartifactId=snmp4j-agent -Dversion=2.1.1 -Dpackaging=jar -Dfile=./snmp4j-agent-2.1.1.jar -DrepositoryId=gitlab-maven -Durl=https://gitlab.com/api/v4/projects/12085055/packages/maven

mvn deploy:deploy-file -DgroupId=org.olap4j -DartifactId=olap4j -Dversion=0.9.7.309-JS-3 -Dpackaging=jar -Dfile=./olap4j-0.9.7.309-JS-3.jar -DrepositoryId=gitlab-maven -Durl=https://gitlab.com/api/v4/projects/12085055/packages/maven
