Brain Dead: 08438957
Bone Head: 1034278 00000
Cabine Accounts:
new.user M00vci@2345
even.newer xd%PE8TC -> xd%25PE8TC
eintermed sPP0P#Ql

#Authenticate
access_token=$(curl ecdsclient:7a361a9c87824855a9cfba63129730af@localhost:9084/oauth/token -d grant_type=password -d username=supplier -d password='M@@v1vyEcd$2016' 2>/dev/null | python -c "import sys, json; print json.load(sys.stdin)['access_token']")
access_token=$(curl ecdsclient:7a361a9c87824855a9cfba63129730af@localhost:9084/oauth/token -d grant_type=password -d username=bundle.web -d password='Qaz@2wsx' 2>/dev/null | python -c "import sys, json; print json.load(sys.stdin)['access_token']") && echo ${access_token}
#new.user
access_token=$(curl ecdsclient:7a361a9c87824855a9cfba63129730af@localhost:9084/oauth/token -d grant_type=password -d username=new.user -d password='M00vci@2345' 2>/dev/null | python -c "import sys, json; print json.load(sys.stdin)['access_token']") && echo ${access_token}
#even.newer
access_token=$(curl ecdsclient:7a361a9c87824855a9cfba63129730af@localhost:9084/oauth/token -d grant_type=password -d username=even.newer -d password='xd%25PE8TC' 2>/dev/null | python -c "import sys, json; print json.load(sys.stdin)['access_token']") && echo ${access_token}
#eintermed
access_token=$(curl ecdsclient:7a361a9c87824855a9cfba63129730af@localhost:9084/oauth/token -d grant_type=password -d username=eintermed -d password='sPP0P#Ql' 2>/dev/null | python -c "import sys, json; print json.load(sys.stdin)['access_token']") && echo ${access_token}
#production amer
access_token=$(curl ecdsclient:7a361a9c87824855a9cfba63129730af@localhost:9084/oauth/token -d grant_type=password -d username=51249812 -d password='00000' 2>/dev/null | python -c "import sys, json; print json.load(sys.stdin)['access_token']") && echo ${access_token}

#Get agent details:
curl -H "COOKIE:JSESSIONID=9CE83F631107D5BD86652757DF18A77C;" -H "Authorization: Bearer ${access_token}" http://localhost:9084/api/agent && echo

#Web UI Auth
curl -H "Cookie:JSESSIONID=EB34693976E257FBB99BE1A396BB6288; C4USESSIONID=o1g25qxptcet1j6ywjpxlbs1a" http://localhost:9084/api/agents/account/root | xargs -I{} echo {}

#Authenticate
access_token=$(curl ecdsclient:7a361a9c87824855a9cfba63129730af@localhost:9084/oauth/token -d grant_type=password -d username=3242345645 -d password='00000' 2>/dev/null | python -c "import sys, json; print json.load(sys.stdin)['access_token']") && echo ${access_token}

#Refresh
refresh_token=$(curl ecdsclient:7a361a9c87824855a9cfba63129730af@localhost:9084/oauth/token -d grant_type=password -d username=3242345645 -d password='00000' 2>/dev/null | python -c "import sys, json; print json.load(sys.stdin)['refresh_token']") && echo ${refresh_token}

access_token=$(curl ecdsclient:7a361a9c87824855a9cfba63129730af@localhost:9084/oauth/token -d 'grant_type=refresh_token&refresh_token=${refresh_token}' 2>/dev/null | python -c "import sys, json; print json.load(sys.stdin)['access_token']") && echo ${access_token}
curl ecdsclient:7a361a9c87824855a9cfba63129730af@localhost:9084/oauth/token -d "grant_type=refresh_token&refresh_token=${refresh_token}"

#Get Agent
curl --verbose -X GET -H "Authorization: Bearer ${access_token}" http://localhost:9084/api/account || echo 

#Update Agent
curl -X PUT -d '{"title":"Prof","firstName":"Bone","surname":"Head","language":"EN","email":"donovan.hutcheon@concurrent.systems"}' -H "Authorization: Bearer ${access_token}" -H "Content-Type: application/json" http://localhost:9084/api/account | xargs -I{} echo {}

accountID:"", accountNumber:"", mobileNumber:"3242345645", title:"", firstName:"Group", initials:"", surname:"User", language:"en", altPhoneNumber:"", email:"", tierName:"", state:""

{accountID:3492453,accountNumber:U00027,msisdn:1034278,title:Mr,firstName:Bone,surname:Head,language:EN,altPhoneNumber:+27118156535,email:donovan.hutcheon@concurrent.systems,tierName:eCabine,state:ACTIVE,activationDate:2017/11/03}

#Get Account
curl -X GET -H "Authorization: Bearer ${access_token}" http://localhost:9084/api/account/balance | xargs -I{} echo {}

#Get Bundles
curl -X GET -H "Authorization: Bearer ${access_token}" http://localhost:9084/api/bundle | xargs -I{} echo {}
curl -X GET -H "Authorization: Bearer ${access_token}" "http://localhost:9084/api/bundle?offset=0&limit=10" | xargs -I{} echo {}

#Get AgentUser
curl -X GET -H "Authorization: Bearer ${access_token}" http://localhost:9084/api/account/user | xargs -I{} echo {}

#Update AgentUser
curl -X PUT -d '{"title":"Mr","firstName":"Bozo","surname":"ClownBoy","language":"en","email":"donovan.hutcheon@concurrent.systems"}' -H "Authorization: Bearer ${access_token}" -H "Content-Type: application/json" "http://localhost:9084/api/user" | xargs -I{} echo {}

curl --verbose -X PUT -d '{"title":"Dr","firstName":"Bundle","surname":"WebConnect","initials":"BW","language":"EN","email":"donovan.hutcheon@concurrent.systems","mobileNumber":"943246354","state":"ACTIVE"}' -H "Authorization: Bearer ${access_token}" -H "Content-Type: application/json" "http://localhost:9084/api/account/user" | xargs -I{} echo {}

curl -X PUT -d '{"userID":"3492453","title":"Mr","firstName":"Bozo","surname":"Clown","language":"en","email":"donovan.hutcheon@concurrent.systems","mobileNumber:3242345645","state":"ACTIVE"}' -H "Authorization: Bearer ${access_token}" -H "Content-Type: application/json" "http://localhost:9084/api/user" | xargs -I{} echo {}


#Get Transactions
curl -X GET -H "Authorization: Bearer ${access_token}" http://localhost:9084/api/account/transaction && echo

#Get Transactions with Sort
curl -X GET -H "Authorization: Bearer ${access_token}" "http://localhost:9084/api/account/transaction?sort=status%2Btype-&offset=0&limit=4" && echo

#Get Transaction
curl -X GET -H "Authorization: Bearer ${access_token}" http://localhost:9084/api/account/transaction/67120849 && echo


#Sell Airtime
curl --verbose -X POST -d '{"targetMSISDN":"40088132","amount":"500"}' -H "Authorization: Bearer ${access_token}" -H "Content-Type: application/json" "http://localhost:9084//api/account/transaction/airtime/sale" | xargs -I{} echo {}

#Login with an eIntermed
access_token2=$(curl ecdsclient:7a361a9c87824855a9cfba63129730af@localhost:9084/oauth/token -d grant_type=password -d username=1111111 -d password='00000' 2>/dev/null | python -c "import sys, json; print json.load(sys.stdin)['access_token']") && echo ${access_token}

#Transfer Credit from eIntermed to eStore
curl -X POST -d '{"targetMSISDN":"1034278","amount":"500"}' -H "Authorization: Bearer ${access_token}" -H "Content-Type: application/json" "http://localhost:9084/api/account/transaction/transfer" && echo

#Change Password
curl --verbose -X POST -d '{"currentPassword":"M00vci@2345","newPassword":"M00vci@23456","repeatPassword":"M00vci@23456"}' -H "Authorization: Bearer ${access_token}" -H "Content-Type: application/json" "http://localhost:9084/api/account/user/password" | xargs -I{} echo {}


curl --verbose -X POST -d '{"currentPassword":"xd%25PE8TC","newPassword":"M00vci@234566666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666","repeatPassword":"M00vci@234566666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666"}' -H "Authorization: Bearer ${access_token}" -H "Content-Type: application/json" "http://localhost:9084/api/account/user/password" && echo

#Change Password Rejects
curl --verbose -X POST -d '{"currentPassword":"M00vci@2345","newPassword":"abcdefgh","repeatPassword":"abcdefgh"}' -H "Authorization: Bearer ${access_token}" -H "Content-Type: application/json" "http://localhost:9084/api/account/user/password" | xargs -I{} echo {}

curl -X GET -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZ2VudElEIjozNDkyNDU1LCJhZ2VudFVzZXJJRCI6NDA2LCJ3ZWJVc2VySUQiOm51bGwsInVzZXJfbmFtZSI6IjExMTExMTEiLCJzY29wZSI6WyJvcGVuaWQiXSwiZXhwIjoxNTE1NTM2Njc2LCJqdGkiOiI1YzgxN2U0MS1kYjI2LTRjZmItYTgzZi0wNjU3NDZmN2JiMTMiLCJjbGllbnRfaWQiOiJhY21lIiwic2lkIjoiNDM5MzE4YjBjN2Q2NDJmZGFiMDJlMjg3MjJkZjMyNGIifQ.zCmg8Mau3GHqnqKDgFCSoLqVyICwMG5B9tQnY-hj-Cw","token_type":"bearer","refresh_token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZ2VudElEIjozNDkyNDU1LCJhZ2VudFVzZXJJRCI6NDA2LCJ3ZWJVc2VySUQiOm51bGwsInVzZXJfbmFtZSI6IjExMTExMTEiLCJzY29wZSI6WyJvcGVuaWQiXSwiYXRpIjoiNWM4MTdlNDEtZGIyNi00Y2ZiLWE4M2YtMDY1NzQ2ZjdiYjEzIiwiZXhwIjoxNTE4MDg1NDc2LCJqdGkiOiJlNWJjYzE3Yy0wOWMzLTQ5MmUtYjQ5YS0wZjFhMTVkMWRkOWIiLCJjbGllbnRfaWQiOiJhY21lIiwic2lkIjoiNDM5MzE4YjBjN2Q2NDJmZGFiMDJlMjg3MjJkZjMyNGIifQ.O2joAWZyiJt6ARhLYAf2Ucz4a5ATiKNPFlB7qFzZZ6g" http://localhost:9084/api/account | xargs -I{} echo {}

#Get Password Rules

curl --verbose -X GET -H "Authorization: Bearer ${access_token}" -H "Content-Type: application/json" "http://localhost:9084/api/account/user/password/rules" && echo

#Logout
curl --verbose -X POST -H "Authorization: Bearer ${access_token}" -H "Content-Type: application/json" "http://localhost:9084/api/account/logout" && echo


#Login as TRUSTED CLIENT
access_token=$(curl ecds-ts:3fc3ad76ba19044c43ba498012a6f5b2@localhost:9084/oauth/token -d grant_type=client_credentials  2>/dev/null | python -c "import sys, json; print json.load(sys.stdin)['access_token']") && echo ${access_token}

#Transaction Inbox
curl --verbose -X GET -H "Authorization: Bearer ${access_token}" http://localhost:9084/api/account/transaction/inbox/67120775 && echo 

#Transaction Notify
curl --verbose -X PUT -d "[]" -H "Authorization: Bearer ${access_token}" -H "Content-Type: application/json" "http://localhost:9084/api/account/transaction/notify" && echo


#Test Server
#=============

#Authenticate
access_token=$(curl ecdsclient:7a361a9c87824855a9cfba63129730af@localhost:9084/oauth/token -d grant_type=password -d username=evil.clown -d password='6dxGZwZq' 2>/dev/null | python -c "import sys, json; print json.load(sys.stdin)['access_token']") && echo ${access_token}




Scrap:

curl ecdsclient:7a361a9c87824855a9cfba63129730af@172.17.4.251:9084/oauth/token -d grant_type=password -d username=evil.clown  -d password='6dxGZwZq'


access_token="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJub3RpZnkiXSwiZXhwIjoxNTE4NzQyNzI2LCJhdXRob3JpdGllcyI6WyJST0xFX1RSVVNURURfQ0xJRU5UIl0sImp0aSI6IjU0MTY3YjRkLTlhMDQtNDczOC1hYWU2LWVkYTYyYmIwNjY4MiIsImNsaWVudF9pZCI6ImVjZHMtdHMifQ.Vt-c7Tz-wpRN7gyjUy0HhNfRM4CuxGVPyDV-VoDq0AcyOkZcPXObg2uGFvMDYKFwcQJP7gHmKnuUck1KoPV26Q7UzXn4B_bgaoqs76K72nib1XZqrCTUrqX_bKeGrSw1ClnFNXNaeBOWH4vFXuFOvZ2Wjyc3mNlCQrl0CE0B78UnyGg_fhzGLwT5vEdmb2LhhKznCA20R6ZySvi_qs2SGsjJZrYcM3Y49CFZoqd5CY02HDK0MXdzJE_r7nNrFQcJGJIanJIr4tKUI0Xx_kvJKUPxbens0DcD-HxfZ39u64XLv1-m1Qx_DFtfrn8m5kVYSK8RIl4GjbLS9B1AiRNJWw"
