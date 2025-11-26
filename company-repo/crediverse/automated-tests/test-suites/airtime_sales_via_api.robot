*** Settings ***
Resource    ../keywords/ussd.resource
Resource    ../keywords/gui.resource
Resource    ../keywords/api.resource
Suite Setup     Setup For The Suite
Suite Teardown      Teardown For The Suite

*** Variables ***

${USSD}=     410
${PIN}=  00000
${AMOUNT}   90
${SUB_INITIAL_BAL}      7000
${AGENT_RETAILER}   0142307792
${AGENT_RETAILER_CONFIRM}   0142307939
${AGENT_RETAILER_INSUFF_FUNDS}     0142307679
${AGENT_RETAILER_INSUFF_FUNDS_CONFIRM}     0142308147
${AGENT_WHOLESALER}     0140399328
${AGENT_WHOLESALER_CONFIRM}     0140383517

*** Keywords ***
Setup For The Suite
    Start Airsim
    Connect To OLTP DB

Teardown For The Suite
    Disconnect From Database

*** Test Cases ***
Successful sell airtime to a subscriber in English without confirmation via API
    [Documentation]     Sell Airtime to a subscriber via API successfully with both agent and subscriber langusge set to English
    ...     USSD confirmation is disabled for the agent
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${amount}=      Generate Random Number        1000    1500
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_RETAILER}
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0

    Clear Call and Sms History On Airsim
    ${api_session}=     Create Session For API      ${ECABINE_API_USER_USERNAME}      ${ECABINE_API_USER_PASSWORD}
    ${ref}   Sell airtime to a target MSISDN via API    ${api_session}     ${subscriber}    ${amount}

    ${a_sms}=   Sms Sent To Msisdn    ${ECABINE_API_USER_MSISDN}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bal_after_for_sms}=   Change Balance Format To Currency    ${agent_bal_after}

    ${ref}      Get Ref Number of Last Transaction via USSD    ${agent}    ${PIN}
    Log     ${ref}
    Ucip Calls Count Should Be      4
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     RefillRequest       GetSubscriberInformationRequest

    ${expected_subs_bal}=      Format String     {:,}    ${${amount}+${SUB_INITIAL_BAL}}
    Sms Count Should Be     3
    Should Be Equal       ${b_sms}      You have Bought ${amount_currency_format} Fcfa Airtime from ${ECABINE_API_USER_MSISDN}. Your new Balance is ${expected_subs_bal} Fcfa. Ref ${ref}
    Should Be Equal       ${a_sms}      You have Sold ${amount_currency_format} Fcfa Airtime to ${subscriber}. Your new Balance is ${agent_bal_after_for_sms} Fcfa. Ref ${ref}
    Subscriber Balance Should Be    ${subscriber}   ${${amount}+${SUB_INITIAL_BAL}}
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${amount}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}

Successful sell airtime to a subscriber in English with confirmation via API
    [Documentation]     Sell Airtime to a subscriber via API successfully with both agent and subscriber langusge set to English
    ...     USSD confirmation is disabled for the agent
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${amount}=      Generate Random Number        1000    1500
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_RETAILER_CONFIRM}
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0

    Clear Call and Sms History On Airsim
    ${api_session}=     Create Session For API      ${ECABINE_API_USER_CONFIRM_USERNAME}      ${ECABINE_API_USER_CONFIRM_PASSWORD}
    ${ref}   Sell airtime to a target MSISDN via API  ${api_session}     ${subscriber}    ${amount}

    ${a_sms}=   Sms Sent To Msisdn    ${ECABINE_API_USER_CONFIRM_MSISDN}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bal_after_for_sms}=   Change Balance Format To Currency    ${agent_bal_after}
    ${ref}      Get Ref Number of Last Transaction via USSD    ${agent}    ${PIN}
    Log     ${ref}
    Ucip Calls Count Should Be      4
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     RefillRequest       GetSubscriberInformationRequest

    ${expected_subs_bal}=      Format String     {:,}    ${${amount}+${SUB_INITIAL_BAL}}
    Sms Count Should Be     3
    Should Be Equal       ${b_sms}      You have Bought ${amount_currency_format} Fcfa Airtime from ${ECABINE_API_USER_CONFIRM_MSISDN}. Your new Balance is ${expected_subs_bal} Fcfa. Ref ${ref}
    Should Be Equal       ${a_sms}      You have Sold ${amount_currency_format} Fcfa Airtime to ${subscriber}. Your new Balance is ${agent_bal_after_for_sms} Fcfa. Ref ${ref}
    Subscriber Balance Should Be    ${subscriber}   ${${amount}+${SUB_INITIAL_BAL}}
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${amount}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}

Failed sell airtime to a subscriber via api in English with insufficient funds
    [Documentation]     Sell Airtime to a subscriber via api failed with both agent and subscriber language set to English
    ...     USSD confirmation is disabled for the agent and Agent has insufficeint funds for the transaction
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${amount}=      Generate Random Number        1000    1500
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_RETAILER_INSUFF_FUNDS}
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0

    Clear Call and Sms History On Airsim
    ${api_session}=     Create Session For API      ${ECABINE_API_USER_INSUFF_FUNDS_USERNAME}      ${ECABINE_API_USER_INSUFF_FUNDS_PASSWORD}
    ${ref}   Sell airtime to a target MSISDN via API  ${api_session}     ${subscriber}    ${amount}     412

    ${a_sms}=   Sms Sent To Msisdn    ${ECABINE_API_USER_INSUFF_FUNDS_MSISDN}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    Ucip Calls Count Should Be      2
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     0
    Should Be Equal       ${a_sms}   "No Message Sent for the MSISDN ${ECABINE_API_USER_INSUFF_FUNDS_MSISDN}"
    Should Be Equal       ${b_sms}   "No Message Sent for the MSISDN ${subscriber}"
    Subscriber Balance Should Be    ${subscriber}   ${${0}+${SUB_INITIAL_BAL}}
    Should Be Equal     ${agent_bal_after}      ${agent_bal_before}

Failed sell airtime to a subscriber via api in English when subscriber is not present on AIR
    [Documentation]     Sell Airtime to a subscriber via api failed when subscriber is not present on AIR
    ...     USSD confirmation is disabled for the agent and subscriber is not active on AIR
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${amount}=      Generate Random Number        1000    1500
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_RETAILER}
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    Delete Subscriber On Airsim     ${subscriber}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0

    Clear Call and Sms History On Airsim
    ${api_session}=     Create Session For API      ${ECABINE_API_USER_USERNAME}      ${ECABINE_API_USER_PASSWORD}
    ${ref}   Sell airtime to a target MSISDN via API  ${api_session}     ${subscriber}    ${amount}     500

    ${a_sms}=   Sms Sent To Msisdn    ${ECABINE_API_USER_MSISDN}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0

    Ucip Calls Count Should Be      3
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     RefillRequest
    Sms Count Should Be     0
    Should Be Equal       ${a_sms}   "No Message Sent for the MSISDN ${ECABINE_API_USER_MSISDN}"
    Should Be Equal       ${b_sms}   "No Message Sent for the MSISDN ${subscriber}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}

Failed sell airtime to a subscriber in English when AIR is not accessible
    [Documentation]     Sell Airtime to a subscriber successfully with both agent and subscriber langusge set to English
    ...     USSD confirmation is enabled for the agent and AIR is not accessible
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable    ${AGENT_RETAILER_CONFIRM}
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    Stop Airsim
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0

    Clear Call and Sms History On Airsim
    ${api_session}=     Create Session For API      ${ECABINE_API_USER_CONFIRM_USERNAME}      ${ECABINE_API_USER_CONFIRM_PASSWORD}
    ${ref}   Sell airtime to a target MSISDN via API  ${api_session}     ${subscriber}    ${amount}     412

    ${a_sms}=   Sms Sent To Msisdn    ${ECABINE_API_USER_CONFIRM_MSISDN}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    Start Airsim

    Ucip Calls Count Should Be      0
    Sms Count Should Be     1
    Should Be Equal       ${a_sms}   "No Message Sent for the MSISDN ${ECABINE_API_USER_CONFIRM_MSISDN}"
    Should Be Equal       ${b_sms}   "No Message Sent for the MSISDN ${subscriber}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}


Failed sell airtime to a subscriber via api in English when Agent is Wholesaler
    [Documentation]     Sell Airtime to a subscriber via api successfully with both agent and subscriber langusge set to English
    ...     USSD confirmation is enabled for the agent and agent tier is wholesaler
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${amount}=      Generate Random Number        1000    1500
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_WHOLESALER_CONFIRM}
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0

    Clear Call and Sms History On Airsim
    ${api_session}=     Create Session For API      ${WHOLESALER_API_USER_CONFIRM_USERNAME}      ${WHOLESALER_API_USER_CONFIRM_PASSWORD}
    ${ref}   Sell airtime to a target MSISDN via API  ${api_session}     ${subscriber}    ${amount}     412

    ${a_sms}=   Sms Sent To Msisdn    ${WHOLESALER_API_USER_CONFIRM_MSISDN}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0

    Ucip Calls Count Should Be      2
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest
    Sms Count Should Be     0
    Should Be Equal       ${a_sms}   "No Message Sent for the MSISDN ${WHOLESALER_API_USER_CONFIRM_MSISDN}"
    Should Be Equal       ${b_sms}   "No Message Sent for the MSISDN ${subscriber}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}

Failed sell airtime to a subscriber in English when RC 999 received from AIR
    [Documentation]     Sell Airtime to a subscriber failed with both agent and subscriber language set to English
    ...     USSD confirmation is disabled for the agent and RC 999 is received from AIR for the Refill
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable        ${AGENT_RETAILER}
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_on_hold_bal_before}=      Get From List  ${agent_bal}  4

    Clear Call and Sms History On Airsim
    Inject Response In Airsim       Refill      999
    ${api_session}=     Create Session For API      ${ECABINE_API_USER_USERNAME}      ${ECABINE_API_USER_PASSWORD}
    ${ref}   Sell airtime to a target MSISDN via API  ${api_session}     ${subscriber}    ${amount}     200

    Reset Injected Response In Airsim       Refill
    ${a_sms}=   Sms Sent To Msisdn    ${ECABINE_API_USER_MSISDN}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bal_after_for_sms}=   Get From List  ${agent_bal}  2
    ${agent_on_hold_bal_after}=      Get From List  ${agent_bal}  4

    ${ref}      Get Ref Number of Last Transaction via USSD    ${agent}    ${PIN}
    Log     ${ref}
    Ucip Calls Count Should Be      5
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     RefillRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     4
#Looks like a bug - The message should be in English
    Should Be Equal       ${b_sms}   Erreur technique. L'achat de credit de ${ECABINE_API_USER_MSISDN} peut avoir echoue. Veuillez contacter Customer Care pour plus de details sur cette transaction si vous ne l'avez pas recue. Ref ${ref}
    Should Be Equal       ${a_sms}   Technical Error. Sale of Airtime to ${subscriber} may have failed. Your new Balance is ${agent_bal_after_for_sms} Fcfa. Please contact Customer Care to query this transaction if the customer did not receive it. Ref ${ref}
    Subscriber Balance Should Be    ${subscriber}   ${${0}+${SUB_INITIAL_BAL}}
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${AMOUNT}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    ${expected_on_hold_bal}=  Evaluate    ${agent_on_hold_bal_before} + ${AMOUNT}
    Should Be Equal     ${agent_on_hold_bal_after}      ${expected_on_hold_bal}


Failed sell airtime to a subscriber in English when amount is less than minimum allowed
    [Documentation]     Sell Airtime to a subscriber failed with both agent and subscriber language set to English
    ...     USSD confirmation is disabled for the agent and and the amount is less than the minimum allowed value
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable        ${AGENT_RETAILER}
    ${amount}=  Set Variable      10
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_on_hold_bal_before}=      Get From List  ${agent_bal}  4

    Clear Call and Sms History On Airsim
    ${api_session}=     Create Session For API      ${ECABINE_API_USER_USERNAME}      ${ECABINE_API_USER_PASSWORD}
    ${ref}   Sell airtime to a target MSISDN via API  ${api_session}     ${subscriber}    ${amount}     412

    ${a_sms}=   Sms Sent To Msisdn    ${ECABINE_API_USER_MSISDN}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bal_after_for_sms}=   Get From List  ${agent_bal}  2
    ${agent_on_hold_bal_after}=      Get From List  ${agent_bal}  4

    Ucip Calls Count Should Be      3
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     1

    Should Be Equal       ${b_sms}   "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}   "No Message Sent for the MSISDN ${ECABINE_API_USER_MSISDN}"
    Subscriber Balance Should Be    ${subscriber}   ${${0}+${SUB_INITIAL_BAL}}
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    ${expected_on_hold_bal}=  Evaluate    ${agent_on_hold_bal_before} + ${0}
    Should Be Equal     ${agent_on_hold_bal_after}      ${expected_on_hold_bal}

