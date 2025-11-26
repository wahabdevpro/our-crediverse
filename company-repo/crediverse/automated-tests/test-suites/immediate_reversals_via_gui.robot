*** Settings ***
Resource    ../keywords/ussd.resource
Resource    ../keywords/gui.resource
Suite Setup     Setup For The Suite
Resource    ../keywords/mysql.resource
Suite Teardown      Teardown For The Suite
Library    Telnet

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
${partial_amount}   50
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

Successful immediate full reversal for an airtime sell to a subscriber
    [Documentation]      Sell Airtime immediate full reversal to a subscriber
    ...     USSD confirmation is disabled for the agent
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable    ${AGENT_RETAILER}
    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT2_PASS}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active

    ${ref}=     Perform Airtime Sale Via Ussd   ${agent}   ${subscriber}   ${AMOUNT}    ${PIN}   ${USSD_AIRTIME}
    Clear Call and Sms History On Airsim
    Immediate Full Reversal    ${ref}    ${reason}     ${agent}
    Coauthorize     ${coauth_user}      ${coauth_pass}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after_for_sms}=   Get From List  ${agent_bal}  2
    ${agent_bal_after}=   Get From List  ${agent_bal}  0

    ${ref_reversal}      Get Last Transaction No For B Agent From DB    ${subscriber}
    Log     ${ref_reversal}
    Ucip Calls Count Should Be      3
    Ucip Calls Should Be    GetBalanceAndDateRequest     UpdateBalanceAndDateRequest     GetSubscriberInformationRequest

    ${expected_subs_bal}=      Format String     {:,}    ${${0}+${SUB_INITIAL_BAL}}
    Sms Count Should Be     5
    Should Be Equal       ${a_sms}      Your Transaction ${ref} has been reversed to the value of -${Amount} Fcfa. Your new balance = ${agent_bal_after_for_sms} Fcfa. Ref ${ref_reversal}.
    Should Be Equal       ${b_sms}      Your Transaction ${ref} has been reversed to the value of -${Amount} Fcfa. Your new balance = ${expected_subs_bal} Fcfa. Ref ${ref_reversal}.
    Subscriber Balance Should Be    ${subscriber}   ${${0}+${SUB_INITIAL_BAL}}
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}

Successful immediate partial reversal for an airtime sell to a subscriber
    [Documentation]      Sell Airtime immediate partial reversal to a subscriber
    ...     USSD confirmation is disabled for the agent
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable    ${AGENT_RETAILER}
    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT2_PASS}

    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    ${ref}=     Perform Airtime Sale Via Ussd   ${agent}   ${subscriber}   ${AMOUNT}    ${PIN}   ${USSD_AIRTIME}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    Clear Call and Sms History On Airsim
    Immediate Partial Reversal   ${ref}    ${reason}     ${partial_amount}   ${agent}
    Coauthorize     ${coauth_user}      ${coauth_pass}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${ref_reversal}      Get Last Transaction No For B Agent From DB    ${subscriber}
    Log     ${ref_reversal}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after_for_sms}=   Get From List  ${agent_bal}  2
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    Ucip Calls Count Should Be      3
    Ucip Calls Should Be    GetBalanceAndDateRequest     UpdateBalanceAndDateRequest     GetSubscriberInformationRequest
    ${deducted_val_after_reversal}=     Evaluate    ${AMOUNT}-${partial_amount}
    ${expected_subs_bal}=      Format String     {:,}    ${${deducted_val_after_reversal}+${SUB_INITIAL_BAL}}
    Subscriber Balance Should Be    ${subscriber}   ${${deducted_val_after_reversal}+${SUB_INITIAL_BAL}}
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} + ${partial_amount}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    Sms Count Should Be     5
    Should Be Equal       ${a_sms}      Your Transaction ${ref} has been reversed to the value of -${partial_amount} Fcfa. Your new balance = ${agent_bal_after_for_sms} Fcfa. Ref ${ref_reversal}.
    Should Be Equal       ${b_sms}      Your Transaction ${ref} has been reversed to the value of -${partial_amount} Fcfa. Your new balance = ${expected_subs_bal} Fcfa. Ref ${ref_reversal}.

Successful immediate full reversal for a transfer from eMaster to eIntermed
    [Documentation]      Transfer immediate full reversal from eMaster to eIntermed
    ...     USSD confirmation is disabled for the agent
    ${trade_bonus}=     Set Variable    1
    ${cum_bonus}=       Set Variable    5.545
    ${a_agent}=     Set Variable    ${AGENT_EMASTER}
    ${b_agent}=     Set Variable    ${AGENT_EINTERMED}
    ${amount}=      Generate Random Number      10000    25000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT2_PASS}

    ${ref}=     Transfer Via Ussd   ${a_agent}    ${b_agent}   ${amount}   ${pin}  ${USSD_TRANSFER}
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_before}=   Get From List  ${a_bal}  0
    ${a_bon_before}=   Get From List  ${a_bal}  1
    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_before}=   Get From List  ${b_bal}  0
    ${b_bon_before}=   Get From List  ${b_bal}  1
    Clear Call and Sms History On Airsim
    Immediate Full Reversal    ${ref}    ${reason}     ${a_agent}
    Coauthorize     ${coauth_user}      ${coauth_pass}
    ${a_sms}=   Sms Sent To Msisdn    ${a_agent}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1
    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1
    ${ref_reversal}      Get Last Transaction No For B Agent From DB    ${b_agent}
    Log     ${ref_reversal}
    Ucip Calls Count Should Be      0
    ${a_bal_after_for_sms}=     Change Balance Format To Currency    ${a_bal_after}
    ${b_bal_after_for_sms}=     Change Balance Format To Currency    ${b_bal_after}

    Sms Count Should Be     4
    Should Be Equal       ${a_sms}      Your Transaction ${ref} has been reversed to the value of -${amount_currency_format} Fcfa. Your new balance = ${a_bal_after_for_sms} Fcfa. Ref ${ref_reversal}.
    Should Be Equal       ${b_sms}      Your Transaction ${ref} has been reversed to the value of -${amount_currency_format} Fcfa. Your new balance = ${b_bal_after_for_sms} Fcfa. Ref ${ref_reversal}.
    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    -${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    -${amount}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    -${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    -${amount}   ${trade_bonus}   ${cum_bonus}

Successful immediate partial reversal for a transfer from eMaster to eIntermed
    [Documentation]      Transfer immediate partial reversal  from eMaster to eIntermed
    ...     USSD confirmation is disabled for the agent
    ${trade_bonus}=     Set Variable    1
    ${cum_bonus}=       Set Variable    5.545
    ${a_agent}=     Set Variable    ${AGENT_EMASTER}
    ${b_agent}=     Set Variable    ${AGENT_EINTERMED}
    ${amount}=      Generate Random Number      10000    25000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${coauth_user}=     Set Variable    ${ROBOT2_USER}
    ${coauth_pass}=     Set Variable    ${ROBOT2_PASS}

    ${ref}=     Transfer Via Ussd   ${a_agent}    ${b_agent}   ${amount}   ${pin}  ${USSD_TRANSFER}
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_before}=   Get From List  ${a_bal}  0
    ${a_bon_before}=   Get From List  ${a_bal}  1
    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_before}=   Get From List  ${b_bal}  0
    ${b_bon_before}=   Get From List  ${b_bal}  1
    Clear Call and Sms History On Airsim
    Immediate Partial Reversal    ${ref}  ${reason}     ${partial_amount}   ${a_agent}
    Coauthorize     ${coauth_user}      ${coauth_pass}
    ${a_sms}=   Sms Sent To Msisdn    ${a_agent}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1
    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1
    ${ref_reversal}      Get Last Transaction No For B Agent From DB    ${b_agent}
    Log     ${ref_reversal}
    Ucip Calls Count Should Be      0
    ${a_bal_after_for_sms}=     Change Balance Format To Currency    ${a_bal_after}
    ${b_bal_after_for_sms}=     Change Balance Format To Currency    ${b_bal_after}
    ${deducted_val_after_reversal}=     Evaluate    ${AMOUNT}-${partial_amount}
    ${expected_agent_bal}=      Format String     {:,}    ${${deducted_val_after_reversal}+${a_bal_after}}
    ${expected_agent_bal}=  Evaluate    ${a_bal_after} + ${partial_amount}
    Sms Count Should Be     4
    Should Be Equal       ${a_sms}      Your Transaction ${ref} has been reversed to the value of -${partial_amount} Fcfa. Your new balance = ${a_bal_after_for_sms} Fcfa. Ref ${ref_reversal}.
    Should Be Equal       ${b_sms}      Your Transaction ${ref} has been reversed to the value of -${partial_amount} Fcfa. Your new balance = ${b_bal_after_for_sms} Fcfa. Ref ${ref_reversal}.
    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    -${partial_amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    -${partial_amount}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    -${partial_amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    -${partial_amount}   ${trade_bonus}   ${cum_bonus}

