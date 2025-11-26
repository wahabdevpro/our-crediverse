*** Settings ***
Library  SeleniumLibrary
Library    XML
Resource    ussd.resource
Resource    mysql.resource
*** Variables ***
${ADMIN_URL}      http://127.0.0.1:12080/login
${BROWSER}        headlesschrome
${SUPPLIER}       supplier
${SUPP_PASS}       M@@vC@rEcd$777!
${ROBOT1_USER}       robot1
${ROBOT1_PASS}      1!GgiV4B
${ROBOT1_MSISDN}=   6000000001
${ROBOT2_USER}       robot2
${ROBOT2_PASS}      IlSwEx#x
${ROBOT2_MSISDN}=   6000000002
*** Keywords ***
Open Admin Gui Login Page
    Open Browser    ${ADMIN_URL}    ${BROWSER}
    Set Selenium Timeout    60
    Maximize Browser Window
    Wait For Page Title     ECDS Login  200     1s
    Title Should Be    ECDS Login

Input Username
    [Arguments]    ${username}
    Input Text    id:user    ${username}

Input Password
    [Arguments]    ${password}
    Input Text    id:pass    ${password}

Submit Credentials
    [Documentation]     Clicks on sign in button on the Login Screen for admin GUI
    Click Button    xpath://button[text()="Sign in"]

Login To Admin Gui As Supplier
    [Documentation]     Logs in to the GUI as a supplier and waits until Agent Management page is loaded
    ...     takes password as an argument.
    [Arguments]     ${password}
    Input Username    supplier
    Input Password    ${password}
    Submit Credentials
    Wait For Successful GUI Loaded After Login  5     5s
    Wait Until Element Is Visible   xpath://h1[contains(text(),'Agent Account Management')]

Login To Admin Gui
    [Documentation]     Logs in to the GUI as non-supplier user and waits until Agent Management page is loaded
    ...     takes username and password as an argument.
    [Arguments]     ${username}     ${password}
    Connect To OLTP DB
    Input Username    ${username}
    Input Password    ${password}
    Submit Credentials
    Wait Until Element Is Visible   xpath://p[contains(text(),'Check your cell phone for SMS containing PIN')]
    ${otp}=     Get Otp From Sms
    Input Text      xpath://input[@id='pininput']   ${otp}
    Click Button    xpath://button[text()="Validate PIN"]
    Wait For Successful GUI Loaded After Login  5     5s
    Wait Until Element Is Visible   xpath://h1[contains(text(),'Agent Account Management')]

Logout From Admin Gui
    [Documentation]     Logs out from the GUI, user should already be logged in
    Wait Until Element Is Visible    xpath://header/nav[1]/div[1]/ul[1]/li[1]/a[1]/img[1]
    Click Element  xpath://header/nav[1]/div[1]/ul[1]/li[1]/a[1]/img[1]
    Wait Until Element Is Visible    xpath://header/nav[1]/div[1]/ul[1]/li[1]/ul[1]/li[1]/a[1]
    Click Element    xpath://header/nav[1]/div[1]/ul[1]/li[1]/ul[1]/li[1]/a[1]
    Wait Until Element Is Visible    xpath://body/div[1]/div[1]/form[1]/div[1]/div[3]/div[1]/div[1]
    Title Should Be    ECDS Login

Navigate To Item On Left Menu
    [Documentation]     Navigates to the Menu item in the Left Menu
    ...     takes the item name as an argument.
    ...     valid options are [Agent Accounts, My Tasks, Transaction, Transfer Rules, Tiers, Groups, Service Class, Promotion & Reward, Batch Processing, Analytics, Audit Log]
    [Arguments]     ${menu_item}
    Scroll Element Into View    xpath://span[contains(text(),'${menu_item}')]
    Click Element   xpath://span[contains(text(),'${menu_item}')]
    ${look_for}=    Set Variable If
    ...     "${menu_item}" == "Agent Accounts"  Agent Account Management
    ...      "${menu_item}" == "My Tasks"   Task Management
    ...      "${menu_item}" == "Transaction"   Transaction Management
    ...      "${menu_item}" == "Transfer Rules"   Transfer Rule Management
    ...      "${menu_item}" == "Tiers"   Tier Management
    ...      "${menu_item}" == "Groups"   Group Management
    ...      "${menu_item}" == "Service Class"   Service Class Management
    ...      "${menu_item}" == "Promotion & Reward"   Promotions Management
    ...      "${menu_item}" == "Batch Processing"   Batch Processing
    ...      "${menu_item}" == "Analytics"   Analytics
    ...      "${menu_item}" == "Audit Log"   Audit Log
    Wait Until Element Is Visible   xpath://h1[contains(text(),'${look_for}')]

Navigate To Dropdown Item On Left Menu
    [Documentation]     Navigates to the Menu item in the Left Menu
    ...     takes the item name and the corresponding dropdown item as an arguments.
    [Arguments]     ${menu_item}    ${dropdown_item}
    Scroll Element Into View    xpath://span[contains(text(),'${menu_item}')]
    Click Element   xpath://span[contains(text(),'${menu_item}')]

    ${look_for}=    Set Variable If
    ...     "${menu_item}" == "Location Information"  //span[contains(text(),'Cell Groups')]
    ...      "${menu_item}" == "Reports"   //a[contains(text(),'Retailers Performance')]
    ...      "${menu_item}" == "User Access"   //a[contains(text(),'Users')]
    ...      "${menu_item}" == "Configuration"   //a[contains(text(),'Adjudication')]

    Mouse Over      xpath:${look_for}
    Scroll Element Into View    xpath:${look_for}
    Wait Until Element Is Visible   xpath:${look_for}

    ${click_on}=    Set Variable If
    ...     "${menu_item}" == "Location Information"  //span[contains(text(),'${dropdown_item}')]
    ...      "${menu_item}" == "Reports"   //a[contains(text(),'${dropdown_item}')]
    ...      "${menu_item}" == "User Access"   //a[contains(text(),'${dropdown_item}')]
    ...      "${menu_item}" == "Configuration"   //a[contains(text(),'${dropdown_item}')]

    Scroll Element Into View    xpath:${click_on}
    Click Element   xpath:${click_on}
    Scroll Element Into View    xpath://span[contains(text(),'${menu_item}')]
    Click Element   xpath://span[contains(text(),'${menu_item}')]
    Wait Until Element Is Not Visible   xpath:${click_on}


Create Agent Via GUI
    [Documentation]     Navigates to Add Agent page and creates a new agent
    ...     Account state is set to active
    ${account_number}=  Generate Random String      61000000    62000000
    [Arguments]     ${agent_msisdn}     ${tier_name}    ${state}=Active
    ...     ${group}=NONE   ${service_class}=NONE   ${owner_agent}=NONE     ${upstream_agent}=NONE
    ...     ${area}=NONE    ${warning_threshold}=NONE
    Navigate To Item On Left Menu   Agent Accounts
    Wait Until Element Is Visible    xpath://div[@class='col-lg-9 col-md-8 col-sm-7 right dtButtonBar']//button[@class='btn btn-primary createAgentButton'][contains(text(),'Add Agent')]
    Click Element    xpath://div[@class='col-lg-9 col-md-8 col-sm-7 right dtButtonBar']//button[@class='btn btn-primary createAgentButton'][contains(text(),'Add Agent')]
    Wait Until Element Is Visible    xpath://h4[contains(text(),'Add New Agent Account')]
    Input Text    xpath://input[@id='title']    Mr
    Input Text    xpath://input[@id='email']    ${account_number}@concurrent.systems
    Input Text    xpath://div[@class='col-xs-8 col-sm-8']//input[@id='firstName']    ${tier_name}
    Input Text    xpath://div[@class='col-xs-8 col-sm-8']//input[@id='surname']    ${agent_msisdn}
    Input Text    xpath://div[@class='col-xs-8 col-sm-8']//input[@id='mobileNumber']    ${agent_msisdn}
    Click Element    xpath://div[@class='col-xs-8 col-sm-8']//span[@id='select2-tierID-container']
    Wait Until Element Is Visible    xpath://input[@role='textbox']
    Input Text      xpath://input[@role='textbox']       ${tier_name}
    Wait Until Element Is Visible    xpath://li[normalize-space()='${tier_name}']
    Click Element    xpath://li[normalize-space()='${tier_name}']
    Input Text    xpath://div[@class='col-xs-8 col-sm-8']//input[@id='accountNumber']    ${account_number}
    Select From List By Label    xpath://select[@id='authenticationMethod']     Pin
    Select From List By Label    xpath://select[@id='language']   English
    Select From List By Label    xpath://select[@id='gender']   Male
    Select From List By Label    xpath://div[@class='col-xs-8 col-sm-8']//select[@id='state']      ${state}
    Wait Until Element Is Visible    xpath://label[contains(text(),'USSD')]
    Click Element    xpath://label[contains(text(),'USSD')]
    Click Element    xpath://span[@id='select2-roleID-container']
    Wait Until Element Is Visible    xpath://input[@role='textbox']
    Input Text    xpath://input[@role='textbox']    AgentAll
    Wait Until Element Is Visible    xpath://li[contains(text(),'AgentAll')]
    Click Element    xpath://li[contains(text(),'AgentAll')]
    IF      "${group}" != "NONE"
        Click Element    xpath://div[@class='col-xs-8 col-sm-8']//span[@id='select2-groupID-container']
        Wait Until Element Is Visible    xpath://input[@role='textbox']
        Input Text      xpath://input[@role='textbox']       ${group}
        Wait Until Element Is Visible    xpath://li[normalize-space()='${group}']
        Click Element    xpath://li[normalize-space()='${group}']
    END
    IF      "${service_class}" != "NONE"
        Click Element    xpath://div[@class='col-xs-8 col-sm-8']//span[@id='select2-serviceClassID-container']
        Wait Until Element Is Visible    xpath://input[@role='textbox']
        Input Text      xpath://input[@role='textbox']       ${service_class}
        Wait Until Element Is Visible    xpath://li[normalize-space()='${service_class}']
        Click Element    xpath://li[normalize-space()='${service_class}']
    END
    IF      "${owner_agent}" != "NONE"
        Click Element    xpath://div[@class='col-xs-8 col-sm-8']//span[@id='select2-ownerAgentID-container']
        Wait Until Element Is Visible    xpath://input[@role='textbox']
        Input Text      xpath://input[@role='textbox']       ${owner_agent}
        Wait Until Element Is Visible    xpath://li[contains(text(),'${owner_agent}')]
        Click Element    xpath://li[contains(text(),'${owner_agent}')]
    END
    IF      "${upstream_agent}" != "NONE"
        Click Element    xpath://span[@id='select2-supplierAgentID-container']
        Wait Until Element Is Visible    xpath://input[@role='textbox']
        Input Text      xpath://input[@role='textbox']       ${upstream_agent}
        Wait Until Element Is Visible    xpath://li[contains(text(),'${upstream_agent}')]
        Click Element    xpath://li[contains(text(),'${upstream_agent}')]
    END
    IF      "${area}" != "NONE"
        Click Element    xpath://div[@class='col-xs-8 col-sm-8']//span[@id='select2-areaID-container']
        Wait Until Element Is Visible    xpath://input[@role='textbox']
        Input Text      xpath://input[@role='textbox']       ${area}
        Wait Until Element Is Visible    xpath://li[contains(text(),'${area}')][1]
        Click Element    xpath://li[contains(text(),'${area}')][1]
    END
    IF      "${warning_threshold}" != "NONE"
        Input Text      xpath://input[@id='warningThreshold']       ${warning_threshold}
    END
    Scroll Element Into View    xpath://button[contains(text(),'Save')]
    Wait Until Element Is Visible    xpath://button[contains(text(),'Save')]
    Click Button    xpath://button[contains(text(),'Save')]
    Wait Until Element Is Not Visible    xpath://h4[contains(text(),'Add New Agent Account')]

Deactivate Agent Via GUI
    [Documentation]
    [Arguments]     ${agent_msisdn}
    Navigate To Item On Left Menu   Agent Accounts
    Wait Until Element Is Visible   xpath://div[@class='col-lg-9 col-md-8 col-sm-7 right dtButtonBar']//button[@role='button'][normalize-space()='Deactivate Agent']
    Click Button    xpath://div[@class='col-lg-9 col-md-8 col-sm-7 right dtButtonBar']//button[@role='button'][normalize-space()='Deactivate Agent']
    Wait Until Element Is Visible    xpath://button[normalize-space()='Deactivate']
    Input Text    xpath://input[@id='quickDeactivateMsisdnTxt']    ${agent_msisdn}
    Wait Until Element Is Visible    xpath://button[normalize-space()='Deactivate']
    Click Button    xpath://button[normalize-space()='Deactivate']
    Sleep    0.5
    Wait Until Element Is Not Visible    xpath://h4[contains(text(),'Deactivate Agent')]

Suspend Agent Via GUI
    [Documentation]
    [Arguments]     ${agent_msisdn}
    View Agent Profile By Quick Search    ${agent_msisdn}
    Wait Until Element Is Visible    xpath://button[contains(text(),'Suspend')]
    Click Button    xpath://button[contains(text(),'Suspend')]
    Wait Until Element Is Visible    xpath://button[@class='btn actionButton btn-warning']
    Click Element    xpath://button[@class='btn actionButton btn-warning']
    Sleep    0.5
    Wait Until Element Is Not Visible    xpath://h4[contains(text(),'Suspend agent account?')]

View Agent Profile By Quick Search
    [Documentation]     Opens the active/suspended agent's profile on the GUI by quick search
    ...     takes the msisdn as an argument.
    [Arguments]     ${msisdn}
    ${status}=      Set Variable    ${False}
    ${max_iterations}=  Set Variable    3
    WHILE  not ${status} and ${max_iterations} > 0
      Navigate To Item On Left Menu   Agent Accounts
      Wait Until Element Is Visible   xpath://input[@type='search']
      Click Element    xpath://input[@type='search']
      ${status}=    Run Keyword And Return Status   Input Text      xpath://input[@type='search']   ${msisdn}
      Log   ${status}
          IF    ${status}
              Press Keys    xpath://input[@type='search']     ENTER
              Sleep     1
              Wait Until Element Is Visible   xpath://td[normalize-space()='${msisdn}']
              Wait Until Element Is Visible   xpath://tr[@class='odd']//td[contains(@class,'right all')]//div[@class='btn-group actionDropdown']//a[@class='btn btn-primary btn-xs routerlink'][contains(text(),'View ...')]
              ${status}=    Run Keyword And Return Status    Click Element    xpath://tr[@class='odd']//td[contains(@class,'right all')]//div[@class='btn-group actionDropdown']//a[@class='btn btn-primary btn-xs routerlink'][contains(text(),'View ...')]
              IF    not ${status}
                    CONTINUE
              END
          END
      ${max_iterations}    Evaluate    ${max_iterations}-1
    END
    Wait Until Element Is Visible   xpath://h1[contains(text(),'Agent Account')]
    Wait Until Element Is Visible   xpath://div/label[text()='Balance']//following-sibling::h2

Get Agent Balance From Agent Profile
    [Documentation]     Returns the balance of the Agent from the GUI. Browser must already be on the Agent Profile
    ...     takes the balance type as an argument and by default returns Balance.
    [Arguments]     ${balance_type}=Balance
    ${value}=   Get Text    xpath://div/label[text()='${balance_type}']//following-sibling::h2
    ${value}=   Replace String      ${value}    ,   ${EMPTY}
    ${value}=   Convert To Number   ${value}    2
    [Return]    ${value}
    sleep   500ms

Balance Check B Party
    [Documentation]     Ensure that the Balance of the B party are as expected after the transaction
    ...     arguments are Balance Before, Balance After, Trade Bonus Percentage(e.g. 5) and commulative bonus pct(e.g 9.998).
    [Arguments]     ${balance_before}   ${balance_after}      ${trans_amount}      ${trade_bonus}  ${commulative_bonus}
    ${bal_diff}=    Evaluate    ${balance_after} - ${balance_before}
    ${expected_bonus}=   Evaluate   ${trans_amount} * ${trade_bonus} * 0.01
    ${expected_diff}=   Evaluate    ${trans_amount} + ${expected_bonus}

    ${bal_diff}=    Convert To Number   ${bal_diff}    0
    ${expected_diff}=   Convert To Number   ${expected_diff}    0
    Log     Balance Before=${balance_before}, Balance After=${balance_after}, Transfered Amount=${expected_diff}
    Should Be Equal As Strings    ${bal_diff}     ${expected_diff}

Bonus Check B Party
    [Documentation]     Ensure that the Balance of the B party are as expected after the transaction
    ...     arguments are Bonus Before, Bonus After, Trade Bonus Percentage(e.g. 5) and commulative bonus pct(e.g 9.998).
    [Arguments]     ${balance_before}   ${balance_after}      ${trans_amount}      ${trade_bonus}  ${commulative_bonus}
    ${bal_diff}=    Evaluate    ${balance_after} - ${balance_before}
    ${expected_bonus}=   Evaluate    ${trans_amount} * ${commulative_bonus} * 0.01
    ${expected_trade_bonus}=    Evaluate    ${trans_amount} * ${trade_bonus} * 0.01
    ${expected_diff}=   Evaluate    ${expected_bonus} - ${expected_trade_bonus}

    ${bal_diff}=    Convert To Number   ${bal_diff}    0
    ${bal_diff}=   Evaluate    round(${bal_diff},0)
    ${expected_diff}=   Convert To Number   ${expected_diff}    0
    ${expected_diff}=   Evaluate    round(${expected_diff},0)
    Log     Bonus Before=${balance_before}, Bonus After=${balance_after}, Transfered Bonus=${expected_diff}
    Should Be Equal As Strings    ${bal_diff}     ${expected_diff}


Balance Check A Party
    [Documentation]     Ensure that the Balance of the B party are as expected after the transaction
    ...     arguments are Balance Before, Balance After, Trade Bonus Percentage(e.g. 5) and commulative bonus pct(e.g 9.998).
    [Arguments]     ${balance_before}   ${balance_after}      ${trans_amount}      ${trade_bonus}  ${commulative_bonus}
    ${bal_diff}=    Evaluate    ${balance_before} - ${balance_after}

    ${bal_diff}=    Convert To Number   ${bal_diff}    0
    ${trans_amount}=    Convert To Number   ${trans_amount}    0
    Log     Balance Before=${balance_before}, Balance After=${balance_after}, Deducted Amount=${bal_diff}
    Should Be Equal As Strings    ${bal_diff}     ${trans_amount}

Bonus Check A Party
    [Documentation]     Ensure that the Bonus of the B party are as expected after the transaction
    ...     arguments are Bonus Before, Bonus After, Trade Bonus Percentage(e.g. 5) and commulative bonus pct(e.g 9.998).
    [Arguments]     ${balance_before}   ${balance_after}      ${trans_amount}      ${trade_bonus}  ${commulative_bonus}
    ${bal_diff}=    Evaluate    ${balance_before} - ${balance_after}
    ${expected_bonus}=   Evaluate    ${commulative_bonus} * 0.01
    ${expected_diff}=   Evaluate    (${trans_amount} * ${expected_bonus})

    ${bal_diff}=    Convert To Number   ${bal_diff}    0
    ${bal_diff}=   Evaluate    round(${bal_diff},0)
    ${expected_diff}=   Convert To Number   ${expected_diff}    0
    ${expected_diff}=   Evaluate    round(${expected_diff},0)
    Log     Bonus Before=${balance_before}, Bonus After=${balance_after}, Deducted Bonus=${expected_diff}
    Should Be Equal As Strings    ${bal_diff}     ${expected_diff}

Enter Coauth Username And Password
    [Documentation]     Enters the Username and password and authorizes the transaction
    ...     User has already clicked on Authorize button
    ...     takes the username and password as arguments.
    [Arguments]     ${username}      ${password}
    Wait Until Element Is Visible   xpath://p[@class='login-box-msg loginheading']
    Input Text      xpath://input[@id='user']   ${username}
    Input Text      xpath://input[@id='pass']   ${password}
    Click Button    xpath://button[@class='btn btn-primary btn-flat signin col-sm-3 pull-right']

Coauthorize
    [Documentation]     Enters the Username and password and authorizes the transaction
    ...     User has already clicked on Authorize button
    ...     takes the username and password as arguments.
    ...     Optional argument otp, it a value is given it uses that value as otp instead of getting from sms
    ...     Optional argument suppier, if True, the keyword expects a failure for coauth
    [Arguments]     ${username}      ${password}    ${otp}=False    ${supplier}=False
    Enter Coauth Username And Password     ${username}      ${password}
    Wait Until Element Is Visible   xpath://p[contains(text(),'Check your cell phone for SMS containing PIN')]
    IF      ${otp} == False and ${supplier} == False
        ${otp}=     Get Otp From Sms
        Input Text      xpath://input[@id='pininput']       ${otp}
        Click Button    xpath://button[@class='btn btn-primary btn-flat pinin col-sm-3 pull-right']
        Wait Until Element Is Visible   xpath://span[@class='logo-lg logo-image']
        Wait Until Element Is Not Visible   xpath://div[@class='authorisation-box']
    ELSE IF     ${otp} != False and ${supplier}==False
        Input Text      xpath://input[@id='pininput']       ${otp}
        Click Button    xpath://button[@class='btn btn-primary btn-flat pinin col-sm-3 pull-right']
        Wait Until Element Is Visible   xpath://span[@class='errortxt errormsg']
        Click Button    xpath://div[@class='col-sm-offset-2 col-sm-10']//button[@type='button'][contains(text(),'Cancel')]
    ELSE IF     ${otp}==False and ${supplier}==True
        ${otp}=     Get Otp From Sms
        Input Text      xpath://input[@id='pininput']       ${otp}
        Click Button    xpath://button[@class='btn btn-primary btn-flat pinin col-sm-3 pull-right']
        Wait Until Element Is Visible   xpath://span[@class='errortxt errormsg']
        Click Button    xpath://div[@class='col-sm-offset-2 col-sm-10']//button[@type='button'][contains(text(),'Cancel')]
    END


Coauthorization Failed Before OTP
    [Documentation]     Enters the Username and password and authorizes the transaction
    ...     User has already clicked on Authorize button
    ...     takes the username and password as arguments.
    [Arguments]     ${username}      ${password}
    Enter Coauth Username And Password     ${username}      ${password}
    Wait Until Element Is Visible   xpath://span[@class='errortxt errormsg']
    Click Button    xpath://div[@class='col-sm-offset-2 col-sm-10']//button[@type='button'][contains(text(),'Cancel')]


Replenish Root Account
    [Documentation]     Navigates to Agent Management and Replenishes ROOT agent account by using quick search
    ...     Must be Logged in as non-supplier user
    ...     takes the amount to be replenished as an argument. Optional bonus argument for custom bonus value
    [Arguments]     ${amount}   ${bonus}=False
    Navigate To Item On Left Menu   Agent Accounts
    View Agent Profile By Quick Search    ROOT
    Wait Until Element Is Visible   xpath://button[contains(text(),'Replenish')]
    Click Button    xpath://button[contains(text(),'Replenish')]
    Wait Until Element Is Visible   xpath://h4[contains(text(),'Replenish Root Account')]
    Input Text      xpath://input[@id='amount']     ${amount}
    Sleep   1
    Wait Until Element Is Not Visible   xpath://button[contains(text(),'Recommend')][@disabled='disabled']
    Click Button       xpath://button[contains(text(),'Recommend')]
    Wait Until Element Is Not Visible   xpath://button[contains(text(),'Authorize')][@disabled='disabled']
    Sleep   1
    IF      ${bonus} != False
        Click Element    xpath://label[@id='allowChangeBonusLabel']
        Sleep   1
        Input Text      xpath://input[@id='bonusProvision']     ${bonus}
        Click Element    xpath://div[@class='replenishdetails']//form[@class='form-horizontal']
    END
    Click Button       xpath://button[contains(text(),'Authorize')]
    Wait Until Element Is Visible   xpath://p[@class='login-box-msg loginheading']


Transfer From Root To Agent
    [Documentation]     Navigates to Agent Management and transfer from ROOT agent account to another agent by using quick search
    ...     Must be Logged in as non-supplier user
    ...     takes the msisdn and the amount to be transfer as arguments.
    [Arguments]     ${msisdn}   ${amount}   ${coauth}=immediate
    Navigate To Item On Left Menu   Agent Accounts
    View Agent Profile By Quick Search    ${msisdn}
    Wait Until Element Is Visible   xpath://button[contains(text(),'Transfer')]
    Click Button    xpath://button[contains(text(),'Transfer')]
    Wait Until Element Is Visible   xpath://label[contains(text(),'Authorisation')]
    Wait Until Element Is Visible   xpath://input[@id='amount']
    Input Text      xpath://input[@id='amount']     ${amount}
    Sleep   1
    Wait Until Element Is Not Visible   xpath://button[contains(text(),'Authorize')][@disabled='disabled']
    IF      "${coauth}" == "immediate"
        Click Button       xpath://button[contains(text(),'Authorize')]
        Wait Until Element Is Visible   xpath://p[@class='login-box-msg loginheading'][contains(text(),'Co-Authorization')]
    ELSE IF     "${coauth}" == "request"
        Click Button    xpath://button[contains(text(),'Request')]
        Click Button    xpath://button[contains(text(),'Authorize')]
    ELSE
        Log     "Incorrect coauth type. Use immediate or request.
    END

Quick Search Transaction By Agent MSISDN
    [Documentation]     Search the Transaction on the transaction page by quick search
    ...     takes the msisdn as an argument.
    [Arguments]     ${agent}
    ${status}=      Set Variable    ${False}
    ${max_iterations}=  Set Variable    3
    WHILE  not ${status} and ${max_iterations} > 0
      Navigate To Item On Left Menu   Transaction
      Wait Until Element Is Visible   xpath://input[@type='search']
      ${status}=    Run Keyword And Return Status   Input Text      xpath://input[@type='search']   ${agent}
      Log   ${status}
          IF    not ${status}
              Sleep    0.25
          END
      ${max_iterations}    Evaluate    ${max_iterations}-1
    END

Immediate Full Reversal
    [Documentation]     Opens the Transactions page on the GUI by quick search
    ...     takes the msisdn as an argument.
    [Arguments]  ${ref}      ${reason}      ${agent}
    Quick Search Transaction By Agent MSISDN    ${agent}
    Wait Until Element Is Visible   xpath://a[contains(text(),'${ref}')]
#    Click Element   xpath://a[contains(text(),'${ref}')]
    Wait For Element And Interact      xpath://a[contains(text(),'${ref}')]
    Wait Until Element Is Visible   xpath://h1[contains(text(),'Transaction Record View')]
    Wait Until Element Is Visible   xpath://button[contains(text(),'Reverse Transaction')]
    Click Button    xpath://button[contains(text(),'Reverse Transaction')]
    Sleep   1
    Wait Until Element Is Visible   xpath://input[@id='amount_full']
    Click Element   xpath://input[@id='amount_full']
    Wait Until Element Is Visible   xpath://label[contains(text(),'Reason')]
    Sleep   1
    Click Element   xpath://input[@id='reason']
    Input Text      xpath://input[@id='reason']   ${reason}
    Click Button    xpath://body/div[@id='templates']/div[1]/div[7]/div[1]/div[1]/div[3]/div[1]/div[1]/button[1]
    Wait Until Element Is Visible    xpath://p[contains(text(),'Co-Authorization')]

Immediate Partial Reversal
    [Documentation]     Opens the Transactions page on the GUI by quick search
    ...     takes the msisdn as an argument.
    [Arguments]  ${ref}      ${reason}  ${partial_amount}  ${agent}
    Quick Search Transaction By Agent MSISDN    ${agent}
    Wait Until Element Is Visible   xpath://a[contains(text(),'${ref}')]
#    Click Element   xpath://a[contains(text(),'${ref}')]
    Wait For Element And Interact      xpath://a[contains(text(),'${ref}')]
    Wait Until Element Is Visible   xpath://h1[contains(text(),'Transaction Record View')]
    Wait Until Element Is Visible   xpath://button[contains(text(),'Reverse Transaction')]
    Click Button    xpath://button[contains(text(),'Reverse Transaction')]
    Sleep   1
    Wait Until Element Is Visible   xpath://input[@id='amount_partial']
    Click Element   xpath://input[@id='amount_partial']
    Click Element   xpath://input[@id='reverseAmount']
    Input Text      xpath://input[@id='reverseAmount']   ${partial_amount}
    Wait Until Element Is Visible   xpath://label[contains(text(),'Reason')]
    Sleep   1
    Click Element   xpath://input[@id='reason']
    Input Text      xpath://input[@id='reason']   ${reason}
    Click Button    xpath://body/div[@id='templates']/div[1]/div[7]/div[1]/div[1]/div[3]/div[1]/div[1]/button[1]
    Wait Until Element Is Visible    xpath://p[contains(text(),'Co-Authorization')]

Request Full Reversal
    [Documentation]     Opens the Transactions page on the GUI by quick search
    ...     takes the msisdn as an argument.
    [Arguments]     ${ref}      ${reason}      ${agent}
    Quick Search Transaction By Agent MSISDN    ${agent}
    Wait Until Element Is Visible   xpath://a[contains(text(),'${ref}')]
#    Click Element   xpath://a[contains(text(),'${ref}')]
    Wait For Element And Interact      xpath://a[contains(text(),'${ref}')]
    Wait Until Element Is Visible   xpath://h1[contains(text(),'Transaction Record View')]
    Wait Until Element Is Visible   xpath://button[contains(text(),'Reverse Transaction')]
    Click Button    xpath://button[contains(text(),'Reverse Transaction')]
    Sleep   0.5
    Wait Until Element Is Visible   xpath://button[contains(text(),'Request')]
    Click Element    xpath://button[contains(text(),'Request')]
    Sleep   0.5
    Wait Until Element Is Visible   xpath://input[@id='amount_full']
    Click Element   xpath://input[@id='amount_full']
    Wait Until Element Is Visible   xpath://label[contains(text(),'Reason')]
    Sleep   0.5
    Click Element   xpath://input[@id='reason']
    Input Text      xpath://input[@id='reason']   ${reason}
    Click Button    xpath://body/div[@id='templates']/div[1]/div[7]/div[1]/div[1]/div[3]/div[1]/div[1]/button[1]
    Wait Until Element Is Not Visible    xpath://body/div[@id='templates']/div[1]/div[7]/div[1]/div[1]/div[2]

Request Partial Reversal
    [Documentation]     Opens the Transactions page on the GUI by quick search
    ...     takes the msisdn as an argument.
    [Arguments]  ${ref}      ${reason}  ${partial_amount}  ${agent}
    Quick Search Transaction By Agent MSISDN    ${agent}
    Wait Until Element Is Visible   xpath://a[contains(text(),'${ref}')]
#    Click Element   xpath://a[contains(text(),'${ref}')]
    Wait For Element And Interact      xpath://a[contains(text(),'${ref}')]
    Wait Until Element Is Visible   xpath://h1[contains(text(),'Transaction Record View')]
    Wait Until Element Is Visible   xpath://button[contains(text(),'Reverse Transaction')]
    Click Button    xpath://button[contains(text(),'Reverse Transaction')]
    Sleep   1
    Wait Until Element Is Visible   xpath://button[contains(text(),'Request')]
    Click Element    xpath://button[contains(text(),'Request')]
    Sleep   1
    Wait Until Element Is Visible   xpath://input[@id='amount_partial']
    Click Element   xpath://input[@id='amount_partial']
    Click Element   xpath://input[@id='reverseAmount']
    Input Text      xpath://input[@id='reverseAmount']   ${partial_amount}
    Wait Until Element Is Visible   xpath://label[contains(text(),'Reason')]
    Sleep   1
    Click Element   xpath://input[@id='reason']
    Input Text      xpath://input[@id='reason']   ${reason}
    Click Button    xpath://body/div[@id='templates']/div[1]/div[7]/div[1]/div[1]/div[3]/div[1]/div[1]/button[1]
    Wait Until Element Is Not Visible    xpath://body/div[@id='templates']/div[1]/div[7]/div[1]/div[1]/div[2]

Approve request
   [Documentation]     Opens the approvals page
    ...     takes the msisdn as an argument.
    Navigate To Item On Left Menu   My Tasks
    Wait Until Element Is Visible   xpath://h1[contains(text(),'Task Management')]
    Wait Until Element Is Visible    xpath://body[1]/div[2]/div[1]/section[1]/div[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[2]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[1]/td[8]/div[1]/a[1]
    Click Element    xpath://body[1]/div[2]/div[1]/section[1]/div[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[2]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[1]/td[8]/div[1]/a[1]
#    Wait Until Element Is Visible     xpath://tbody/tr[1]/td[8]/div[1]/a[1]
#    Click Element    xpath://tbody/tr[1]/td[8]/div[1]/a[1]
    Wait Until Element Is Visible   xpath://body/div[@id='templates']/div[1]/div[7]/div[1]/div[1]/div[1]/div[1]/div[2]/form[1]/div[5]/div[1]/button[1]
    Click Button    xpath://body/div[@id='templates']/div[1]/div[7]/div[1]/div[1]/div[1]/div[1]/div[2]/form[1]/div[5]/div[1]/button[1]
    Wait Until Element Is Visible   xpath://input[@id='workItemOTP']
    Sleep   0.5s
    ${otp}=     Get Otp From Sms At Index   5
    Input Text   xpath://input[@id='workItemOTP']   ${otp}
    Wait Until Element Is Visible   xpath://button[contains(text(),'Approve')]
    Click Button    xpath://button[contains(text(),'Approve')]
    Wait Until Element Is Not Visible  xpath://div[@id='taskItemView']

Immediate Adjustment
    [Documentation]     performs an ajdustment
    ...     takes the amount and reason as an argument.
    [Arguments]     ${amount}   ${reason}    ${agent}
    Navigate To Item On Left Menu   Agent Accounts
    View Agent Profile By Quick Search     ${agent}
    Wait Until Element Is Visible   xpath://h1[contains(text(),'Agent Account')]
    Sleep   1
    Wait Until Element Is Visible   xpath://button[contains(text(),'Adjustment')]
    Click Button    xpath://button[contains(text(),'Adjustment')]
    Sleep   1
    Wait Until Element Is Visible   xpath://input[@id='amount']
    Input Text   xpath://input[@id='amount']  ${amount}
    Wait Until Element Is Visible   xpath://label[@id='reasonLabel']
    Sleep   1
    Click Element   xpath://input[@id='reason']
    Input Text      xpath://input[@id='reason']   ${reason}
    Click Button    xpath://button[contains(text(),'Authorize')]

Request Adjustment
    [Documentation]     performs an ajdustment
    ...     takes the amount and reason as an argument.
    [Arguments]     ${amount}   ${reason}    ${agent}
    Navigate To Item On Left Menu   Agent Accounts
    View Agent Profile By Quick Search     ${agent}
    Wait Until Element Is Visible   xpath://h1[contains(text(),'Agent Account')]
    Sleep   1
    Wait Until Element Is Visible   xpath://button[contains(text(),'Adjustment')]
    Click Button    xpath://button[contains(text(),'Adjustment')]
    Sleep   1
    Wait Until Element Is Visible    xpath://button[contains(text(),'Request')]
    Click Button    xpath://button[contains(text(),'Request')]
    Wait Until Element Is Visible   xpath://input[@id='amount']
    Input Text   xpath://input[@id='amount']  ${amount}
    Wait Until Element Is Visible   xpath://label[@id='reasonLabel']
    Sleep   1
    Click Element   xpath://input[@id='reason']
    Input Text      xpath://input[@id='reason']   ${reason}
    Click Button    xpath://button[contains(text(),'Authorize')]
    Wait Until Element Is Not Visible    xpath://body/div[@id='templates']/div[1]/div[7]/div[1]/div[1]/div[2]

Reset PIN via GUI
    [Documentation]     resets pin to default pin for an agent
    ...     changes pin to default pin
    [Arguments]  ${agent}
    Navigate To Item On Left Menu   Agent Accounts
    View Agent Profile By Quick Search     ${agent}
    Wait Until Element Is Visible   xpath://h1[contains(text(),'Agent Account')]
    Sleep   1
    Click Button    xpath://button[contains(text(),'Reset Pin')]
    Wait Until Element Is Visible    xpath://body/div[@id='templates']/div[1]/div[5]/div[1]/div[1]/div[2]
    Click Button    xpath://button[contains(text(),'Yes')]
    Wait Until Element Is Not Visible    xpath://button[contains(text(),'Yes')]
    Wait Until Element Is Visible    xpath://body/div[@id='templates']/div[1]/div[5]/div[1]/div[1]/div[2]
    Click Button    xpath://body/div[@id='templates']/div[1]/div[5]/div[1]/div[1]/div[3]/button[2]


Change Agent Pin
    [Documentation]     Set Agent to recieve a temporary pin instead of a default pin
    [Arguments]
    Wait Until Element Is Visible    xpath://span[contains(text(),'Configuration')]
    Click Element    xpath://span[contains(text(),'Configuration')]
    Wait Until Element Is Visible    xpath://body/div[2]/aside[1]/div[1]/div[1]/section[1]/ul[1]/li[16]/ul[1]/li[3]/a[1]
    Click Element    xpath://body/div[2]/aside[1]/div[1]/div[1]/section[1]/ul[1]/li[16]/ul[1]/li[15]/a[1]
    Wait Until Element Is Visible    xpath://button[contains(text(),'Edit')]
    Click Element    xpath://button[contains(text(),'Edit')]
    Wait Until Element Is Visible    //span[contains(text(),'Areas')]

Wait For Page Title
    [Arguments]   ${expected_title}     ${max_iterations}   ${delay}
    ${is_opened}    Set Variable    ${False}
    WHILE   not ${is_opened} and ${max_iterations} > 0
      ${title}    Get Title
      Log   ${title}
          IF    "${title}" == "${expected_title}"
              ${is_opened}    Set Variable    ${True}
              BREAK
          END
      Sleep    ${delay}
      Reload Page
      ${max_iterations}    Evaluate    ${max_iterations}-1
    END

    IF    ${max_iterations} == 0
       Fail   msg= The ${title} == ${expected_title} was not found.
    END

Wait For Successful Supplier Login
    [Arguments]   ${expected_title}     ${max_iterations}   ${delay}
    ${is_opened}    Set Variable    ${False}
    WHILE   not ${is_opened} and ${max_iterations} > 0
      Input Username    supplier
      Input Password    ${SUPP_PASS}
      Submit Credentials
      Sleep    ${delay}
      ${title}    Get Title
      Log   ${title}
          IF    "${title}" == "${expected_title}"
              ${is_opened}    Set Variable    ${True}
              BREAK
          END
      Sleep    ${delay}
      Reload Page
      ${max_iterations}    Evaluate    ${max_iterations}-1
    END

    IF    ${max_iterations} == 0
       Fail   msg= The ${title} == ${expected_title} was not found.
    END

Wait For Successful GUI Loaded After Login
    [Arguments]   ${max_iterations}   ${delay}
    ${is_opened}    Set Variable    ${False}
    WHILE   not ${is_opened} and ${max_iterations} > 0
      ${status}  Run Keyword And Return Status    Wait Until Element Is Visible   xpath://h1[contains(text(),'Agent Account Management')]
      Log   ${status}
          IF    ${status}
              ${is_opened}    Set Variable    ${True}
              BREAK
          END
      Sleep    ${delay}
      Reload Page
      ${max_iterations}    Evaluate    ${max_iterations}-1
    END

    IF    ${max_iterations} == 0
       Fail   msg= The page was not loaded.
    END

Wait For Element And Interact
    [Arguments]   ${locator}    ${action_keyword}=Click Element     ${argument_for_keyword}=${NONE}     ${max_iterations}=2   ${delay}=0.25
    ${is_clicked}    Set Variable    ${False}
    WHILE   not ${is_clicked} and ${max_iterations} > 0
      Wait Until Element Is Visible    ${locator}
      ${status}=    Run Keyword And Return Status   ${action_keyword}    ${locator}     ${argument_for_keyword}
      Log   ${status}
          IF    ${status}
              ${is_clicked}    Set Variable    ${True}
              BREAK
          END
      Sleep    ${delay}
      Reload Page
      ${max_iterations}    Evaluate    ${max_iterations}-1
    END

    IF    ${max_iterations} == 0
       Fail   msg= The element ${locator}  was not found.
    END
