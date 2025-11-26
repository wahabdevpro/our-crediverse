*** Settings ***
Resource    ../keywords/ussd.resource
Resource    ../keywords/gui.resource
Suite Setup     Setup For The Suite
Resource    ../keywords/mysql.resource
Suite Teardown      Teardown For The Suite

*** Variables ***
${AGENT_Acc}=      0142307792
${USSD_TRANSFER}=     413
${USSD_AIRTIME}=     410
${PIN}=  00000
${AMOUNT}   90
${SUB_INITIAL_BAL}      7000
${AGENT_RETAILER}   0142307792
${AGENT_RETAILER_CONFIRM}   0142307939
${AGENT_RETAILER_INSUFF_FUNDS}     0142307679
${AGENT_RETAILER_INSUFF_FUNDS_CONFIRM}     0142308147
${AGENT_WHOLESALER}     0140399328
${AGENT_WHOLESALER_CONFIRM}     0140383517
${reason}  Text
${AGENT_ROOT}=      0101024646
${PIN}=  00000
${AGENT_ESTORE}=    0103177994

${AGENT_ESTORE_CONFIRM}=    0102389367
${AGENT_EMASTER_BANQUE}=    0143672741
${AGENT_EMASTER}=   0101046224
${AGENT_EMASTER_DEACTIVATED}=   0141797777
${ECABINE_INTL}=    0101476983
${AGENT_EINTERMED}=     0160688493
${AGENT_EINTERMED_INSUFF_FUNDS}=     0152783027
${AGENT_EINTERMED_DALOA}=     0160747508
${AGENT_EINTERMED_SUSPENDED}=       0152783074

*** Keywords ***
Setup For The Suite
    Open Admin Gui Login Page
    Start Airsim
#   Login To Admin Gui      ${ROBOT1_USER}      ${ROBOT1_PASS}
    Connect To OLTP DB

Teardown For The Suite
    Disconnect From Database
    Close All Browsers

*** Test Cases ***
Successful positive request for an adjustment for an agent eMaster
    [Documentation]    Successful positive request for adjustment for an agent eMaster
    ...     USSD confirmation is disabled for the agent
    [Setup]     Login To Admin Gui    ${ROBOT1_USER}    ${ROBOT1_PASS}
    [Teardown]   Logout From Admin Gui
    ${trade_bonus}=     Set Variable    0
    ${cum_bonus}=       Set Variable    0
    ${agent}=   Set Variable    ${AGENT_EMASTER}
    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT2_PASS}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    Clear Call and Sms History On Airsim
    Request Adjustment    ${amount}    ${reason}    ${agent}
    Sleep    1
    Logout From Admin Gui
    Login To Admin Gui      ${ROBOT2_USER}      ${ROBOT2_PASS}
    Approve request
    ${a_sms}=   Sms Sent To Msisdn    ${AGENT_ROOT}
    ${b_sms}=   Sms Sent To Msisdn    ${agent}

    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after_for_sms}=   Get From List  ${agent_bal}  2
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1

    ${user1_sms}=    Sms Sent To Msisdn    ${ROBOT1_MSISDN}
    ${user2_sms}=    Sms Sent To Msisdn    ${ROBOT2_MSISDN}

    ${ref}      Get Last Transaction No For B Agent From DB   ${agent}
    Log     ${ref}
    Ucip Calls Count Should Be      1
    Ucip Calls Should Be        GetSubscriberInformationRequest
    Sms Count Should Be     10
    Should Be Equal           ${b_sms}      Your ECDS account balance has been adjusted to ${agent_bal_after_for_sms} Fcfa by robot1. Ref ${ref}.
    Should Be Equal           ${user1_sms}   Agent account ${AGENT_EMASTER}, has been adjusted to ${agent_bal_after_for_sms} Fcfa/0 Fcfa. Ref ${ref}
    Should Be Equal           ${user2_sms}   Agent account ${AGENT_EMASTER}, has been adjusted to ${agent_bal_after_for_sms} Fcfa/0 Fcfa. Ref ${ref}
    Balance Check B Party     ${agent_bal_before}    ${agent_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party       ${agent_bon_before}    ${agent_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

Successful positive request for an adjustment for an agent eMaster banque
    [Documentation]    Successful positive request for adjustment for an agent eMaster banque
    ...     USSD confirmation is disabled for the agent
    [Setup]     Login To Admin Gui    ${ROBOT1_USER}    ${ROBOT1_PASS}
    [Teardown]   Logout From Admin Gui
    ${trade_bonus}=     Set Variable    0
    ${cum_bonus}=       Set Variable    0
    ${agent}=   Set Variable    ${AGENT_EMASTER_BANQUE}
    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT2_PASS}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    Clear Call and Sms History On Airsim
    Request Adjustment    ${amount}    ${reason}    ${agent}
    Sleep    1
    Logout From Admin Gui
    Login To Admin Gui      ${ROBOT2_USER}      ${ROBOT2_PASS}
    Approve request
    ${a_sms}=   Sms Sent To Msisdn    ${AGENT_ROOT}
    ${b_sms}=   Sms Sent To Msisdn    ${agent}

    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after_for_sms}=   Get From List  ${agent_bal}  2
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1

    ${user1_sms}=    Sms Sent To Msisdn    ${ROBOT1_MSISDN}
    ${user2_sms}=    Sms Sent To Msisdn    ${ROBOT2_MSISDN}

    ${ref}      Get Last Transaction No For B Agent From DB   ${agent}
    Log     ${ref}
    Ucip Calls Count Should Be      1
    Ucip Calls Should Be        GetSubscriberInformationRequest
    Sms Count Should Be     10
    Should Be Equal       ${b_sms}      Your ECDS account balance has been adjusted to ${agent_bal_after_for_sms} Fcfa by robot1. Ref ${ref}.
   # Should Be Equal       ${user1_sms}   Agent account ${AGENT_EMASTER_BANQUE}, has been adjusted to ${agent_bal_after_for_sms} Fcfa/0 Fcfa. Ref ${ref}
   # Should Be Equal       ${user2_sms}   Agent account ${AGENT_EMASTER_BANQUE}, has been adjusted to ${agent_bal_after_for_sms} Fcfa/0 Fcfa. Ref ${ref}
    Balance Check B Party   ${agent_bal_before}    ${agent_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${agent_bon_before}    ${agent_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

Successful negative request for an adjustment for an agent eCabine international
    [Documentation]    Successful positive request for adjustment for an agent eCabine international
    ...     USSD confirmation is disabled for the agent
    [Setup]     Login To Admin Gui    ${ROBOT1_USER}    ${ROBOT1_PASS}
    [Teardown]   Logout From Admin Gui
    ${trade_bonus}=     Set Variable    0
    ${cum_bonus}=       Set Variable    0
    ${agent}=   Set Variable    ${ECABINE_INTL}
    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT2_PASS}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${amount}=   Set Variable    -100

    Clear Call and Sms History On Airsim
    Request Adjustment    ${amount}    ${reason}    ${agent}
    Sleep    1
    Logout From Admin Gui
    Login To Admin Gui      ${ROBOT2_USER}      ${ROBOT2_PASS}
    Approve request
    ${a_sms}=   Sms Sent To Msisdn    ${AGENT_ROOT}
    ${b_sms}=   Sms Sent To Msisdn    ${agent}

    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after_for_sms}=   Get From List  ${agent_bal}  2
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1

    ${user1_sms}=    Sms Sent To Msisdn    ${ROBOT1_MSISDN}
    ${user2_sms}=    Sms Sent To Msisdn    ${ROBOT2_MSISDN}

    ${ref}      Get Last Transaction No For B Agent From DB   ${agent}
    Log     ${ref}
    Ucip Calls Count Should Be      1
    Ucip Calls Should Be        GetSubscriberInformationRequest
    Sms Count Should Be     10
    Should Be Equal       ${b_sms}      Your ECDS account balance has been adjusted to ${agent_bal_after_for_sms} Fcfa by robot1. Ref ${ref}.
 #   Should Be Equal       ${user1_sms}   Agent account ${ECABINE_INTL}, has been adjusted to ${agent_bal_after_for_sms} Fcfa/0 Fcfa. Ref ${ref}
 #   Should Be Equal       ${user2_sms}   Agent account ${ECABINE_INTL}, has been adjusted to ${agent_bal_after_for_sms} Fcfa/0 Fcfa. Ref ${ref}
    Balance Check B Party   ${agent_bal_before}    ${agent_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${agent_bon_before}    ${agent_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

Successful negative request for an adjustment for an agent eintermed
    [Documentation]    Successful negative request for adjustment for agent eintermed
    ...     USSD confirmation is disabled for the agent
    [Setup]     Login To Admin Gui    ${ROBOT1_USER}    ${ROBOT1_PASS}
    [Teardown]   Logout From Admin Gui
    ${trade_bonus}=     Set Variable    0
    ${cum_bonus}=       Set Variable    0
    ${agent}=   Set Variable    ${AGENT_EINTERMED}
    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT2_PASS}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${amount}=   Set Variable    -100

    Clear Call and Sms History On Airsim
    Request Adjustment    ${amount}    ${reason}    ${agent}
    Sleep    1
    Logout From Admin Gui
    Login To Admin Gui      ${ROBOT2_USER}      ${ROBOT2_PASS}
    Approve request
    ${a_sms}=   Sms Sent To Msisdn    ${AGENT_ROOT}
    ${b_sms}=   Sms Sent To Msisdn    ${agent}

    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after_for_sms}=   Get From List  ${agent_bal}  2
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1

    ${user1_sms}=    Sms Sent To Msisdn    ${ROBOT1_MSISDN}
    ${user2_sms}=    Sms Sent To Msisdn    ${ROBOT2_MSISDN}

    ${ref}      Get Last Transaction No For B Agent From DB   ${agent}
    Log     ${ref}
    Ucip Calls Count Should Be      1
    Ucip Calls Should Be        GetSubscriberInformationRequest
    Sms Count Should Be     10
    Should Be Equal       ${b_sms}      Your ECDS account balance has been adjusted to ${agent_bal_after_for_sms} Fcfa by robot1. Ref ${ref}.
 #   Should Be Equal       ${user1_sms}   Agent account ${AGENT_EINTERMED}, has been adjusted to ${agent_bal_after_for_sms} Fcfa/0 Fcfa. Ref ${ref}
 #   Should Be Equal       ${user2_sms}   Agent account ${AGENT_EINTERMED}, has been adjusted to ${agent_bal_after_for_sms} Fcfa/0 Fcfa. Ref ${ref}
    Balance Check B Party   ${agent_bal_before}    ${agent_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${agent_bon_before}    ${agent_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

## Needs some rework to handle the request co-auth as logging out and loggin in fails very often.