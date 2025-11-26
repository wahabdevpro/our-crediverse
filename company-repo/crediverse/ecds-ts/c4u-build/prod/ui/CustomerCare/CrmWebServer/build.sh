#!/bin/bash
ant clean
mkdir -p webapp/WEB-INF/classes
ant

cd ../plugins/CrmPlugin/
ant clean
mkdir -p webapp/WEB-INF/classes
ant
cd -
