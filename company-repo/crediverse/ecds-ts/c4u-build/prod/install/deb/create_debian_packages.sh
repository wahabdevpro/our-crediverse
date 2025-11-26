#!/bin/bash

cd ../../core/HostProcess
ant
cd install/deb
sudo ../../../../install/deb/make_deb.sh hostprocess "hxc.test.TestHost" true
cd ../../../../ui/GuiServer
ant
cd install/deb
sudo ../../../../install/deb/make_deb.sh guiserver "hxc.userinterfaces.gui.jetty.JettyMain" true
cd ../../../../ui/cli
ant
cd install/deb
sudo ../../../../install/deb/make_deb.sh cli "hxc.ui.cli.HxC" false
echo "Finished creating the debian packages..."
