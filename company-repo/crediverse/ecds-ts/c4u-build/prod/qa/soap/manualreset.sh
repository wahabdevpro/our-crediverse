#!/bin/bash
ssh c4u "/etc/init.d/supervisor stop" 
ssh c4u "var/opt/cs/c4u/installations/attach_vip.sh"
ssh c4u "/etc/init.d/supervisor start"
