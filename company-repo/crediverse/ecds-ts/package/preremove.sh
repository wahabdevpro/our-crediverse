if [ $1 -eq 0 ] ; then
        # Package removal, not upgrade
        systemctl --no-reload disable c4u-supervisor.service c4u-hostprocess.service c4u-guiserver.service > /dev/null 2>&1 || :
        systemctl stop c4u-supervisor.service c4u-hostprocess.service c4u-guiserver.service > /dev/null 2>&1 || :
fi
