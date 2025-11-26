#!/usr/bin/env bash

robot --quiet  /suites/aaa_wait_for_ecds_to_start.robot
CMD="robot --console verbose --exclude startup --outputdir /reports /suites/"


echo ${CMD}

``${CMD}``
