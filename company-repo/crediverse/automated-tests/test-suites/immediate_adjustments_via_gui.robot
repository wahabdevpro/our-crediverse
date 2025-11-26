*** Settings ***
Resource    ../keywords/ussd.resource
Resource    ../keywords/gui.resource
Suite Setup     Setup For The Suite
Resource    ../keywords/mysql.resource
Suite Teardown      Teardown For The Suite

*** Variables ***

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
    Login To Admin Gui      ${ROBOT1_USER}      ${ROBOT1_PASS}
    Connect To OLTP DB

Teardown For The Suite
    Disconnect From Database
    Close All Browsers

*** Test Cases ***

Successful positive immediate adjustment for an agent retailer
    [Documentation]    Successful positive immediate adjustment for an agent retailer
    ...     USSD confirmation is disabled for the agent
    ${trade_bonus}=     Set Variable    0
    ${cum_bonus}=       Set Variable    0
    ${agent}=   Set Variable    ${AGENT_RETAILER}
    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT2_PASS}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${root_bal}=     Get Agent Balances From DB   ${AGENT_ROOT}
    ${root_bal_before}=   Get From List  ${root_bal}  0
    ${root_bon_before}=   Get From List  ${root_bal}  1
    Clear Call and Sms History On Airsim
    Immediate Adjustment    ${amount}    ${reason}    ${agent}
    Coauthorize     ${coauth_user}      ${coauth_pass}
    ${a_sms}=   Sms Sent To Msisdn    ${AGENT_ROOT}
    ${b_sms}=   Sms Sent To Msisdn    ${agent}
    ${user1_sms}=    Sms Sent To Msisdn    ${ROBOT1_MSISDN}
    ${user2_sms}=    Sms Sent To Msisdn    ${ROBOT2_MSISDN}

    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after_for_sms}=   Get From List  ${agent_bal}  2
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1

    ${root_bal}=     Get Agent Balances From DB   ${AGENT_ROOT}
    ${root_bal_after}=   Get From List  ${root_bal}  0
    ${root_bal_after_currency_format}=     Change Balance Format To Currency    ${root_bal_after}
    ${root_bon_after}=   Get From List  ${root_bal}  1

    ${ref}      Get Last Transaction No For B Agent From DB   ${agent}
    Log     ${ref}
    Ucip Calls Count Should Be      1
    Ucip Calls Should Be        GetSubscriberInformationRequest
    Sms Count Should Be     5
    Should Be Equal       ${b_sms}      Your ECDS account balance has been adjusted to ${agent_bal_after_for_sms} Fcfa by robot1. Ref ${ref}.
    Should Be Equal       ${user1_sms}   Agent account ${AGENT_RETAILER}, has been adjusted to ${agent_bal_after_for_sms} Fcfa/0 Fcfa. Ref ${ref}
    Should Be Equal       ${user2_sms}   Agent account ${AGENT_RETAILER}, has been adjusted to ${agent_bal_after_for_sms} Fcfa/0 Fcfa. Ref ${ref}

    Balance Check A Party   ${root_bal_before}    ${root_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${root_bon_before}    ${root_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Balance Check B Party   ${agent_bal_before}    ${agent_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${agent_bon_before}    ${agent_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

Successful positive immediate adjustment for an agent eStore
    [Documentation]    Successful positive immediate adjustment for an agent eStore
    ...     USSD confirmation is disabled for the agent
    ${trade_bonus}=     Set Variable    0
    ${cum_bonus}=       Set Variable    6.864313
    ${agent}=     Set Variable    ${AGENT_ESTORE}
    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT2_PASS}
    ${amount}=      Generate Random Number      1000000    5000000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${bonus_amount}=    Evaluate    ${amount} * ${cum_bonus} * 0.01
    ${bonus_amount_currency_format}=    Change Balance Format To Currency    ${bonus_amount}


    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${root_bal}=     Get Agent Balances From DB   ${AGENT_ROOT}
    ${root_bal_before}=   Get From List  ${root_bal}  0
    ${root_bon_before}=   Get From List  ${root_bal}  1
    Clear Call and Sms History On Airsim
    Immediate Adjustment    ${amount}    ${reason}    ${agent}
    Coauthorize     ${coauth_user}      ${coauth_pass}
    ${a_sms}=   Sms Sent To Msisdn    ${AGENT_ROOT}
    ${b_sms}=   Sms Sent To Msisdn    ${agent}
    ${user1_sms}=    Sms Sent To Msisdn    ${ROBOT1_MSISDN}
    ${user2_sms}=    Sms Sent To Msisdn    ${ROBOT2_MSISDN}

    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after_for_sms}=   Get From List  ${agent_bal}  2
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_bon_after_currency_format}=     Change Balance Format To Currency    ${agent_bon_after}
    ${root_bal}=     Get Agent Balances From DB   ${AGENT_ROOT}
    ${root_bal_after}=   Get From List  ${root_bal}  0
    ${root_bal_after_currency_format}=     Change Balance Format To Currency    ${root_bal_after}
    ${root_bon_after}=   Get From List  ${root_bal}  1

    ${ref}      Get Last Transaction No For B Agent From DB   ${agent}
    Log     ${ref}
    Ucip Calls Count Should Be      1
    Ucip Calls Should Be        GetSubscriberInformationRequest
    Sms Count Should Be     5
    Should Be Equal       ${b_sms}      Your ECDS account balance has been adjusted to ${agent_bal_after_for_sms} Fcfa by robot1. Ref ${ref}.
    Should Be Equal       ${user1_sms}   Agent account ${AGENT_ESTORE}, has been adjusted to ${agent_bal_after_for_sms} Fcfa/${agent_bon_after_currency_format} Fcfa. Ref ${ref}
    Should Be Equal       ${user2_sms}   Agent account ${AGENT_ESTORE}, has been adjusted to ${agent_bal_after_for_sms} Fcfa/${agent_bon_after_currency_format} Fcfa. Ref ${ref}

    Balance Check A Party   ${root_bal_before}    ${root_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${root_bon_before}    ${root_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Balance Check B Party   ${agent_bal_before}    ${agent_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

Successful negative immediate adjustment for an agent retailer
    [Documentation]    Successful positive immediate adjustment for an agent retailer
    ...     USSD confirmation is disabled for the agent
    ${trade_bonus}=     Set Variable    0
    ${cum_bonus}=       Set Variable    0
    ${agent}=   Set Variable    ${AGENT_RETAILER}
    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT2_PASS}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${amount}=   Set Variable    -100
    Clear Call and Sms History On Airsim
    Immediate Adjustment    ${amount}    ${reason}    ${agent}
    Coauthorize     ${coauth_user}      ${coauth_pass}
    ${a_sms}=   Sms Sent To Msisdn    ${AGENT_ROOT}
    ${b_sms}=   Sms Sent To Msisdn    ${agent}
    ${user1_sms}=    Sms Sent To Msisdn    ${ROBOT1_MSISDN}
    ${user2_sms}=    Sms Sent To Msisdn    ${ROBOT2_MSISDN}

    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after_for_sms}=   Get From List  ${agent_bal}  2
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1

    ${ref}      Get Last Transaction No For B Agent From DB   ${agent}
    Log     ${ref}
    Ucip Calls Count Should Be      1
    Ucip Calls Should Be        GetSubscriberInformationRequest
    Sms Count Should Be     5
    Should Be Equal       ${b_sms}      Your ECDS account balance has been adjusted to ${agent_bal_after_for_sms} Fcfa by robot1. Ref ${ref}.
    Should Be Equal       ${user1_sms}   Agent account ${AGENT_RETAILER}, has been adjusted to ${agent_bal_after_for_sms} Fcfa/0 Fcfa. Ref ${ref}
    Should Be Equal       ${user2_sms}   Agent account ${AGENT_RETAILER}, has been adjusted to ${agent_bal_after_for_sms} Fcfa/0 Fcfa. Ref ${ref}
    Balance Check B Party   ${agent_bal_before}    ${agent_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${agent_bon_before}    ${agent_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

