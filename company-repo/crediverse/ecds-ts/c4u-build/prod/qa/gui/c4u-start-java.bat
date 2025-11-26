/var/opt/cs/c4u/installations/attach_vip.sh
sleep 5
/etc/init.d/supervisor start
sleep 5
mysql -u root -pussdgw -e "use hxc; update ct_role set attachCommand = 'sh /var/opt/cs/c4u/installations/attach_vip.sh'; update ct_role set detachCommand = 'sh /var/opt/cs/c4u/installations/detach_vip.sh';"
mysql -u root -pussdgw -e "use hxc; insert into ct_server (serverhost, peerhost) values('c4u-fo','c4u-zone');"
mysql -u root -pussdgw -e "use hxc; insert into ct_server (serverhost, peerhost) values('c4u-zone','c4u-fo');"
sleep 5
/etc/init.d/supervisor restart
sleep 5
/var/opt/cs/c4u/installations/attach_vip.sh
sleep 5