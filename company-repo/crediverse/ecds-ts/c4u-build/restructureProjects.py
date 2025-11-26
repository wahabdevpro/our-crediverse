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

def listDuplicates(file):
    for root, dirs, files in os.walk("."):
        if '.svn' in dirs:
            dirs.remove('.svn')
        path = root.split(os.sep)
        current = path[len(path) - 1]
        if os.path.isdir(root) and current == 'java':
            if len(path) > 1:
                parent = path[len(path) - 2]
            else:
                parent = ''
            if len(path) > 2:
                grandParent = path[len(path) - 3]
            else:
                grandParent = ''
            if grandParent != 'src' and parent != 'test' and parent != 'main':
                dirToMove = path.pop()
                baseDir = '/'.join(path)
                if os.path.exists(baseDir+'/src/main/java') and os.path.exists(baseDir+'/java'):
                    print('rm -rf '+baseDir+'/src/main/java')
        
        
def updateClassPath(file):
    for line in fileinput.input(file, inplace=1):
        if "kind=\"src\"" in line:
            line = line.replace("path=\"java\"","path=\"src/main/java\"")
            line = line.replace("path=\"Java\"","path=\"src/main/java\"")
            
        sys.stdout.write(line)

def restructureSourceFolders(root):
    for root, dirs, files in os.walk("."):
        if '.svn' in dirs:
            dirs.remove('.svn')
        path = root.split(os.sep)
        current = path[len(path) - 1]
        if os.path.isdir(root) and current == 'java':
            if len(path) > 1:
                parent = path[len(path) - 2]
            else:
                parent = ''
            if len(path) > 2:
                grandParent = path[len(path) - 3]
            else:
                grandParent = ''
            if grandParent != 'src' and parent != 'test' and parent != 'main':
                dirToMove = path.pop()
                baseDir = '/'.join(path)
                if os.path.exists(baseDir+'/.classpath'):
                    if False:
                        if not os.path.exists(baseDir+'/src/main/resources'):
                            os.makedirs(baseDir+'/src/main/resources')
                        if not os.path.exists(baseDir+'/src/test/resources'):
                            os.makedirs(baseDir+'/src/test/resources')
                        if not os.path.exists(baseDir+'/src/test/java'):
                            os.makedirs(baseDir+'/src/test/java')
                        
                    #print ("Attempting to move ", baseDir+'/'+dirToMove, " => ", baseDir+'/src/main')
                    #print ("svn move ", baseDir+'/'+dirToMove, " => ", baseDir+'/src/main')
                    #print ("svn move "+baseDir+'/'+dirToMove+" "+baseDir+'/src/main')
                    destinationDir = baseDir+'/src/main'
                    sourceDir = baseDir+'/'+dirToMove
                    os.system("svn mkdir --parents "+destinationDir)
                    #svn mkdir --parents ./mtncm/ui/wui/plugins/CustomerCareUI/src/main
                    os.system("svn move "+sourceDir+" "+destinationDir)
                    #shutil.move(baseDir+'/'+dirToMove, baseDir+'/src/main')
                    updateClassPath(baseDir+'/.classpath')
                    #print ("Updated src attribue in ", baseDir+'/.classpath')


shutil.move('./prod/connectors/SoapConnector/Java', './prod/connectors/SoapConnector/java')
restructureSourceFolders('.')
shutil.move('./prod/connectors/SoapConnector/java', './prod/connectors/SoapConnector/src/main/java')

#listDuplicates('.')

#updateClassPath('mtncm/ui/wui/plugins/CustomerCareUI/.classpath')
