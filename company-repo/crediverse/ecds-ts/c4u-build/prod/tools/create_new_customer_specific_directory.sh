#!/bin/bash

cd ../
STRUCTURE=`ls`
cd ../
mkdir $1
cd $1
for folder in $STRUCTURE; do
	mkdir $folder
done

