#! /usr/bin/python3

import os
import os.path
import sys
import pdb
import untangle
import pprint
import csv
import string

def load_properties(filepath, sep='=', comment_char='#'):
    """
    Read the file passed as parameter as a properties file.
    """
    props = {}
    try:
        with open(filepath, "rt") as f:
            for line in f:
                l = line.strip()
                if l and not l.startswith(comment_char):
                    key_value = l.split(sep)
                    key = key_value[0].strip()
                    value = sep.join(key_value[1:]).strip().strip('"') 
                    props[key] = value 
    except FileNotFoundError:
        props['include.libs'] = 'false'
        props['main.class'] = ''
    return props

def parseClassPath(path, projects, currentProject):
    classPathData = untangle.parse(path+os.sep+'.classpath')
    otherprojects = []
    currentProject['projects'] = otherprojects
    currentProject['libs'] = []
    
    # <classpathentry combineaccessrules="false" kind="src" path="/CoreInterfaces"/>
    #pdb.set_trace()
    for pathEntry in classPathData.classpath.classpathentry:
        if pathEntry['kind'] == 'src':
            path = pathEntry['path']
            if path != 'java':
                otherprojects.append(path[1:])
        if pathEntry['kind'] == 'lib':
            currentProject['libs'].append(pathEntry['path'])
            projects['libs'][pathEntry['path']] = 'jar'
        #if pathEntry['kind'] == 'var':
        #    varpath = pathEntry['path']
        #    if varpath.startswith( 'M2_REPO' )
        
def printSubProject(compileFile, projectName, projects):
    compileFile.write('project(\':'+projectName+'\') {\n')
    for dependency in projects:
        if dependency == './java' or dependency.startswith('esources') or dependency == 'ava' or dependency == 'rc' or dependency == 'en' or dependency.find('/') != -1:
            continue
        compileFile.write('     evaluationDependsOn(\':'+dependency+'\')\n')
        compileFile.write('     dependencies {\n')
        compileFile.write('         compile project(\':'+dependency+'\')\n')
        compileFile.write('     }\n')
    compileFile.write('}\n')

def writeProjectGradle(compileFile, currentProject, basepath, projectName, projects, dependencyType):
    useMavenLocal=False
    #if projectName == 'RestProtocol':
    #    pdb.set_trace()
    needsJunit = {}
    needsJunit['ClassicTests'] = ''
    #needsJunit['BasicLocationServiceTest'] = ''
    needsJunit['SubscriptionServiceTests'] = ''
    needsJunit['VoucherSimTests'] = ''
    
    #gradleFileName = basepath+'/'+projectName.lower()+'.gradle'
    
    
    gradleFileName = basepath+os.sep+'build.gradle'
    #if not os.path.exists(gradleFileName):
    #    os.mknod(gradleFileName)
    gradleFile = open(gradleFileName, "w+")
    
    if useMavenLocal:
        # Used when plugin is deployed to maven local for testing
        gradleFile.write('buildscript {\n')
        gradleFile.write('\trepositories {\n')
        gradleFile.write('\t\tmaven {\n')
        gradleFile.write('\t\t\turl \'/home/martinc/.m2/repository\'\n')
        gradleFile.write('\t\t}\n')
        gradleFile.write('\t\tjcenter()\n')
        gradleFile.write('\t}\n')
        gradleFile.write('\tdependencies {\n')
        gradleFile.write('\t\tclasspath "ga.csys.tooling:apply-default-plugin:0.17"\n')
        gradleFile.write('\t}\n')
        gradleFile.write('}\n')
        gradleFile.write('\n')
        gradleFile.write('\n')
        gradleFile.write('apply plugin: \'apply-defaults-plugin\'\n')
    else:
        # Used in the final version
        gradleFile.write('plugins {\n')
        gradleFile.write('  id "ga.csys.tooling.apply-defaults-plugin" version "0.19"\n')
        gradleFile.write('}\n')

    gradleFile.write('\n')
    
    gradleFile.write('description = "TS"\n')
    gradleFile.write('group = "ga.csys.c4u"\n')
    gradleFile.write('version = "1.0-SNAPSHOT"\n')
    #gradleFile.write('version = "1.0.0"\n')
    gradleFile.write('\n')
    
   # gradleFile.write('configurations.all {\n')
   # gradleFile.write('    // check for updates every build\n')
   # gradleFile.write('    resolutionStrategy.cacheChangingModulesFor 0, \'seconds\'\n')
    
    #if projectName != 'CoreInterfaces':
    #if projectName == 'HxC' or projectName == 'Supervisor' or projectName == 'HostProcess' or projectName == 'NumberPlanService':
    #    gradleFile.write('    all*.exclude group: \'com.sun.mail\', module: \'mailapi\'\n')
   # gradleFile.write('}\n')


   # gradleFile.write('apply plugin: \'java\'\n')
   # gradleFile.write('apply plugin: \'maven\'\n')
   # gradleFile.write('\n')
   # gradleFile.write('sourceCompatibility = 1.7\n')
   # gradleFile.write('targetCompatibility = 1.7\n')
    
   # gradleFile.write('configurations {\n')
   # gradleFile.write(' deployerJars\n')
   # gradleFile.write('}\n')

  #  gradleFile.write('\n')
  #  gradleFile.write('repositories {\n')
    #gradleFile.write('mavenLocal()\n')
    
  #  gradleFile.write('    maven {\n')
  #  gradleFile.write('        url \'https://warehouse.concurrent.systems/teams/ecds/maven/repo\'\n')
  #  gradleFile.write('        credentials {\n')
  #  gradleFile.write('            username = "$warehouseUsername"\n')
  #  gradleFile.write('            password = "$warehousePassword"\n')
  #  gradleFile.write('        }\n')
  #  gradleFile.write('    }\n')
    
 #   gradleFile.write('jcenter()\n')
    #gradleFile.write('	mavenCentral()\n')
    
    #if projectName == 'CreditDistributionServiceTests' or projectName == 'SnmpConnector' or projectName == 'TestHost' or projectName == 'UnitTests':
    #    gradleFile.write('  flatDir {\n')
    #    gradleFile.write('	    dirs \'lib\'\n')
    #    gradleFile.write('  }\n')
   # if projectName == 'ReportingService' or projectName == 'AdvancedCreditTransferTests' or 'AirSimTests':
   #     gradleFile.write('  maven {\n')
   #     gradleFile.write('      url "http://jaspersoft.artifactoryonline.com/jaspersoft/third-party-ce-artifacts/"\n')
   #     gradleFile.write('  }\n')
    
    
   # gradleFile.write('}\n')
   # gradleFile.write('\n')
    
   # gradleFile.write('uploadArchives {\n') 
   # gradleFile.write('  repositories {\n')
   # gradleFile.write('      mavenDeployer {\n')
   # gradleFile.write('          configuration = configurations.deployerJars\n')
    #gradleFile.write('          pom.artifactId = project.property("name").toLowerCase()\n') # Causes duplicate artifacts in the repository, no idea why
   # gradleFile.write('          pom.artifactId = project.property("name")\n')
    #gradleFile.write('          repository(url: mavenLocal().url)\n')
   # gradleFile.write('          repository(url: \'scpexe://warehouse.concurrent.systems/var/opt/webdav/warehouse.concurrent.co.za/teams/ecds/maven/repo\')\n')
   # gradleFile.write('      }\n')
   # gradleFile.write('  }\n')
   # gradleFile.write('}\n')
    
   # gradleFile.write('\n')
    
    gradleFile.write('sourceSets {\n')
    gradleFile.write('    main {\n')
    
    
    #gradleFile.write('        java {\n')
    #if projectName == 'SoapConnector':
    #    gradleFile.write('            srcDirs = [\'Java\'] // Custom source directory as we don\'t seem to be able to follow any standard layouts.\n')
    #else:
    #    gradleFile.write('            srcDirs = [\'java\'] // Custom source directory as we don\'t seem to be able to follow any standard layouts.\n')
    #gradleFile.write('        }\n')
    
    
    #if os.path.exists(basepath+"/resources") or os.path.exists(basepath+"/share/resources"):
    gradleFile.write('      resources {\n')
    gradleFile.write('          srcDirs "src/main/resources", "resources", "share/resources"\n')
    gradleFile.write('      }\n')

    gradleFile.write('    }\n')
    gradleFile.write('}\n')
    gradleFile.write('\n')
    
  #  gradleFile.write('task copyToLib(type: Copy) {\n')
  ##  gradleFile.write('  from configurations.runtime\n')
  #  gradleFile.write('}\n')
  #  gradleFile.write('build.dependsOn(copyToLib)\n')
    
    jarConfig = load_properties(basepath+'/jar.properties')
    
    if jarConfig['include.libs'] == 'true' or jarConfig.get('main.class', '') != '':
        gradleFile.write('jar {\n')
  #      gradleFile.write('  zip64 true\n')
        if jarConfig['include.libs'] == 'true' and 'CreditDistribution222' in projectName:
            gradleFile.write('  from {\n')
            gradleFile.write('      (configurations.runtime).collect {\n')
            gradleFile.write('          it.isDirectory() ? it : zipTree(it)\n')
            gradleFile.write('      }\n')
            gradleFile.write('  } {\n')
            gradleFile.write('  exclude "META-INF/*.SF"\n')
            gradleFile.write('  exclude "META-INF/*.DSA"\n')
            gradleFile.write('  exclude "META-INF/*.RSA"\n')
            gradleFile.write('  }\n')

        if jarConfig.get('main.class', '') != '':
            gradleFile.write('  manifest {\n')
            mainClass = jarConfig['main.class']
            gradleFile.write('      attributes("Main-Class": "'+mainClass+'" )\n')
            gradleFile.write('  }\n')
        gradleFile.write('}\n')
    
    
    
 #   gradleFile.write('task sourcesJar(type: Jar, dependsOn: classes) {\n')
 #   gradleFile.write('  classifier = \'sources\'\n')
 #   gradleFile.write('  from sourceSets.main.allSource\n')
 #   gradleFile.write('}\n')
    
 #   gradleFile.write('task javadocJar(type: Jar, dependsOn: javadoc) {\n')
 #   gradleFile.write('  classifier = \'javadoc\'\n')
 #   gradleFile.write('  from javadoc.destinationDir\n')
 #   gradleFile.write('}\n')
    
 #   gradleFile.write('artifacts {\n')
 #   gradleFile.write('  archives sourcesJar\n')
 #   gradleFile.write('//  archives javadocJar\n')
 #   gradleFile.write('}\n')


    gradleFile.write('dependencies {\n')
    gradleFile.write('  deployerJars "org.apache.maven.wagon:wagon-ssh-external:2.12"\n')
    printSubProject(compileFile, currentProject['name'], currentProject['projects'])
    for dependency in currentProject['projects']:
        if dependency == './java' or dependency.startswith('esources') or dependency == 'ava' or dependency == 'rc' or dependency == 'en' or dependency.find('/') != -1:
            continue
            
        if (dependencyType == 'project'):
            gradleFile.write('\tcompile project(\':'+dependency+'\')\n')
        else:
            gradleFile.write('\tcompile \'ga.csys.c4u:'+dependency+':1.0-SNAPSHOT\'\n')
            #gradleFile.write('\tcompile \'ga.csys.c4u:'+dependency.lower()+':1.0.0\'\n')
            #compile group: "groupId", name: "artifactId", version: "1.0", changing: true
        
    if projectName == 'ReportingService' or projectName == 'CreditDistributionServiceTests':
        if (dependencyType == 'project'):
            gradleFile.write('\tcompile project(\':SoapConnector\')\n')
        else:
            gradleFile.write('\tcompile \'ga.csys.c4u:SoapConnector:1.0-SNAPSHOT\'\n')
            #gradleFile.write('\tcompile \'ga.csys.c4u:soapconnector:1.0.0\'\n')
            #compile group: "groupId", name: "artifactId", version: "1.0", changing: true
    
    if projectName == 'CreditDistributionServiceTests' or projectName == 'SnmpConnector' or projectName == 'TestHost':
        gradleFile.write('\tcompile \'org.snmp4j:snmp4j:2.2.4\'\n')
        gradleFile.write('\tcompile \'org.snmp4j:snmp4j-agent:2.1.1\'\n')
        
    if projectName in needsJunit:
        gradleFile.write('\tcompile \'junit:junit:4.12\'\n')
        
    dependencyData = projects['depend']
    for dependency in currentProject['libs']:
        libname = os.path.basename(os.path.normpath(dependency))
        if libname in projects['depend']:
            depvalue = projects['depend'][libname]
            #print("Got dependency:: ", depvalue)
            #gradleFile.write('\n')
            gradleFile.write('\tcompile \''+depvalue+'\'\n')
        #else:
        #    print("Unable to find dependency:: ", libname)
    
    gradleFile.write('}\n')
    
    
    #gradleFile.write('build.finalizedBy(uploadArchives)\n')
    
    gradleFile.close()

def writeGradleSettings(projects, ignoreList):
    gradleSettingFilename = "settings.gradle"
    #if not os.path.exists(gradleSettingFilename):
    #    os.mknod(gradleSettingFilename)
    gradleSettings = open(gradleSettingFilename, "w+")
    
    gradleSettings.write('rootProject.name = \'ecds-ts\'\n')
    gradleSettings.write('\n')
    #gradleSettings.write('rootProject.children.each {\n')
    #gradleSettings.write('\tString fileBaseName = it.name.toLowerCase()\n')
    #gradleSettings.write('\tit.buildFileName = "${fileBaseName}.gradle"\n')
    #gradleSettings.write('}\n')
    
    #gradleSettings.write('\n')
    
    includeList = []
    first = True;
    for key, value in projects.items():
        if key in ignoreList:
            continue
        if (first):
            gradleSettings.write('include "'+key+'"')
            first = False
        else:
            gradleSettings.write(',\n        "'+key+'"')
    gradleSettings.write('\n\n')
    
    for key, value in projects.items():
        if key in ignoreList:
            continue
        # project(":CoreInterfaces").projectDir = file("prod/core/CoreInterfaces")
        #pdb.set_trace()
        path = value['path']
        if path.startswith("."+os.sep):
            path = path[2:]
        path = path.replace('\\', '/')
        #pdb.set_trace()
        gradleSettings.write('project(":'+key+'").projectDir = file("'+path+'")\n')
        includeList.append(key)

    gradleSettings.write('\n')
    gradleSettings.close()

def writeDependencyFile(projects, filename):
    existing = {}
    with open(filename, 'w', newline='') as csvfile:
        fieldnames = ['library', 'group', 'artifactid', 'version']
        libwriter = csv.DictWriter(csvfile, fieldnames=fieldnames)
        libwriter.writeheader()
        for currentLib in projects['libs']:
            libname = os.path.basename(os.path.normpath(currentLib))
            if libname in existing:
                continue;
            else:
                libwriter.writerow({'library': libname, 'group': '', 'artifactid': '', 'version': ''})
                existing[libname] = 'true'

def processConfig(compileFile, basepath, projectName, projects, buildType, ignoreList):
    currentProject = {}
    projectData = untangle.parse(basepath+os.sep+'.project')
    currentProject['name'] = projectData.projectDescription.name.cdata
    currentProject['path'] = basepath
    if 'android' in currentProject['path'].lower():
        #print("Ignoring Android Project => "+currentProject['name']+"::"+currentProject['path'])
        return
    if '/bin/' in currentProject['path'].lower():
        #print("Ignoring Android Project => "+currentProject['name']+"::"+currentProject['path'])
        return
    if '/poc/' in currentProject['path'].lower():
        #print("Ignoring POC Project => "+currentProject['name']+"::"+currentProject['path'])
        return
    if currentProject['name'] in ignoreList:
        return
    if  currentProject['name'] in projects:
        #pdb.set_trace()
        origProject = projects[currentProject['name']]
        print("Duplicate Project => "+currentProject['name']+"::"+currentProject['path']+" || orig "+origProject['name']+"::"+origProject['path'])
        return
    else:
        projects[currentProject['name']] = currentProject
    # currentProject['description'] = projectData.projectDescription.comment.cdata
    
    classPathData = parseClassPath(basepath, projects, currentProject)
    writeProjectGradle(compileFile, currentProject, basepath, projectName, projects, buildType)

def loadLibDependencies(projects, filename):
    projects['depend'] = {}
    if not os.path.exists(filename):
        return
    with open(filename) as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            if len(row['group']) > 0 and len(row['artifactid']) > 0 and len(row['version']) > 0:
                varname = row['group']+"_"+row['artifactid']
                varname = varname.replace(".", "_");
                varname = varname.replace("-", "_");
                #dependency = row['group']+':'+row['artifactid']+':$'+varname
                #dependency = row['group']+':'+row['artifactid']+':'+row['version']
                dependency = row['group']+':'+row['artifactid']
                projects['depend'][row['library']] = dependency
                
               # print(varname, " = ", row['version'], "\n")

ignoreList = {}
ignoreList['libs'] = ''
ignoreList['depend'] = ''
ignoreList['.'+os.sep+'java'] = ''

ignoreList['BasicLocationService'] = ''
ignoreList['CustomerCareUI'] = ''
ignoreList['MarketingGUI'] = ''
ignoreList['LanguageServicePoc'] = ''
ignoreList['LanguageChangeCrmAspect'] = ''
ignoreList['C4uEclipseTemplate'] = ''
ignoreList['BurnTester'] = ''
ignoreList['CallMeBackService'] = ''
ignoreList['FriendsAndFamilyService'] = ''
ignoreList['ZTEConnector'] = ''
ignoreList['CrmPOCPlugin'] = ''
ignoreList['CrmPlugin'] = ''

ignoreList['ClassicTests'] = ''
ignoreList['BasicLocationServiceTest'] = ''
ignoreList['SubscriptionServiceTests'] = ''
ignoreList['VoucherSimTests'] = ''

ignoreList['c4u-build-plugin'] = ''

csvFileName = 'dependantLibs.csv'
projects = {}
projects['libs'] = {}
loadLibDependencies(projects, csvFileName)
pp = pprint.PrettyPrinter(indent=4)
compileFile = open("compileDependencies.gradle", "w+")
for root, dirs, files in os.walk("."):
    if '.svn' in dirs:
        dirs.remove('.svn')
    path = root.split(os.sep)
    if os.path.isfile(root+os.sep+".classpath") and os.path.isfile(root+os.sep+".project"):
        processConfig(compileFile, root, path[len(path) - 1], projects, 'not-project', ignoreList)
compileFile.close()
#pp.pprint(projects)

#DONOTUSEAGAIN writeDependencyFile(projects, csvFileName)

writeGradleSettings(projects, ignoreList)



