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
    Start Airsim
    Connect To OLTP DB

Teardown For The Suite
    Disconnect From Database

*** Test Cases ***
Successful transfer from eStore to eMaster(Banque) via api in English without confirmation
    [Documentation]     Transfer successfully via api from eStore agent to eMaster (banque)
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
    ${api_session}=     Create Session For API      ${ESTORE_API_USER_USERNAME}      ${ESTORE_API_USER_PASSWORD}
    ${response}   Transfer to a target MSISDN via API  ${api_session}     ${b_agent}    ${amount}
    ${ref}=     Get From List    ${response}    0

    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${AGENT_ESTORE_API_USER_MSISDN}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}
    ${a_bal_ussd}=     Get Agent Balances Via Ussd   ${a_agent}  ${PIN}
    ${a_bal_after_for_sms}=     Get From List  ${a_bal_ussd}  2
    ${b_bal_ussd}=     Get Agent Balances Via Ussd   ${b_agent}  ${PIN}
    ${b_bal_after_for_sms}=     Get From List  ${b_bal_ussd}  2
    Ucip Calls Count Should Be      4
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     4
    Should Be Equal       ${a_sms}      You have Transferred ${amount_currency_format} Fcfa to ${b_agent}. Your new Balance is ${a_bal_after_for_sms} Fcfa. Ref ${ref}
    Should Be Equal       ${b_sms}      You have Received ${amount_currency_format} Fcfa from ${a_agent}. Your new Balance is ${b_bal_after_for_sms} Fcfa. Ref ${ref}

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

Successful transfer from eStore to eCabine International via api in English with confirmation and transfer bonus
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
    ${api_session}=     Create Session For API      ${ESTORE_API_USER_CONFIRM_USERNAME}      ${ESTORE_API_USER_CONFIRM_PASSWORD}
    ${response}   Transfer to a target MSISDN via API  ${api_session}     ${b_agent}    ${amount}
    ${ref}=     Get From List    ${response}    0

    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${AGENT_ESTORE_API_USER_MSISDN_CONFIRM}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}
    ${a_bal_ussd}=     Get Agent Balances Via Ussd   ${a_agent}  ${PIN}
    ${a_bal_after_for_sms}=     Get From List  ${a_bal_ussd}  2
    ${b_bal_ussd}=     Get Agent Balances Via Ussd   ${b_agent}  ${PIN}
    ${b_bal_after_for_sms}=     Get From List  ${b_bal_ussd}  2
    Ucip Calls Count Should Be      5
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     RefillRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     4
    Should Be Equal       ${a_sms}      You have Transferred ${amount_currency_format} Fcfa to ${b_agent}. Your new Balance is ${a_bal_after_for_sms} Fcfa. Ref ${ref}
    Should Be Equal       ${b_sms}      You have Received ${amount_currency_format} Fcfa from ${a_agent}. Your new Balance is ${b_bal_after_for_sms} Fcfa. Ref ${ref}

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    ${expected_b_agent_airtime}=    Evaluate    math.ceil(${${amount}*${transfer_bonus}*0.01+${SUB_INITIAL_BAL}})
    Subscriber Balance Should Be    ${b_agent}   ${expected_b_agent_airtime}

Successful transfer from eMaster(Banque) to eMaster via api in English without confirmation
    [Documentation]     Transfer successfully from eMaster(Banque) agent to eMaster via api
    ...     USSD confirmation is disabled for the A agent
    ${trade_bonus}=     Set Variable    1.25
    ${cum_bonus}=       Set Variable    6.864313
    ${a_agent}=     Set Variable    ${AGENT_EMASTER_BANQUE}
    ${b_agent}=     Set Variable    ${AGENT_EMASTER}
    ${amount}=      Generate Random Number       1000000    2000000
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_before}=   Get From List  ${a_bal}  0
    ${a_bon_before}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_before}=   Get From List  ${b_bal}  0
    ${b_bon_before}=   Get From List  ${b_bal}  1

    Clear Call and Sms History On Airsim
    ${api_session}=     Create Session For API      ${EMASTER_BANQUE_API_USER_USERNAME}      ${AGENT_EMASTER_BANQUE_API_USER_PASSWORD}
    ${response}   Transfer to a target MSISDN via API  ${api_session}     ${b_agent}    ${amount}
    ${ref}=     Get From List    ${response}    0

    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${AGENT_EMASTER_BANQUE_API_USER_MSISDN}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}
    ${a_bal_ussd}=     Get Agent Balances Via Ussd   ${a_agent}  ${PIN}
    ${a_bal_after_for_sms}=     Get From List  ${a_bal_ussd}  2
    ${b_bal_ussd}=     Get Agent Balances Via Ussd   ${b_agent}  ${PIN}
    ${b_bal_after_for_sms}=     Get From List  ${b_bal_ussd}  2

    Ucip Calls Count Should Be      4
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     4
    Should Be Equal       ${a_sms}      You have Transferred ${amount_currency_format} Fcfa to ${b_agent}. Your new Balance is ${a_bal_after_for_sms} Fcfa. Ref ${ref}
    Should Be Equal       ${b_sms}      You have Received ${amount_currency_format} Fcfa from ${a_agent}. Your new Balance is ${b_bal_after_for_sms} Fcfa. Ref ${ref}

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

Successful transfer from eMaster to eIntermed via api in English without confirmation
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
    ${api_session}=     Create Session For API      ${AGENT_EMASTER_API_USER_USERNAME}      ${AGENT_EMASTER_API_USER_PASSWORD}
    ${response}   Transfer to a target MSISDN via API  ${api_session}     ${b_agent}    ${amount}
    ${ref}=     Get From List    ${response}    0

    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${AGENT_EMASTER_API_USER_MSISDN}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}
    ${a_bal_ussd}=     Get Agent Balances Via Ussd   ${a_agent}  ${PIN}
    ${a_bal_after_for_sms}=     Get From List  ${a_bal_ussd}  2
    ${b_bal_ussd}=     Get Agent Balances Via Ussd   ${b_agent}  ${PIN}
    ${b_bal_after_for_sms}=     Get From List  ${b_bal_ussd}  2

    Ucip Calls Count Should Be      4
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     4
    Should Be Equal       ${a_sms}      You have Transferred ${amount_currency_format} Fcfa to ${b_agent}. Your new Balance is ${a_bal_after_for_sms} Fcfa. Ref ${ref}
    Should Be Equal       ${b_sms}      You have Received ${amount_currency_format} Fcfa from ${a_agent}. Your new Balance is ${b_bal_after_for_sms} Fcfa. Ref ${ref}

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

Successful transfer from eIntermed to eCabine via api in English without confirmation with transfer bonus
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
    ${api_session}=     Create Session For API      ${AGENT_EINTERMED_API_USER_USERNAME}      ${AGENT_EINTERMED_API_USER_PASSWORD}
    ${response}   Transfer to a target MSISDN via API  ${api_session}     ${b_agent}    ${amount}
    ${ref}=     Get From List    ${response}    0

    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${AGENT_EINTERMED_API_USER_MSISDN}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}
    ${a_bal_ussd}=     Get Agent Balances Via Ussd   ${a_agent}  ${PIN}
    ${a_bal_after_for_sms}=     Get From List  ${a_bal_ussd}  2
    ${b_bal_ussd}=     Get Agent Balances Via Ussd   ${b_agent}  ${PIN}
    ${b_bal_after_for_sms}=     Get From List  ${b_bal_ussd}  2
    Ucip Calls Count Should Be      5
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     RefillRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     4
    Should Be Equal       ${a_sms}      You have Transferred ${amount_currency_format} Fcfa to ${b_agent}. Your new Balance is ${a_bal_after_for_sms} Fcfa. Ref ${ref}
    Should Be Equal       ${b_sms}      You have Received ${amount_currency_format} Fcfa from ${a_agent}. Your new Balance is ${b_bal_after_for_sms} Fcfa. Ref ${ref}

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${amount}   ${trade_bonus}   ${cum_bonus}
    ${expected_b_agent_airtime}=    Evaluate    math.ceil(${${amount}*${transfer_bonus}*0.01+${SUB_INITIAL_BAL}})
    Subscriber Balance Should Be    ${b_agent}   ${expected_b_agent_airtime}

Failed transfer from eIntermed to eCabine via api in English without confirmation when agent balance is insufficient
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
    ${api_session}=     Create Session For API      ${AGENT_EINTERMED_INSUFF_FUNDS_API_USER_USERNAME}      ${AGENT_EINTERMED_INSUFF_FUNDS_API_USER_PASSWORD}
    ${response}   Transfer to a target MSISDN via API  ${api_session}     ${b_agent}    ${amount}   412
    ${ref}=     Get From List    ${response}    0
    ${response_message}=     Get From List    ${response}    1
    ${response_status}=     Get From List    ${response}     2
    Should Be Equal    ${response_message}     INSUFFICIENT_FUNDS
    Should Be Equal    ${response_status}      PRECONDITION_FAILED

    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${AGENT_EINTERMED_INSUFF_FUNDS_API_USER_MSISDN}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}

    Ucip Calls Count Should Be      2
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     0
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${AGENT_EINTERMED_INSUFF_FUNDS_API_USER_MSISDN}"
    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${b_agent}"

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    ${expected_b_agent_airtime}=    Evaluate    math.ceil(${${0}*${transfer_bonus}*0.01+${SUB_INITIAL_BAL}})
    Subscriber Balance Should Be    ${b_agent}   ${expected_b_agent_airtime}

Failed transfer from eMaster to eIntermed via api in English without confirmation when no transfer rule exists
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
    ${api_session}=     Create Session For API      ${AGENT_EMASTER_API_USER_USERNAME}      ${AGENT_EMASTER_API_USER_PASSWORD}
    Get List Of Transactions    ${api_session}
    ${response}   Transfer to a target MSISDN via API  ${api_session}     ${b_agent}    ${amount}   412
    ${ref}=     Get From List    ${response}    0
    ${response_message}=     Get From List    ${response}    1
    ${response_status}=     Get From List    ${response}     2
    Should Be Equal    ${response_message}     NO_TRANSFER_RULE
    Should Be Equal    ${response_status}      PRECONDITION_FAILED

    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${AGENT_EMASTER_API_USER_MSISDN}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}

    Ucip Calls Count Should Be      2
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     0
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${AGENT_EMASTER_API_USER_MSISDN}"
    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${b_agent}"

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}

Failed transfer from eMaster(Banque) to eMaster via api in English without confirmation when amount is less than min allowed
    [Documentation]     Transfer failed from eMaster(Banque) agent to eMaster
    ...     USSD confirmation is disabled for the A agent and the amount is less than the min amount allowed in the transfer rule
    ${trade_bonus}=     Set Variable    1.25
    ${cum_bonus}=       Set Variable    6.864313
    ${a_agent}=     Set Variable    ${AGENT_EMASTER_BANQUE}
    ${b_agent}=     Set Variable    ${AGENT_EMASTER}
    ${amount}=      Generate Random Number      1    10
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_before}=   Get From List  ${a_bal}  0
    ${a_bon_before}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_before}=   Get From List  ${b_bal}  0
    ${b_bon_before}=   Get From List  ${b_bal}  1

    Clear Call and Sms History On Airsim
    ${api_session}=     Create Session For API      ${EMASTER_BANQUE_API_USER_USERNAME}      ${AGENT_EMASTER_BANQUE_API_USER_PASSWORD}
    ${response}   Transfer to a target MSISDN via API  ${api_session}     ${b_agent}    ${amount}   412
    ${ref}=     Get From List    ${response}    0
    ${response_message}=     Get From List    ${response}    1
    ${response_status}=     Get From List    ${response}     2
    Should Be Equal    ${response_message}     NO_TRANSFER_RULE
    Should Be Equal    ${response_status}      PRECONDITION_FAILED


    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${AGENT_EMASTER_BANQUE_API_USER_MSISDN}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}

    Ucip Calls Count Should Be      2
    Ucip Calls Should Be     GetSubscriberInformationRequest    GetSubscriberInformationRequest

    Sms Count Should Be     0
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${AGENT_EMASTER_BANQUE_API_USER_MSISDN}"
    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${b_agent}"

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}

Failed transfer from eMaster to eIntermed via api in English without confirmation when amount is greater than max allowed
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
    ${api_session}=     Create Session For API      ${AGENT_EMASTER_API_USER_USERNAME}      ${AGENT_EMASTER_API_USER_PASSWORD}
    ${response}   Transfer to a target MSISDN via API  ${api_session}     ${b_agent}    ${amount}   412
    ${ref}=     Get From List    ${response}    0
    ${response_message}=     Get From List    ${response}    1
    ${response_status}=     Get From List    ${response}     2
    Should Be Equal    ${response_message}     NO_TRANSFER_RULE
    Should Be Equal    ${response_status}      PRECONDITION_FAILED


    ${a_bal}=     Get Agent Balances From DB   ${a_agent}
    ${a_bal_after}=   Get From List  ${a_bal}  0
    ${a_bon_after}=   Get From List  ${a_bal}  1

    ${b_bal}=     Get Agent Balances From DB   ${b_agent}
    ${b_bal_after}=   Get From List  ${b_bal}  0
    ${b_bon_after}=   Get From List  ${b_bal}  1

    ${a_sms}=   Sms Sent To Msisdn    ${AGENT_EMASTER_API_USER_MSISDN}
    ${b_sms}=   Sms Sent To Msisdn    ${b_agent}

    Ucip Calls Count Should Be      2
    Ucip Calls Should Be     GetSubscriberInformationRequest    GetSubscriberInformationRequest

    Sms Count Should Be     0
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${AGENT_EMASTER_API_USER_MSISDN}"
    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${b_agent}"

    Balance Check A Party   ${a_bal_before}    ${a_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check A Party     ${a_bon_before}    ${a_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}

    Balance Check B Party   ${b_bal_before}    ${b_bal_after}    ${0}   ${trade_bonus}   ${cum_bonus}
    Bonus Check B Party     ${b_bon_before}    ${b_bon_after}    ${0}   ${trade_bonus}   ${cum_bonus}

Failed transfer from eIntermed to eCabine via api in English when A agent is suspended
    [Documentation]     Transfer failed from eIntermed agent to eCabine
    ...     USSD confirmation is disabled for the A agent and A agent is in suspended state
    ${access_token}=     Get Access Token      ${AGENT_EINTERMED_SUSPENDED_API_USER_USERNAME}      ${INVALID_PASSWORD}      400
    Should Be Equal As Strings    ${access_token}   None


Failed transfer from eIntermed to eCabine via api in English when A agent is deactivated
    [Documentation]     Transfer failed from eMaster agent to eCabine
    ...     USSD confirmation is disabled for the A agent and A agent is in deactivated state
    ${access_token}=     Get Access Token      ${AGENT_EMASTER_DEACTIVATED_API_USER_USERNAME}      ${INVALID_PASSWORD}      400
    Should Be Equal As Strings    ${access_token}   None