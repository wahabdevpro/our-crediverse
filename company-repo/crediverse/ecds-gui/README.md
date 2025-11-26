## SET UP DEV ENV
The following directories miust exist and must be writable by the GUI process:

`/var/opt/cs/ecdsui/log` - GUI will write logs here
`/var/opt/cs/ecdsui/tmp` - Embedded Tomcat writes things here

This is all that is needed for the GUI to run.  You can import the GUI into your favorite IDE, or build it here and run it using gradle with:

`gradle runAdminGui` - Runs on port http://localhost:8084 by default
`gradle runPortalGui` - Runs on port http://localhost:8085 by default

## BUILD
`gradle clean`
`gradle build`
`gradle ecdsGuiRpm`
`gradle publish` - Will create the RPM and publish it to the gitlab repository

## RELEASE
`git ls-remote --tags | tail` - Lists the last 10 tags in the remote repo.  Use this to find what the last release tag was.
`git tag <version>` - Tag the local repository, make sure you sre on master when doing this snd your changes are merged into master.
`git push <version>` - Push the tag you just created to the remote repo.  This will cause the autobuild to take place and push an rpm and docker image to the appropriate repositories on gitlab.

## MISC
NB. The GUI uses lombok, so you will need to configure that in your IDE if you use one.
For detailed info on setting up your environment, see the developer docs here https://gitlab.com/csys/products/ecds/developer-docs