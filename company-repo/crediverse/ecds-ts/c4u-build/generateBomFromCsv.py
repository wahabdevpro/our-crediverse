#! /usr/bin/python3

import os
import os.path
import sys
import pdb
import untangle
import pprint
import csv
import string

#{   'antlr-2.7.7.jar': 'org.lucee:antlr:2.7.7',
#    'aopalliance-repackaged-2.4.0-b34.jar': 'org.glassfish.hk2.external:aopalliance-repackaged:2.4.0-b34',
#    'apacheds-i18n-2.0.0-M23.jar': 'org.apache.directory.server:apacheds-i18n:2.0.0-M23',

def generateBom(dependencies, filename):
    if not os.path.exists(filename):
        os.mknod(filename)
    ecdsBom = open(filename, "w+")
    
    configStart = """<?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>ga.csys.tooling</groupId>
  <artifactId>ecds-bom</artifactId>
  <version>1.0.0.RELEASE</version>
  <packaging>pom</packaging>
  <name>ECDS (Bill of Materials)</name>
  <description>ECDS (Bill of Materials)</description>
  <url>https://git.concurrent.systems</url>
  <organization>
    <name>Concurrent Systems</name>
    <url>http://www.concurrent.systems</url>
  </organization>
  <licenses>
    <license>
      <name>Apache License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <developers>
    <developer>
      <id>martinc</id>
      <name>Martin Cooper</name>
      <email>martin.cooper@concurrent.systems</email>
    </developer>
  </developers>
  <!-- <scm>
    <connection>scm:git:git://github.com/spring-projects/spring-framework</connection>
    <developerConnection>scm:git:git://github.com/spring-projects/spring-framework</developerConnection>
    <url>https://github.com/spring-projects/spring-framework</url>
  </scm> -->"""
    
    ecdsBom.write(configStart+'\n')
    
    
    ecdsBom.write('\t<dependencyManagement>\n')
    ecdsBom.write('\t\t<dependencies>\n')
    
    for dependency in dependencies:
        #print(dependency['group'])

    
        ecdsBom.write('\t\t\t<dependency>\n')
        ecdsBom.write('\t\t\t\t<groupId>'+dependency['group']+'</groupId>\n')
        ecdsBom.write('\t\t\t\t<artifactId>'+dependency['id']+'</artifactId>\n')
        ecdsBom.write('\t\t\t\t<version>'+dependency['version']+'</version>\n')
        ecdsBom.write('\t\t\t</dependency>\n')
        
    ecdsBom.write('\t\t</dependencies>\n')
    ecdsBom.write('\t</dependencyManagement>\n')
    ecdsBom.write('</project>\n')
    ecdsBom.close()

def loadLibDependencies(dependencies, filename):
    if not os.path.exists(filename):
        return
    with open(filename) as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            if len(row['group']) > 0 and len(row['artifactid']) > 0 and len(row['version']) > 0:
                dependency = {}
                dependency["group"] = row['group']
                dependency["id"] = row['artifactid']
                dependency["version"] = row['version']
                dependencies.append(dependency)

               
csvFileName = 'dependantLibs.csv'
bomFileName = 'pom.xml'
dependencies = []
loadLibDependencies(dependencies, csvFileName)

generateBom(dependencies, bomFileName)

print("To install the bom into maven local, use:\n\t mvn install:install-file -Dfile=./pom.xml -DgroupId=ga.csys.tooling -DartifactId=ecds-bom -Dversion=1.0.0.RELEASE\n")

#pp = pprint.PrettyPrinter(indent=4)
#pp.pprint(dependencies)
