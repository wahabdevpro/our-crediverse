*** Settings ***
Resource    ../keywords/ussd.resource
Resource    ../keywords/gui.resource
Resource    ../keywords/mysql.resource
Resource    ../keywords/api.resource
Suite Setup     Setup For The Suite
Suite Teardown      Teardown For The Suite

*** Variables ***

${USSD}=     413
${PIN}=  00000

*** Keywords ***
Setup For The Suite
    Start Airsim
    Connect To OLTP DB

Teardown For The Suite
    Disconnect From Database

*** Test Cases ***
Get account last transaction via api
    [Documentation]     Get account last transaction via api
    ...     USSD confirmation is disabled for the A agent
    ${api_session}=     Create Session For API      ${ECABINE_API_USER_USERNAME}      ${ECABINE_API_USER_PASSWORD}
    Get the account's last Transaction    ${api_session}

Get particulars of authenticated user
    [Documentation]     Get account last transaction via api
    ...     USSD confirmation is disabled for the A agent
    ${api_session}=     Create Session For API      ${ECABINE_API_USER_USERNAME}      ${ECABINE_API_USER_PASSWORD}
    Get particulars of authenticated user    ${api_session}

Update the authenticated user's particulars
        [Documentation]     Get account last transaction via api
    ...     Update user's particulars
    ${title}=   Set Variable    Mr
    ${firstName}=   Set Variable    eCabine
    ${surname}=     Set Variable    API
    ${initials}=    Set Variable    RA
    ${language}=    Set Variable    EN
    ${email}=       Set Variable    bushra.gul@concurrent.systems
    ${mobileNumber}=    Set Variable        9000000001
    ${api_session}=     Create Session For API      ${ECABINE_API_USER_USERNAME}      ${ECABINE_API_USER_PASSWORD}
    Update the authenticated user's particulars    ${api_session}  ${title}    ${firstName}    ${surname}  ${initials}     ${language}     ${email}      ${mobileNumber}

Query your account details via API
    [Documentation]     Query your account details via api
    ...     query account detials
    ${api_session}=     Create Session For API      ${ECABINE_API_USER_USERNAME}      ${ECABINE_API_USER_PASSWORD}
    Query your account details via API    ${api_session}

Update the account detials via API
        [Documentation]     update accont detials via api
    ...     Update user's particulars
    ${title}=   Set Variable    AgentAll
    ${firstName}=   Set Variable    SODITEL DIVO
    ${surname}=     Set Variable    eCab_0142307792
    ${initials}=    Set Variable    SOD
    ${language}=    Set Variable    EN
    ${email}=       Set Variable    bushra.gul@concurrent.systems
    ${altPhoneNumber}=    Set Variable        9000000001
    ${api_session}=     Create Session For API      ${ECABINE_API_USER_USERNAME}      ${ECABINE_API_USER_PASSWORD}
    Update the authenticated user's particulars    ${api_session}  ${title}    ${firstName}    ${surname}  ${initials}     ${language}     ${email}      ${altPhoneNumber}

Get the account balance information via API
    [Documentation]     Get account balance information via api
    ...     Get account balance information via api
    ${api_session}=     Create Session For API      ${ECABINE_API_USER_USERNAME}      ${ECABINE_API_USER_PASSWORD}
    Get the account balance information    ${api_session}

Failed to get access token via api when Password is invalid
    [Documentation]     Failed to get access token via api when Password is invalid
    ...     USSD confirmation is disabled for the agent and the pin entered in invalid
    ${access_token}=     Get Access Token      ${ECABINE_API_USER_USERNAME}      ${INVALID_PASSWORD}      400
    Should Be Equal As Strings    ${access_token}   None


Failed to get access token three times via api when Password is invalid
    [Documentation]     Failed to get access token via api when Password is invalid
    ...    Failed to get access token via api when Password is invalid
    ${attempts}=    Get Number of Attempts For Users        ${LOCKED_API_USER_USERNAME}
    Should Not Be Equal As Numbers    ${attempts}    -1

    ${access_token}=     Get Access Token      ${LOCKED_API_USER_USERNAME}      ${INVALID_PASSWORD}      400
    Should Be Equal As Strings    ${access_token}   None
    ${access_token}=     Get Access Token      ${LOCKED_API_USER_USERNAME}      ${INVALID_PASSWORD}      400
    Should Be Equal As Strings    ${access_token}   None
    ${access_token}=     Get Access Token      ${LOCKED_API_USER_USERNAME}      ${INVALID_PASSWORD}      400
    Should Be Equal As Strings    ${access_token}   None
    ${access_token}=     Get Access Token      ${LOCKED_API_USER_USERNAME}      ${LOCKED_API_USER_PASSWORD}      400
    Should Be Equal As Strings    ${access_token}   None

    ${attempts}=    Get Number of Attempts For Users        ${LOCKED_API_USER_USERNAME}
    Should Be Equal As Strings   ${attempts}    -1
