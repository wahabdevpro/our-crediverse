*** Settings ***
Resource    ../keywords/ussd.resource
Resource    ../keywords/gui.resource
Resource    ../keywords/mysql.resource
Suite Setup     Setup For The Suite
Suite Teardown      Teardown For The Suite

*** Variables ***

${AGENT_ROOT}=      0101024646
${USSD}=     413
${PIN}=  00000
${AGENT_ESTORE}=    0103177994

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
Successful Transfer from Root to eStore via GUI
    [Documentation]     Transfer balance from Root to eStore Agent via web portal GUI
    ...     The language for the Root agent and eStore Agent is English
    ${trade_bonus}=     Set Variable    0
    ${cum_bonus}=       Set Variable    6.864313
    ${agent}=     Set Variable    ${AGENT_ESTORE}
    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT2_PASS}
    ${coauth_msisdn}=   Set Variable    ${ROBOT2_MSISDN}
    ${amount}=      Generate Random Number      1000000    5000000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${bonus_amount}=    Evaluate    ${amount} * ${cum_bonus} * 0.01
    ${bonus_amount_currency_format}=    Change Balance Format To Currency    ${bonus_amount}

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${root_bal}=     Get Agent Balances From DB   ${AGENT_ROOT}
    ${root_bal_before}=   Get From List  ${root_bal}  0
    ${root_bon_before}=   Get From List  ${root_bal}  1
    Clear Call and Sms History On Airsim

    Transfer From Root To Agent     ${agent}    ${amount}
    Coauthorize     ${coauth_user}      ${coauth_pass}

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bal_after_currency_format}=     Change Balance Format To Currency    ${agent_bal_after}
    ${agent_bon_after}=   Get From List  ${agent_bal}  1

    ${root_bal}=     Get Agent Balances From DB   ${AGENT_ROOT}
    ${root_bal_after}=   Get From List  ${root_bal}  0
    ${root_bal_after_currency_format}=     Change Balance Format To Currency    ${root_bal_after}
    ${root_bon_after}=   Get From List  ${root_bal}  1

    ${agent_sms}=   Sms Sent To Msisdn    ${agent}
    ${user_sms}=    Sms Sent To Msisdn    ${ROBOT1_MSISDN}
    ${root_sms}=    Sms Sent To Msisdn    ${AGENT_ROOT}
    ${ref}=     Get Last Transaction No For B Agent From DB   ${agent}

    Ucip Calls Count Should Be      0
    Sms Count Should Be     4
    Should Be Equal       ${agent_sms}      You have Received ${amount_currency_format} Fcfa from ${AGENT_ROOT}. Your new Balance is ${agent_bal_after_currency_format} Fcfa. Ref ${ref}
    Should Be Equal       ${user_sms}      You have Transferred ${amount_currency_format} Fcfa Airtime from ${AGENT_ROOT} to ${agent}. Ref ${ref}
    Should Be Equal       ${root_sms}       You have Transferred ${amount_currency_format} Fcfa to ${agent}. Your new Balance is ${root_bal_after_currency_format} Fcfa. Ref ${ref}

    Balance Check A Party   ${root_bal_before}    ${root_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${root_bon_before}    ${root_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Balance Check B Party   ${agent_bal_before}    ${agent_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${agent_bon_before}    ${agent_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

#Successful Transfer from Root to eStore via GUI using Request coauth
#    [Documentation]     Transfer balance from Root to eStore Agent via web portal GUI
#    ...     The language for the Root agent and eStore Agent is English
#    ${trade_bonus}=     Set Variable    0
#    ${cum_bonus}=       Set Variable    6.864313
#    ${agent}=     Set Variable    ${AGENT_ESTORE}
#    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
#    ${coauth_pass}=     Set Variable    ${ROBOT2_PASS}
#    ${coauth_msisdn}=   Set Variable    ${ROBOT2_MSISDN}
#    ${amount}=      Generate Random Number      1000000    5000000
#    ${amount_currency_format}=      Format String     {:,}    ${amount}
#    ${bonus_amount}=    Evaluate    ${amount} * ${cum_bonus} * 0.01
#    ${bonus_amount_currency_format}=    Change Balance Format To Currency    ${bonus_amount}
#
#    ${agent_bal}=     Get Agent Balances From DB   ${agent}
#    ${agent_bal_before}=   Get From List  ${agent_bal}  0
#    ${agent_bon_before}=   Get From List  ${agent_bal}  1
#    ${root_bal}=     Get Agent Balances From DB   ${AGENT_ROOT}
#    ${root_bal_before}=   Get From List  ${root_bal}  0
#    ${root_bon_before}=   Get From List  ${root_bal}  1
#    Clear Call and Sms History On Airsim
#
#    Transfer From Root To Agent     ${agent}    ${amount}   coauth=request
#
#
#    ${agent_bal}=     Get Agent Balances From DB   ${agent}
#    ${agent_bal_after}=   Get From List  ${agent_bal}  0
#    ${agent_bal_after_currency_format}=     Change Balance Format To Currency    ${agent_bal_after}
#    ${agent_bon_after}=   Get From List  ${agent_bal}  1
#
#    ${root_bal}=     Get Agent Balances From DB   ${AGENT_ROOT}
#    ${root_bal_after}=   Get From List  ${root_bal}  0
#    ${root_bal_after_currency_format}=     Change Balance Format To Currency    ${root_bal_after}
#    ${root_bon_after}=   Get From List  ${root_bal}  1
#
#    ${agent_sms}=   Sms Sent To Msisdn    ${agent}
#    ${user_sms}=    Sms Sent To Msisdn    ${ROBOT1_MSISDN}
#    ${root_sms}=    Sms Sent To Msisdn    ${AGENT_ROOT}
#    ${ref}=     Get Last Transaction No For B Agent From DB   ${agent}
#
#    Ucip Calls Count Should Be      0
#    Sms Count Should Be     4
#    Should Be Equal       ${agent_sms}      You have Received ${amount_currency_format} Fcfa from ${AGENT_ROOT}. Your new Balance is ${agent_bal_after_currency_format} Fcfa. Ref ${ref}
#    Should Be Equal       ${user_sms}      You have Transferred ${amount_currency_format} Fcfa Airtime from ${AGENT_ROOT} to ${agent}. Ref ${ref}
#    Should Be Equal       ${root_sms}       You have Transferred ${amount_currency_format} Fcfa to ${agent}. Your new Balance is ${root_bal_after_currency_format} Fcfa. Ref ${ref}
#
#    Balance Check A Party   ${root_bal_before}    ${root_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
#    Bonus Check A Party     ${root_bon_before}    ${root_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
#    Balance Check B Party   ${agent_bal_before}    ${agent_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
#    Bonus Check B Party     ${agent_bon_before}    ${agent_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}


Failed Transfer from Root to eStore via GUI with same user coauthoriztion
    [Documentation]     Transfer balance from Root to eStore Agent via web portal GUI
    ...     The language for the Root agent and eStore Agent is English
    ${trade_bonus}=     Set Variable    0
    ${cum_bonus}=       Set Variable    6.864313
    ${agent}=     Set Variable    ${AGENT_ESTORE}
    ${coauth_user}=     Set Variable    ${ROBOT1_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT1_PASS}
    ${coauth_msisdn}=   Set Variable    ${ROBOT1_MSISDN}
    ${amount}=      Generate Random Number      1000000    5000000

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${root_bal}=     Get Agent Balances From DB   ${AGENT_ROOT}
    ${root_bal_before}=   Get From List  ${root_bal}  0
    ${root_bon_before}=   Get From List  ${root_bal}  1
    Clear Call and Sms History On Airsim

    Transfer From Root To Agent     ${agent}    ${amount}
    Coauthorization Failed Before OTP     ${coauth_user}      ${coauth_pass}

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1

    ${root_bal}=     Get Agent Balances From DB   ${AGENT_ROOT}
    ${root_bal_after}=   Get From List  ${root_bal}  0
    ${root_bon_after}=   Get From List  ${root_bal}  1

    ${agent_sms}=   Sms Sent To Msisdn    ${agent}
    ${user_sms}=    Sms Sent To Msisdn    ${ROBOT1_MSISDN}
    ${root_sms}=    Sms Sent To Msisdn    ${AGENT_ROOT}

    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    Should Be Equal       ${agent_sms}      "No Message Sent for the MSISDN ${agent}"
    Should Be Equal       ${user_sms}      "No Message Sent for the MSISDN ${ROBOT1_MSISDN}"
    Should Be Equal       ${root_sms}       "No Message Sent for the MSISDN ${AGENT_ROOT}"

    Balance Check A Party   ${root_bal_before}    ${root_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${root_bon_before}    ${root_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Balance Check B Party   ${agent_bal_before}    ${agent_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${agent_bon_before}    ${agent_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}
