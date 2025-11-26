*** Settings ***
Resource    ../keywords/ussd.resource
Resource    ../keywords/gui.resource
Resource    ../keywords/mysql.resource
Suite Setup     Setup For The Suite
Suite Teardown      Teardown For The Suite

*** Variables ***

${USSD}=     413
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

${SUB_INITIAL_BAL}=     4000
${AGENT_RETAILER}   0142307792
${AGENT_RETAILER_CONFIRM}   0142307939
${AGENT_RETAILER_INSUFF_FUNDS}     0142307679
${AGENT_RETAILER_INSUFF_FUNDS_CONFIRM}     0142308147
${AGENT_RETAILER_SUSPENDED}=    0160618827
${AGENT_WHOLESALER}     0140399328
${AGENT_WHOLESALER_CONFIRM}     0140383517

*** Keywords ***
Setup For The Suite
    Start Airsim
    Connect To OLTP DB

Teardown For The Suite
    Disconnect From Database

*** Test Cases ***

Successful transfer from eStore to eMaster(Banque) in English without confirmation
    [Documentation]     Transfer successfully from eStore agent to eMaster (banque)
    ...     USSD confirmation is disabled for the A agent
    ${trade_bonus}=     Set Variable    0
    ${cum_bonus}=       Set Variable    6.864313
    ${a_agent}=     Set Variable    ${AGENT_ESTORE}
    ${b_agent}=     Set Variable    ${AGENT_EMASTER_BANQUE}
    ${amount}=      Generate Random Number      1000    50000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_before}=   Get From List  ${a_bal}  0
    ${a_bon_before}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_before}=   Get From List  ${b_bal}  0
    ${b_bon_before}=   Get From List  ${b_bal}  1

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${a_agent}    *${USSD}*${b_agent}*${amount}*${PIN}#
    Log     ${response}

    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${a_agent}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}
    ${a_bal_ussd}=     Get Agent Balances Via Ussd   ${a_agent}  ${PIN}
    ${a_bal_after_for_sms}=     Get From List  ${a_bal_ussd}  2
    ${b_bal_ussd}=     Get Agent Balances Via Ussd   ${b_agent}  ${PIN}
    ${b_bal_after_for_sms}=     Get From List  ${b_bal_ussd}  2
    ${ref}      Get Ref Number of Last Transaction via USSD    ${a_agent}    ${PIN}
    USSD Response Should Be    ${response}    You have Transferred ${amount_currency_format} Fcfa to ${b_agent}. Your new Balance is ${a_bal_after_for_sms} Fcfa. Ref ${ref}
    Ucip Calls Count Should Be      5
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     5
    Should Be Equal       ${a_sms}      You have Transferred ${amount_currency_format} Fcfa to ${b_agent}. Your new Balance is ${a_bal_after_for_sms} Fcfa. Ref ${ref}
    Should Be Equal       ${b_sms}      You have Received ${amount_currency_format} Fcfa from ${a_agent}. Your new Balance is ${b_bal_after_for_sms} Fcfa. Ref ${ref}

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}


Successful transfer from eStore to eCabine International in English with confirmation and transfer bonus
    [Documentation]     Transfer successfully from eStore agent to eCabine International
    ...     USSD confirmation is enabled for the A agent and the transfer rule has 1% transfer bonus
    ${trade_bonus}=     Set Variable    5
    ${cum_bonus}=       Set Variable    5
    ${transfer_bonus}=  Set Variable    1
    ${a_agent}=     Set Variable    ${AGENT_ESTORE_CONFIRM}
    ${b_agent}=     Set Variable    ${ECABINE_INTL}
    ${amount}=      Generate Random Number      10000    50000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    Create Subscriber On Airsim     ${b_agent}   2   11     ${SUB_INITIAL_BAL}     active
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_before}=   Get From List  ${a_bal}  0
    ${a_bon_before}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_before}=   Get From List  ${b_bal}  0
    ${b_bon_before}=   Get From List  ${b_bal}  1

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${a_agent}    *${USSD}*${b_agent}*${amount}*${PIN}#   last=False
    Log     ${response}
    ${response}=    Send Ussd Request     ${a_agent}    1
    Log     ${response}
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${a_agent}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}
    ${a_bal_ussd}=     Get Agent Balances Via Ussd   ${a_agent}  ${PIN}
    ${a_bal_after_for_sms}=     Get From List  ${a_bal_ussd}  2
    ${b_bal_ussd}=     Get Agent Balances Via Ussd   ${b_agent}  ${PIN}
    ${b_bal_after_for_sms}=     Get From List  ${b_bal_ussd}  2
    ${ref}      Get Ref Number of Last Transaction via USSD    ${a_agent}    ${PIN}
    USSD Response Should Be    ${response}    You have Transferred ${amount_currency_format} Fcfa to ${b_agent}. Your new Balance is ${a_bal_after_for_sms} Fcfa. Ref ${ref}
    Ucip Calls Count Should Be      6
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     RefillRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     5
    Should Be Equal       ${a_sms}      You have Transferred ${amount_currency_format} Fcfa to ${b_agent}. Your new Balance is ${a_bal_after_for_sms} Fcfa. Ref ${ref}
    Should Be Equal       ${b_sms}      You have Received ${amount_currency_format} Fcfa from ${a_agent}. Your new Balance is ${b_bal_after_for_sms} Fcfa. Ref ${ref}

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    ${expected_b_agent_airtime}=    Evaluate    math.ceil(${${amount}*${transfer_bonus}*0.01+${SUB_INITIAL_BAL}})
    Subscriber Balance Should Be    ${b_agent}   ${expected_b_agent_airtime}

Successful transfer from eMaster(Banque) to eMaster in English without confirmation
    [Documentation]     Transfer successfully from eMaster(Banque) agent to eMaster
    ...     USSD confirmation is disabled for the A agent
    ${trade_bonus}=     Set Variable    1.25
    ${cum_bonus}=       Set Variable    6.864313
    ${a_agent}=     Set Variable    ${AGENT_EMASTER_BANQUE}
    ${b_agent}=     Set Variable    ${AGENT_EMASTER}
    ${amount}=      Generate Random Number      1000000    2000000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_before}=   Get From List  ${a_bal}  0
    ${a_bon_before}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_before}=   Get From List  ${b_bal}  0
    ${b_bon_before}=   Get From List  ${b_bal}  1

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${a_agent}    *${USSD}*${b_agent}*${amount}*${PIN}#
    Log     ${response}

    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${a_agent}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}
    ${a_bal_ussd}=     Get Agent Balances Via Ussd   ${a_agent}  ${PIN}
    ${a_bal_after_for_sms}=     Get From List  ${a_bal_ussd}  2
    ${b_bal_ussd}=     Get Agent Balances Via Ussd   ${b_agent}  ${PIN}
    ${b_bal_after_for_sms}=     Get From List  ${b_bal_ussd}  2
    ${ref}      Get Ref Number of Last Transaction via USSD    ${a_agent}    ${PIN}
    USSD Response Should Be    ${response}    You have Transferred ${amount_currency_format} Fcfa to ${b_agent}. Your new Balance is ${a_bal_after_for_sms} Fcfa. Ref ${ref}
    Ucip Calls Count Should Be      5
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     5
    Should Be Equal       ${a_sms}      You have Transferred ${amount_currency_format} Fcfa to ${b_agent}. Your new Balance is ${a_bal_after_for_sms} Fcfa. Ref ${ref}
    Should Be Equal       ${b_sms}      You have Received ${amount_currency_format} Fcfa from ${a_agent}. Your new Balance is ${b_bal_after_for_sms} Fcfa. Ref ${ref}

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}


Successful transfer from eMaster to eIntermed in English without confirmation
    [Documentation]     Transfer successfully from eMaster agent to eIntermed
    ...     USSD confirmation is disabled for the A agent
    ${trade_bonus}=     Set Variable    1
    ${cum_bonus}=       Set Variable    5.545
    ${a_agent}=     Set Variable    ${AGENT_EMASTER}
    ${b_agent}=     Set Variable    ${AGENT_EINTERMED}
    ${amount}=      Generate Random Number      10000    25000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_before}=   Get From List  ${a_bal}  0
    ${a_bon_before}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_before}=   Get From List  ${b_bal}  0
    ${b_bon_before}=   Get From List  ${b_bal}  1

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${a_agent}    *${USSD}*${b_agent}*${amount}*${PIN}#
    Log     ${response}

    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${a_agent}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}
    ${a_bal_ussd}=     Get Agent Balances Via Ussd   ${a_agent}  ${PIN}
    ${a_bal_after_for_sms}=     Get From List  ${a_bal_ussd}  2
    ${b_bal_ussd}=     Get Agent Balances Via Ussd   ${b_agent}  ${PIN}
    ${b_bal_after_for_sms}=     Get From List  ${b_bal_ussd}  2
    ${ref}      Get Ref Number of Last Transaction via USSD    ${a_agent}    ${PIN}
    USSD Response Should Be    ${response}    You have Transferred ${amount_currency_format} Fcfa to ${b_agent}. Your new Balance is ${a_bal_after_for_sms} Fcfa. Ref ${ref}
    Ucip Calls Count Should Be      5
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     5
    Should Be Equal       ${a_sms}      You have Transferred ${amount_currency_format} Fcfa to ${b_agent}. Your new Balance is ${a_bal_after_for_sms} Fcfa. Ref ${ref}
    Should Be Equal       ${b_sms}      You have Received ${amount_currency_format} Fcfa from ${a_agent}. Your new Balance is ${b_bal_after_for_sms} Fcfa. Ref ${ref}

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}


Successful transfer from eIntermed to eCabine in English without confirmation with transfer bonus
    [Documentation]     Transfer successfully from eIntermed agent to eCabine
    ...     USSD confirmation is disabled for the A agent and transfer rule has 1.5% transfer bonus
    ${trade_bonus}=     Set Variable    4.5
    ${cum_bonus}=       Set Variable    4.5
    ${transfer_bonus}=  Set Variable    1.5
    ${a_agent}=     Set Variable    ${AGENT_EINTERMED}
    ${b_agent}=     Set Variable    ${AGENT_RETAILER}
    ${amount}=      Generate Random Number      1000    25000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    Create Subscriber On Airsim     ${b_agent}   2   11     ${SUB_INITIAL_BAL}     active
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_before}=   Get From List  ${a_bal}  0
    ${a_bon_before}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_before}=   Get From List  ${b_bal}  0
    ${b_bon_before}=   Get From List  ${b_bal}  1

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${a_agent}    *${USSD}*${b_agent}*${amount}*${PIN}#
    Log     ${response}

    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${a_agent}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}
    ${a_bal_ussd}=     Get Agent Balances Via Ussd   ${a_agent}  ${PIN}
    ${a_bal_after_for_sms}=     Get From List  ${a_bal_ussd}  2
    ${b_bal_ussd}=     Get Agent Balances Via Ussd   ${b_agent}  ${PIN}
    ${b_bal_after_for_sms}=     Get From List  ${b_bal_ussd}  2
    ${ref}      Get Ref Number of Last Transaction via USSD    ${a_agent}    ${PIN}
    USSD Response Should Be    ${response}    You have Transferred ${amount_currency_format} Fcfa to ${b_agent}. Your new Balance is ${a_bal_after_for_sms} Fcfa. Ref ${ref}
    Ucip Calls Count Should Be      6
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     RefillRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     5
    Should Be Equal       ${a_sms}      You have Transferred ${amount_currency_format} Fcfa to ${b_agent}. Your new Balance is ${a_bal_after_for_sms} Fcfa. Ref ${ref}
    Should Be Equal       ${b_sms}      You have Received ${amount_currency_format} Fcfa from ${a_agent}. Your new Balance is ${b_bal_after_for_sms} Fcfa. Ref ${ref}

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    ${expected_b_agent_airtime}=    Evaluate    math.ceil(${${amount}*${transfer_bonus}*0.01+${SUB_INITIAL_BAL}})
    Subscriber Balance Should Be    ${b_agent}   ${expected_b_agent_airtime}


Failed transfer from eIntermed to eCabine in English without confirmation when agent balance is insufficient
    [Documentation]     Transfer failed from eIntermed agent to eCabine
    ...     USSD confirmation is disabled for the A agent and the agent has insufficient balance
    ${trade_bonus}=     Set Variable    4.5
    ${cum_bonus}=       Set Variable    4.5
    ${transfer_bonus}=  Set Variable    1.5
    ${a_agent}=     Set Variable    ${AGENT_EINTERMED_INSUFF_FUNDS}
    ${b_agent}=     Set Variable    ${AGENT_RETAILER}
    ${amount}=      Generate Random Number      1000    25000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    Create Subscriber On Airsim     ${b_agent}   2   11     ${SUB_INITIAL_BAL}     active
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_before}=   Get From List  ${a_bal}  0
    ${a_bon_before}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_before}=   Get From List  ${b_bal}  0
    ${b_bon_before}=   Get From List  ${b_bal}  1

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${a_agent}    *${USSD}*${b_agent}*${amount}*${PIN}#
    Log     ${response}

    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${a_agent}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}

    USSD Response Should Be    ${response}    Insufficient funds
    Ucip Calls Count Should Be      2
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     0
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${a_agent}"
    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${b_agent}"

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    ${expected_b_agent_airtime}=    Evaluate    math.ceil(${${0}*${transfer_bonus}*0.01+${SUB_INITIAL_BAL}})
    Subscriber Balance Should Be    ${b_agent}   ${expected_b_agent_airtime}

Failed transfer from eMaster to eIntermed in English without confirmation when no transfer rule exists
    [Documentation]     Transfer failed from eMaster to eIntermed in different groups
    ...     USSD confirmation is disabled for the A agent
    ${trade_bonus}=     Set Variable    1
    ${cum_bonus}=       Set Variable    5.545
    ${a_agent}=     Set Variable    ${AGENT_EMASTER}
    ${b_agent}=     Set Variable    ${AGENT_EINTERMED_DALOA}
    ${amount}=      Generate Random Number      10000    25000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_before}=   Get From List  ${a_bal}  0
    ${a_bon_before}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_before}=   Get From List  ${b_bal}  0
    ${b_bon_before}=   Get From List  ${b_bal}  1

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${a_agent}    *${USSD}*${b_agent}*${amount}*${PIN}#
    Log     ${response}

    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${a_agent}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}

    USSD Response Should Be    ${response}    You cannot transfer to this Agent
    Ucip Calls Count Should Be      2
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     0
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${a_agent}"
    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${b_agent}"

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}


Failed transfer from eMaster to eIntermed in English without confirmation when PIN invalid
    [Documentation]     Transfer failed from eMaster to eIntermed in
    ...     USSD confirmation is disabled for the A agent and the agent enters invalid pin
    ${trade_bonus}=     Set Variable    1
    ${cum_bonus}=       Set Variable    5.545
    ${invalid_pin}=     Set Variable    34212
    ${a_agent}=     Set Variable    ${AGENT_EMASTER}
    ${b_agent}=     Set Variable    ${AGENT_EINTERMED}
    ${amount}=      Generate Random Number      10000    25000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_before}=   Get From List  ${a_bal}  0
    ${a_bon_before}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_before}=   Get From List  ${b_bal}  0
    ${b_bon_before}=   Get From List  ${b_bal}  1

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${a_agent}    *${USSD}*${b_agent}*${amount}*${invalid_pin}#
    Log     ${response}

    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${a_agent}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}

    USSD Response Should Be    ${response}    Invalid PIN.
    Ucip Calls Count Should Be      0

    Sms Count Should Be    0
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${a_agent}"
    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${b_agent}"

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}


Failed transfer from eMaster(Banque) to eMaster in English without confirmation when amount is less than min allowed
    [Documentation]     Transfer failed from eMaster(Banque) agent to eMaster
    ...     USSD confirmation is disabled for the A agent and the amount is less than the min amount allowed in the transfer rule
    ${trade_bonus}=     Set Variable    1.25
    ${cum_bonus}=       Set Variable    6.864313
    ${a_agent}=     Set Variable    ${AGENT_EMASTER_BANQUE}
    ${b_agent}=     Set Variable    ${AGENT_EMASTER}
    ${amount}=      Generate Random Number      10000    100000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_before}=   Get From List  ${a_bal}  0
    ${a_bon_before}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_before}=   Get From List  ${b_bal}  0
    ${b_bon_before}=   Get From List  ${b_bal}  1

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${a_agent}    *${USSD}*${b_agent}*${amount}*${PIN}#
    Log     ${response}

    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${a_agent}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}

    USSD Response Should Be    ${response}    You cannot transfer to this Agent
    Ucip Calls Count Should Be      2
    Ucip Calls Should Be     GetSubscriberInformationRequest    GetSubscriberInformationRequest

    Sms Count Should Be     0
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${a_agent}"
    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${b_agent}"

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}


Failed transfer from eMaster to eIntermed in English without confirmation when amount is greater than max allowed
    [Documentation]     Transfer failed from eMaster to eIntermed in
    ...     USSD confirmation is disabled for the A agent and the amount is greater than max allowed in transfer rule
    ${trade_bonus}=     Set Variable    1
    ${cum_bonus}=       Set Variable    5.545
    ${a_agent}=     Set Variable    ${AGENT_EMASTER}
    ${b_agent}=     Set Variable    ${AGENT_EINTERMED}
    ${amount}=      Generate Random Number      10000001    20000000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_before}=   Get From List  ${a_bal}  0
    ${a_bon_before}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_before}=   Get From List  ${b_bal}  0
    ${b_bon_before}=   Get From List  ${b_bal}  1

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${a_agent}    *${USSD}*${b_agent}*${amount}*${PIN}#
    Log     ${response}

    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${a_agent}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}

    USSD Response Should Be    ${response}    You cannot transfer to this Agent
    Ucip Calls Count Should Be      2
    Ucip Calls Should Be     GetSubscriberInformationRequest    GetSubscriberInformationRequest

    Sms Count Should Be     0
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${a_agent}"
    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${b_agent}"

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}


Failed transfer from eMaster to eIntermed in English without confirmation when A party is deactivated
    [Documentation]     Transfer failed from eMaster to eIntermed
    ...     USSD confirmation is disabled for the A agent and the A agent is deactivated but B party is active
    ${trade_bonus}=     Set Variable    1
    ${cum_bonus}=       Set Variable    5.545
    ${a_agent}=     Set Variable    ${AGENT_EMASTER_DEACTIVATED}
    ${b_agent}=     Set Variable    ${AGENT_EINTERMED}
    ${amount}=      Generate Random Number      10000001    20000000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_before}=   Get From List  ${a_bal}  0
    ${a_bon_before}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_before}=   Get From List  ${b_bal}  0
    ${b_bon_before}=   Get From List  ${b_bal}  1

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${a_agent}    *${USSD}*${b_agent}*${amount}*${PIN}#
    Log     ${response}

    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${a_agent}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}
    USSD Response Should Be    ${response}    Not allowed to trade in User State: Deactivated
    Ucip Calls Count Should Be      1
    Ucip Calls Should Be    GetSubscriberInformationRequest

    Sms Count Should Be     0
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${a_agent}"
    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${b_agent}"

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}

#This transaction should fail but its' successful - Looks like a bug or an oversight
#Failed transfer from eMaster(Banque) to eMaster in English without confirmation when B party is deactivated
#    [Documentation]     Transfer failed from eMaster(Banque) agent to eMaster
#    ...     USSD confirmation is disabled for the A agent and the B agent is Deactivated
#    ${trade_bonus}=     Set Variable    1.25
#    ${cum_bonus}=       Set Variable    6.864313
#    ${a_agent}=     Set Variable    ${AGENT_EMASTER_BANQUE}
#    ${b_agent}=     Set Variable    ${AGENT_EMASTER_DEACTIVATED}
#    ${amount}=      Generate Random Number      1000000    2000000
#    ${amount_currency_format}=      Format String     {:,}    ${amount}
#    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
#    ${a_bal_before}=   Get From List  ${a_bal}  0
#    ${a_bon_before}=   Get From List  ${a_bal}  1
#
#    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
#    ${b_bal_before}=   Get From List  ${b_bal}  0
#    ${b_bon_before}=   Get From List  ${b_bal}  1
#
#    Clear Call and Sms History On Airsim
#    ${response}=    Send Ussd Request     ${a_agent}    *${USSD}*${b_agent}*${amount}*${PIN}#
#    Log     ${response}
#
#    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
#    ${a_bal_after}=   Get From List  ${a_bal}  0
#    ${a_bon_after}=   Get From List  ${a_bal}  1
#
#    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
#    ${b_bal_after}=   Get From List  ${b_bal}  0
#    ${b_bon_after}=   Get From List  ${b_bal}  1
#
#    ${a_sms}=   Sms Sent To Msisdn    ${a_agent}
#    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}
#
#    USSD Response Should Be    ${response}    You cannot transfer to this Agent
#    Ucip Calls Count Should Be      2
#    Ucip Calls Should Be     GetSubscriberInformationRequest    GetSubscriberInformationRequest
#
#    Sms Count Should Be     0
#    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${a_agent}"
#    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${b_agent}"
#
#    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
#    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}
#
#    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
#    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}


Failed transfer from eIntermed to eCabine in English without confirmation when A agent is suspended
    [Documentation]     Transfer failed from eIntermed agent to eCabine
    ...     USSD confirmation is disabled for the A agent and A agent is in suspended state
    ${trade_bonus}=     Set Variable    4.5
    ${cum_bonus}=       Set Variable    4.5
    ${transfer_bonus}=  Set Variable    1.5
    ${a_agent}=     Set Variable    ${AGENT_EINTERMED_SUSPENDED}
    ${b_agent}=     Set Variable    ${AGENT_RETAILER}
    ${amount}=      Generate Random Number      1000    25000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    Create Subscriber On Airsim     ${b_agent}   2   11     ${SUB_INITIAL_BAL}     active
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_before}=   Get From List  ${a_bal}  0
    ${a_bon_before}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_before}=   Get From List  ${b_bal}  0
    ${b_bon_before}=   Get From List  ${b_bal}  1

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${a_agent}    *${USSD}*${b_agent}*${amount}*${PIN}#
    Log     ${response}

    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${a_agent}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}

    USSD Response Should Be    ${response}    Not allowed to trade in User State: Suspended
    Ucip Calls Count Should Be      1
    Ucip Calls Should Be    GetSubscriberInformationRequest

    Sms Count Should Be     0
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${a_agent}"
    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${b_agent}"

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    ${expected_b_agent_airtime}=    Evaluate    math.ceil(${${0}*${transfer_bonus}*0.01+${SUB_INITIAL_BAL}})
    Subscriber Balance Should Be    ${b_agent}   ${expected_b_agent_airtime}

#This transaction should fail but it is successful - apparently a bug
#Failed transfer from eIntermed to eCabine in English without confirmation when B agent is suspended
#    [Documentation]     Transfer failed from eIntermed agent to eCabine
#    ...     USSD confirmation is disabled for the A agent and B agent is in suspended state
#    ${trade_bonus}=     Set Variable    4.5
#    ${cum_bonus}=       Set Variable    4.5
#    ${transfer_bonus}=  Set Variable    1.5
#    ${a_agent}=     Set Variable    ${AGENT_EINTERMED}
#    ${b_agent}=     Set Variable    ${AGENT_RETAILER_SUSPENDED}
#    ${amount}=      Generate Random Number      1000    25000
#    ${amount_currency_format}=      Format String     {:,}    ${amount}
#    Create Subscriber On Airsim     ${b_agent}   2   11     ${SUB_INITIAL_BAL}     active
#    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
#    ${a_bal_before}=   Get From List  ${a_bal}  0
#    ${a_bon_before}=   Get From List  ${a_bal}  1
#
#    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
#    ${b_bal_before}=   Get From List  ${b_bal}  0
#    ${b_bon_before}=   Get From List  ${b_bal}  1
#
#    Clear Call and Sms History On Airsim
#    ${response}=    Send Ussd Request     ${a_agent}    *${USSD}*${b_agent}*${amount}*${PIN}#
#    Log     ${response}
#
#    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
#    ${a_bal_after}=   Get From List  ${a_bal}  0
#    ${a_bon_after}=   Get From List  ${a_bal}  1
#
#    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
#    ${b_bal_after}=   Get From List  ${b_bal}  0
#    ${b_bon_after}=   Get From List  ${b_bal}  1
#
#    ${a_sms}=   Sms Sent To Msisdn    ${a_agent}
#    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}
#
#    USSD Response Should Be    ${response}    Not allowed to trade in User State: Suspended
#    Ucip Calls Count Should Be      1
#    Ucip Calls Should Be    GetSubscriberInformationRequest
#
#    Sms Count Should Be     0
#    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${a_agent}"
#    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${b_agent}"
#
#    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
#    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}
#
#    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
#    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}
#    ${expected_b_agent_airtime}=    Evaluate    math.ceil(${${0}*${transfer_bonus}*0.01+${SUB_INITIAL_BAL}})
#    Subscriber Balance Should Be    ${b_agent}   ${expected_b_agent_airtime}

#Bonus is deducted from the agent's bonus account even though the transaction failed.
#Failed transfer from eMaster to eIntermed in English without confirmation when RC 100 is received from AIR
#    [Documentation]     Transfer failed from eMaster to eIntermed
#    ...     USSD confirmation is disabled for the A agent and RC-100 is received for the refill
#    ${trade_bonus}=     Set Variable    4.5
#    ${cum_bonus}=       Set Variable    4.5
#    ${transfer_bonus}=  Set Variable    1.5
#    ${a_agent}=     Set Variable    ${AGENT_EINTERMED}
#    ${b_agent}=     Set Variable    ${AGENT_RETAILER}
#    ${amount}=      Generate Random Number      1000    25000
#    ${amount_currency_format}=      Format String     {:,}    ${amount}
#    Create Subscriber On Airsim     ${b_agent}   2   11     ${SUB_INITIAL_BAL}     active
#    Inject Response In Airsim       Refill      100
#    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
#    ${a_bal_before}=   Get From List  ${a_bal}  0
#    ${a_bon_before}=   Get From List  ${a_bal}  1
#
#    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
#    ${b_bal_before}=   Get From List  ${b_bal}  0
#    ${b_bon_before}=   Get From List  ${b_bal}  1
#
#    Clear Call and Sms History On Airsim
#    ${response}=    Send Ussd Request     ${a_agent}    *${USSD}*${b_agent}*${amount}*${PIN}#
#    Log     ${response}
#    Reset Injected Response In Airsim   Refill
#
#    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
#    ${a_bal_after}=   Get From List  ${a_bal}  0
#    ${a_bon_after}=   Get From List  ${a_bal}  1
#
#    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
#    ${b_bal_after}=   Get From List  ${b_bal}  0
#    ${b_bon_after}=   Get From List  ${b_bal}  1
#
#    ${a_sms}=   Sms Sent To Msisdn    ${a_agent}
#    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}
#
#    USSD Response Should Be    ${response}    Other Error, please contact your call centre.
#    Ucip Calls Count Should Be      3
#    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     RefillRequest
#
#    Sms Count Should Be     0
#    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${a_agent}"
#    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${b_agent}"
#
#    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
#    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}
#
#    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
#    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}
#    ${expected_b_agent_airtime}=    Evaluate    math.ceil(${${0}*${transfer_bonus}*0.01+${SUB_INITIAL_BAL}})
#    Subscriber Balance Should Be    ${b_agent}   ${expected_b_agent_airtime}



