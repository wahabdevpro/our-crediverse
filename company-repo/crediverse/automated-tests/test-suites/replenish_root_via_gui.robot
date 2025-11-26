*** Settings ***
Resource    ../keywords/ussd.resource
Resource    ../keywords/gui.resource
Resource    ../keywords/mysql.resource
Suite Setup     Setup For The Suite
Suite Teardown      Teardown For The Suite

*** Variables ***

${AGENT_ROOT}=      0101024646

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
Successful Replenish Root account from the GUI
    [Documentation]     Replenish Root account on the web portal GUI
    ...     The language for the Root agent is English
    ${trade_bonus}=     Set Variable    0
    ${cum_bonus}=       Set Variable    6.864313
    ${agent}=     Set Variable    ${AGENT_ROOT}
    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT2_PASS}
    ${coauth_msisdn}=   Set Variable    ${ROBOT2_MSISDN}
    ${amount}=      Generate Random Number      10000000    50000000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${bonus_amount}=    Evaluate    ${amount} * ${cum_bonus} * 0.01
    ${bonus_amount_currency_format}=    Change Balance Format To Currency    ${bonus_amount}

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    Clear Call and Sms History On Airsim

    Replenish Root Account  ${amount}
    Coauthorize     ${coauth_user}      ${coauth_pass}

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bal_after_currency_format}=     Change Balance Format To Currency    ${agent_bal_after}
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_bon_after_currency_format}=     Change Balance Format To Currency    ${agent_bon_after}

    ${agent_sms}=   Sms Sent To Msisdn    ${agent}
    ${user_sms}=    Sms Sent To Msisdn    ${coauth_msisdn}
    ${ref}=     Get Last Transaction No For B Agent From DB   ${agent}

    Ucip Calls Count Should Be      0
    Sms Count Should Be     4
    Should Be Equal       ${agent_sms}      The ROOT Account has been replenished by ${ROBOT1_USER} with ${amount_currency_format} Fcfa, and ${bonus_amount_currency_format} Fcfa for bonus provision. The new ROOT balance is ${agent_bal_after_currency_format} Fcfa and a Bonus Provision for ${agent_bon_after_currency_format} Fcfa. Ref ${ref}
    Should Be Equal       ${user_sms}      The ROOT Account has been replenished by ${ROBOT1_USER} with ${amount_currency_format} Fcfa, and ${bonus_amount_currency_format} Fcfa for bonus provision. The new ROOT balance is ${agent_bal_after_currency_format} Fcfa and a Bonus Provision for ${agent_bon_after_currency_format} Fcfa. Ref ${ref}
    Balance Check B Party   ${agent_bal_before}    ${agent_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${agent_bon_before}    ${agent_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}


Successful Replenish Root account from the GUI with custom bonus more than recommended
    [Documentation]     Replenish Root account on the web portal GUI
    ...     The language for the Root agent is English
    ${trade_bonus}=     Set Variable    0
    ${cum_bonus}=       Set Variable    7.85
    ${agent}=     Set Variable    ${AGENT_ROOT}
    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT2_PASS}
    ${coauth_msisdn}=   Set Variable    ${ROBOT2_MSISDN}
    ${amount}=      Generate Random Number      10000000    50000000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${bonus_amount}=    Evaluate    ${amount} * ${cum_bonus} * 0.01
    ${bonus_amount}=    Evaluate    round(${bonus_amount},0)
    ${bonus_amount_currency_format}=    Change Balance Format To Currency    ${bonus_amount}

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    Clear Call and Sms History On Airsim

    Replenish Root Account  ${amount}   ${bonus_amount}
    Coauthorize     ${coauth_user}      ${coauth_pass}

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bal_after_currency_format}=     Change Balance Format To Currency    ${agent_bal_after}
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_bon_after_currency_format}=     Change Balance Format To Currency    ${agent_bon_after}

    ${agent_sms}=   Sms Sent To Msisdn    ${agent}
    ${user_sms}=    Sms Sent To Msisdn    ${coauth_msisdn}
    ${ref}=     Get Last Transaction No For B Agent From DB   ${agent}

    Ucip Calls Count Should Be      0
    Sms Count Should Be     4
    Should Be Equal       ${agent_sms}      The ROOT Account has been replenished by ${ROBOT1_USER} with ${amount_currency_format} Fcfa, and ${bonus_amount_currency_format} Fcfa for bonus provision. The new ROOT balance is ${agent_bal_after_currency_format} Fcfa and a Bonus Provision for ${agent_bon_after_currency_format} Fcfa. Ref ${ref}
    Should Be Equal       ${user_sms}      The ROOT Account has been replenished by ${ROBOT1_USER} with ${amount_currency_format} Fcfa, and ${bonus_amount_currency_format} Fcfa for bonus provision. The new ROOT balance is ${agent_bal_after_currency_format} Fcfa and a Bonus Provision for ${agent_bon_after_currency_format} Fcfa. Ref ${ref}
    Balance Check B Party   ${agent_bal_before}    ${agent_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${agent_bon_before}    ${agent_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

Successful Replenish Root account from the GUI with custom bonus less than recommended
    [Documentation]     Replenish Root account on the web portal GUI
    ...     The language for the Root agent is English
    ${trade_bonus}=     Set Variable    0
    ${cum_bonus}=       Set Variable    1.025
    ${agent}=     Set Variable    ${AGENT_ROOT}
    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT2_PASS}
    ${coauth_msisdn}=   Set Variable    ${ROBOT2_MSISDN}
    ${amount}=      Generate Random Number      10000000    50000000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${bonus_amount}=    Evaluate    ${amount} * ${cum_bonus} * 0.01
    ${bonus_amount}=    Evaluate    round(${bonus_amount},0)
    ${bonus_amount_currency_format}=    Change Balance Format To Currency    ${bonus_amount}

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    Clear Call and Sms History On Airsim

    Replenish Root Account  ${amount}   ${bonus_amount}
    Coauthorize     ${coauth_user}      ${coauth_pass}

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bal_after_currency_format}=     Change Balance Format To Currency    ${agent_bal_after}
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_bon_after_currency_format}=     Change Balance Format To Currency    ${agent_bon_after}

    ${agent_sms}=   Sms Sent To Msisdn    ${agent}
    ${user_sms}=    Sms Sent To Msisdn    ${coauth_msisdn}
    ${ref}=     Get Last Transaction No For B Agent From DB   ${agent}

    Ucip Calls Count Should Be      0
    Sms Count Should Be     4
    Should Be Equal       ${agent_sms}      The ROOT Account has been replenished by ${ROBOT1_USER} with ${amount_currency_format} Fcfa, and ${bonus_amount_currency_format} Fcfa for bonus provision. The new ROOT balance is ${agent_bal_after_currency_format} Fcfa and a Bonus Provision for ${agent_bon_after_currency_format} Fcfa. Ref ${ref}
    Should Be Equal       ${user_sms}      The ROOT Account has been replenished by ${ROBOT1_USER} with ${amount_currency_format} Fcfa, and ${bonus_amount_currency_format} Fcfa for bonus provision. The new ROOT balance is ${agent_bal_after_currency_format} Fcfa and a Bonus Provision for ${agent_bon_after_currency_format} Fcfa. Ref ${ref}
    Balance Check B Party   ${agent_bal_before}    ${agent_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${agent_bon_before}    ${agent_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}


Failed Replenish Root account from the GUI with same user coauth
    [Documentation]     Replenish Root account on the web portal GUI fails when logged in user is used for coauth
    ...     The language for the Root agent is English
    ${trade_bonus}=     Set Variable    0
    ${cum_bonus}=       Set Variable    6.864313
    ${agent}=     Set Variable    ${AGENT_ROOT}
    ${coauth_user}=     Set Variable    ${ROBOT1_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT1_PASS}
    ${coauth_msisdn}=   Set Variable    ${ROBOT1_MSISDN}
    ${amount}=      Generate Random Number      10000000    50000000


    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    Clear Call and Sms History On Airsim

    Replenish Root Account  ${amount}
    Coauthorization Failed Before OTP     ${coauth_user}      ${coauth_pass}

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_sms}=   Sms Sent To Msisdn    ${agent}
    ${user_sms}=    Sms Sent To Msisdn    ${coauth_msisdn}

    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    Should Be Equal       ${agent_sms}      "No Message Sent for the MSISDN ${agent}"
    Should Be Equal       ${user_sms}      "No Message Sent for the MSISDN ${coauth_msisdn}"
    Balance Check B Party   ${agent_bal_before}    ${agent_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${agent_bon_before}    ${agent_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}


Failed Replenish Root account from the GUI with invalid password coauth
    [Documentation]     Replenish Root account on the web portal GUI fails when incorrect password is entered for coauth
    ...     The language for the Root agent is English
    ${trade_bonus}=     Set Variable    0
    ${cum_bonus}=       Set Variable    6.864313
    ${agent}=     Set Variable    ${AGENT_ROOT}
    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
    ${coauth_pass}=     Set Variable    invalid_password
    ${coauth_msisdn}=   Set Variable    ${ROBOT2_MSISDN}
    ${amount}=      Generate Random Number      10000000    50000000


    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    Clear Call and Sms History On Airsim

    Replenish Root Account  ${amount}
    Coauthorization Failed Before OTP     ${coauth_user}      ${coauth_pass}

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_sms}=   Sms Sent To Msisdn    ${agent}
    ${user_sms}=    Sms Sent To Msisdn    ${coauth_msisdn}

    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    Should Be Equal       ${agent_sms}      "No Message Sent for the MSISDN ${agent}"
    Should Be Equal       ${user_sms}      "No Message Sent for the MSISDN ${coauth_msisdn}"
    Balance Check B Party   ${agent_bal_before}    ${agent_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${agent_bon_before}    ${agent_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}


Failed Replenish Root account from the GUI with invalid OTP coauth
    [Documentation]     Replenish Root account on the web portal GUI fails when incorrect OTP is entered
    ...     The language for the Root agent is English
    ${trade_bonus}=     Set Variable    0
    ${cum_bonus}=       Set Variable    6.864313
    ${agent}=     Set Variable    ${AGENT_ROOT}
    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT2_PASS}
    ${coauth_msisdn}=   Set Variable    ${ROBOT2_MSISDN}
    ${amount}=      Generate Random Number      10000000    50000000

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    Clear Call and Sms History On Airsim

    Replenish Root Account  ${amount}
    Coauthorize     ${coauth_user}      ${coauth_pass}      otp=34501

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_sms}=   Sms Sent To Msisdn    ${agent}
    ${user_sms}=    Sms Sent To Msisdn    ${coauth_msisdn}

    Ucip Calls Count Should Be      0
    Sms Count Should Be     2
    Should Be Equal       ${agent_sms}      "No Message Sent for the MSISDN ${agent}"
    Should Start With       ${user_sms}      Your One Time PIN is
    Balance Check B Party   ${agent_bal_before}    ${agent_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${agent_bon_before}    ${agent_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}

Failed Replenish Root account from the GUI via supplier
    [Documentation]     Replenish Root account on the web portal GUI fails when logged in via supplier
    ...     The language for the Root agent is English
    [setup]     Open Admin Gui Login Page
    ${trade_bonus}=     Set Variable    0
    ${cum_bonus}=       Set Variable    6.864313
    ${agent}=     Set Variable    ${AGENT_ROOT}
    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT2_PASS}
    ${coauth_msisdn}=   Set Variable    ${ROBOT2_MSISDN}
    ${amount}=      Generate Random Number      10000000    50000000

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    Clear Call and Sms History On Airsim

    Login To Admin Gui As Supplier      ${SUPP_PASS}
    Replenish Root Account  ${amount}
    Coauthorize     ${coauth_user}      ${coauth_pass}     supplier=True

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_sms}=   Sms Sent To Msisdn    ${agent}
    ${user_sms}=    Sms Sent To Msisdn    ${coauth_msisdn}

    Ucip Calls Count Should Be      0
    Sms Count Should Be     1
    Should Be Equal       ${agent_sms}      "No Message Sent for the MSISDN ${agent}"
    Should Start With       ${user_sms}      Your One Time PIN is
    Balance Check B Party   ${agent_bal_before}    ${agent_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${agent_bon_before}    ${agent_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}

