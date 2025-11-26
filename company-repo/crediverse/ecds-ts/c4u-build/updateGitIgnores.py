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

def updateGitIgnores(root):
    for root, dirs, files in os.walk("."):
        if '.svn' in dirs:
            dirs.remove('.svn')
        if '.git' in dirs:
            dirs.remove('.git')
        path = root.split(os.sep)
        current = path[len(path) - 1]
        gradleBuildFile = root+'/build.gradle'
        gitIgnoreFile = root+'/.gitignore'
        if os.path.exists(gradleBuildFile):
            f = open(gitIgnoreFile, 'w')
            f.write('/build/\n');
            f.write('/bin/\n');
            f.write('/.settings/\n');
            f.write('.classpath\n');
            f.write('.project\n');
            f.write('/target/\n');
            f.close();

updateGitIgnores('.')
