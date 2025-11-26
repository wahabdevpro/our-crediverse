## SET UP DEV ENV
The directory `/var/opt/cs/ecdsapi/tmp/default` must exist and must be writable by the API.  If this is not in place, then tomcat will fail to start.  To run the API:

`gradle runApi` - Runs on port http://localhost:9084 by default

## BUILD
`gradle clean`
`gradle build`
`gradle publish` - Will create a Docker image and publish it to the gitlab repository

## RELEASE
`git ls-remote --tags | tail` - Lists the last 10 tags in the remote repo. Use this to find what the last release tag was.
`git tag <version>` - Tag the local repository, make sure you sre on master when doing this snd your changes are merged into master.
`git push <version>` - Push the tag you just created to the remote repo. This will cause the autobuild to take place and push the docker image to the appropriate repositories on gitlab.

## Install and Run Swagger

git clone https://github.com/swagger-api/swagger-editor.git
cd swagger-editor
npm install
npm run build
npm start


## Test curl commands from bash

#Authenticate
access_token=$(curl acme:acmesecret@localhost:8084/oauth/token -d grant_type=password -d username=supplier -d password='M@@v1vyEcd$2016' 2>/dev/null | python -c "import sys, json; print json.load(sys.stdin)['access_token']")

#Get agent details:
curl -H "COOKIE:JSESSIONID=9CE83F631107D5BD86652757DF18A77C;" -H "Authorization: Bearer ${access_token}" http://localhost:8084/api/agent | xargs -I{} echo {}

#Web UI Auth
curl -H "Cookie:JSESSIONID=EB34693976E257FBB99BE1A396BB6288; C4USESSIONID=o1g25qxptcet1j6ywjpxlbs1a" http://localhost:8084/api/agents/account/root | xargs -I{} echo {}

## MISC
NB. The GUI uses lombok, so you will need to configure that in your IDE if you use one.
For detailed info on setting up your environment, see the developer docs here https://gitlab.com/csys/products/ecds/developer-docs

## VSCode and lombok
https://github.com/redhat-developer/vscode-java/wiki/Lombok-support

