if [ $1 -eq 1 ] ; then
        # Initial installation
        systemctl preset c4u-supervisor.service c4u-hostprocess.service c4u-guiserver.service >/dev/null 2>&1 || : 
fi

# Taken from
# https://github.com/systemd/systemd/blob/v219/src/core/macros.systemd.in#L48L54

