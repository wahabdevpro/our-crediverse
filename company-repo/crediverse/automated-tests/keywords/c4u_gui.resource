*** Settings ***
Library  SeleniumLibrary
Resource    ussd.resource

*** Variables ***
${C4U_URL}      http://127.0.0.1:12082
${BROWSER}        chrome
${USERNAME}       supplier
${PASSWORD}       M@@vC@rEcd$777!
*** Keywords ***
Open C4U Gui Login Page
    Open Browser    ${C4U_URL}    ${BROWSER}
    Set Selenium Timeout    60
    Maximize Browser Window
    Title Should Be    Credit4U login

Input Username C4U
    [Arguments]    ${username}
    Input Text    xpath://body/div[1]/form[1]/input[1]    ${username}

Input Password C4U
    [Arguments]    ${password}
    Input Text    xpath://body/div[1]/form[1]/input[2]    ${password}

Submit Credentials C4U
    [Documentation]     Clicks on sign in button on the Login Screen for admin GUI
    Click Button    xpath://button[contains(text(),'Sign in')]


Login To C4U Gui
    [Documentation]      Logs in to the C4U GUI
    ...     takes username and password as an argument.
    [Arguments]     ${username}     ${password}
    Input Username C4U    ${username}
    Input Password C4U    ${password}
    Submit Credentials
    Wait Until Element Is Visible   xpath://h3[contains(text(),'My Details')]

Logout From C4U Gui
    [Documentation]     Logs out from the C4U GUI, user should already be logged in
    Click Element  xpath://span[@id='myname']
    Wait Until Element Is Visible    xpath://a[contains(text(),'Logout')]
    Click Element    xpath://a[contains(text(),'Logout')]
    Wait Until Element Is Visible    xpath://body/div[1]/form[1]/legend[1]/center[1]/h1[1]
    Title Should Be    Credit4U login

Enable sending SMS via SMSQ
    [Documentation]      Enable's sending SMSes via SMSQ
    ...     takes username password and smsc url as an argument
    [Arguments]      ${username}     ${password}       ${SMSC_URL_ENABLE}
    Open C4U Gui Login Page
    Login To C4U Gui      ${username}      ${password}
    Wait Until Element Is Visible    xpath://h3[contains(text(),'My Details')]
    Wait Until Element Is Visible    xpath://h3[@id='serviceconfig']
    Click Element    xpath://body/div[@id='main_content']/div[@id='serviceconfigavail']/h5[1]/a[1]
    Wait Until Element Is Visible    xpath://div[contains(text(),'Technical Settings')]
    Wait Until Element Is Visible    xpath://a[@id='-3662775870768294396']
    Double Click Element    xpath://a[@id='-3662775870768294396']
    Wait Until Element Is Visible    xpath://a[@id='-3662775870768294396_5361420027196137936']
    Double Click Element    xpath://a[@id='-3662775870768294396_5361420027196137936']
    Wait Until Element Is Visible    xpath://label[contains(text(),'Smsc Name')]
    Double Click Element     xpath://input[@id='SmscUrl']
    Input Text    xpath://input[@id='SmscUrl']    ${SMSC_URL_ENABLE}
    Wait Until Element Is Visible    xpath://label[contains(text(),'Smsc Binding')]
    Select From List By Value    xpath://select[@id='SmscBinding']     NONE
   # Wait Until Element Is Visible    xpath://label[contains(text(),'Password')]
   # Input Text    xpath://input[@id='Password']    hxcevd1
   # Wait Until Element Is Visible    xpath://label[contains(text(),'System ID')]
   # Input Text    xpath://input[@id='SystemID']    hxcevd1
    Click Button    xpath://button[@id='submit_config']

Disable sending SMS via SMSQ
    [Documentation]      Disable sending SMSes via SMSQ
    ...     takes username password and smsc url as an argument
    [Arguments]      ${username}     ${password}       ${SMSC_URL_DISABLE}
    Open C4U Gui Login Page
    Login To C4U Gui      ${username}      ${password}
    Wait Until Element Is Visible    xpath://h3[contains(text(),'My Details')]
    Wait Until Element Is Visible    xpath://h3[@id='serviceconfig']
    Click Element    xpath://body/div[@id='main_content']/div[@id='serviceconfigavail']/h5[1]/a[1]
    Wait Until Element Is Visible    xpath://div[contains(text(),'Technical Settings')]
    Wait Until Element Is Visible    xpath://a[@id='-3662775870768294396']
    Double Click Element    xpath://a[@id='-3662775870768294396']
    Wait Until Element Is Visible    xpath://a[@id='-3662775870768294396_5361420027196137936']
    Double Click Element    xpath://a[@id='-3662775870768294396_5361420027196137936']
    Wait Until Element Is Visible    xpath://label[contains(text(),'Smsc Name')]
    Double Click Element     xpath://input[@id='SmscUrl']
    Input Text    xpath://input[@id='SmscUrl']    ${SMSC_URL_DISABLE}
    Wait Until Element Is Visible    xpath://label[contains(text(),'Smsc Binding')]
    Select From List By Value    xpath://select[@id='SmscBinding']     TRANSCEIVER
   # Wait Until Element Is Visible    xpath://label[contains(text(),'Password')]
   # Input Text    xpath://input[@id='Password']    hxcevd1
   # Wait Until Element Is Visible    xpath://label[contains(text(),'System ID')]
   # Input Text    xpath://input[@id='SystemID']    hxcevd1
    Click Button    xpath://button[@id='submit_config']