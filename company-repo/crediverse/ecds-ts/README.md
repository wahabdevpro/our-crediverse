## SET UP DEV ENV
To run the TS, the following directories must be created and be writable by the TS:

mkdir -p /opt/cs/c4u/1.0/hostprocess/plugins
mkdir -p /var/opt/cs/c4u/backup/database
mkdir -p /var/opt/cs/c4u/log
mkdir -p /var/opt/cs/c4u/cdr
mkdir -p /var/opt/cs/c4u/share

`gradle runC4uGui` - Runs on port http://localhost:8082 by default
`gradle runTestHost` - Runs the EcdsTestHost on port http://localhost:14400 by default

## BUILD
- `gradle clean`
- `gradle build`
- `gradle publish` - Will create the RPM and publish it to the gitlab repository
- `docker build -t ecds-ts:latest .` - Build a local docker image

## RELEASE
- `git ls-remote --tags | tail` - Lists the last 10 tags in the remote repo.  Use this to find what the last release tag was.
- `git tag <version>` - Tag the local repository, make sure you sre on master when doing this snd your changes are merged into master.
- `git push <version>` - Push the tag you just created to the remote repo.  This will cause the autobuild to take place and push an rpm and docker image to the appropriate repositories on gitlab.

## CREATE LICENSE
see [how-to-create-license.md](./how-to-create-license.md) for details

## MISC
NB. The TS uses lombok, so you will need to configure that in your IDE if you use one.
For detailed info on setting up your environment, see the developer docs here https://gitlab.com/csys/products/ecds/developer-docs

### ALARMS / SNMP
Note that the MIB file for SNMP is in the company wide [OID repository here](https://gitlab.com/csys/ops/oid-repository) in the file [CONCURRENT-SYSTEMS-C4U-MIB.mib](https://gitlab.com/csys/ops/oid-repository/-/blob/master/mibs/CONCURRENT-SYSTEMS-C4U-MIB.mib)
For details of testing alarms, see the [SNMP-ALARMS](https://gitlab.com/csys/products/ecds/developer-docs/SNMP-ALARMS.md)

## VSCode
To use VSCode, there is an example workspace file available in the [developer-docs](https://gitlab.com/csys/products/ecds/developer-docs) repo
### VSCode and lombok
[VSCode Lombok Addon](https://github.com/redhat-developer/vscode-java/wiki/Lombok-support)

