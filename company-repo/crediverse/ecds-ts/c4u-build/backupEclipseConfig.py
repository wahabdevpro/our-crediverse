#! /usr/bin/python3

import os
import os.path
import sys
import pdb
import untangle
import pprint
import csv
import string
import shutil
import fileinput
import sys

# find . -name .classpath -exec rm {} \;
# find . -name .project -exec rm {} \;

def backupEclipseConfigs(root):
    for root, dirs, files in os.walk("."):
        if '.svn' in dirs:
            dirs.remove('.svn')
        path = root.split(os.sep)
        current = path[len(path) - 1]
        classpathFile = root+'/.classpath'
        projectFile = root+'/.project'
        if os.path.exists(classpathFile) or os.path.exists(projectFile):
            eclipseConfigDir = root+'/oldEclipseConfig'
            if not os.path.exists(eclipseConfigDir):
                os.makedirs(eclipseConfigDir)
                os.system("svn add "+eclipseConfigDir)
            if os.path.exists(classpathFile):
                #print("Classpath :- "+classpathFile)
                os.system("svn move "+classpathFile+" "+eclipseConfigDir+"/classpath")
            else:
                print("ERROR: No classpath file: "+classpathFile)
            if os.path.exists(projectFile):
                #print("Project :- "+projectFile)
                os.system("svn move "+projectFile+" "+eclipseConfigDir+"/project")
            else:
                print("ERROR: No project file: "+projectFile)
        



backupEclipseConfigs('.')
