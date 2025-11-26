*** Settings ***
Resource    ../keywords/ussd.resource
Resource    ../keywords/gui.resource
Suite Setup     Start Airsim

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

*** Test Cases ***

Successful sell airtime to a subscriber in English without confirmation
    [Documentation]     Sell Airtime to a subscriber successfully with both agent and subscriber langusge set to English
    ...     USSD confirmation is disabled for the agent
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable    ${AGENT_RETAILER}
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${agent}    *${USSD}*${subscriber}*${AMOUNT}*${PIN}#
    Log     ${response}

    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after_for_sms}=   Get From List  ${agent_bal}  2
    ${agent_bal_after}=   Get From List  ${agent_bal}  0

    ${ref}      Get Ref Number of Last Transaction via USSD    ${agent}    ${PIN}
    Log     ${ref}
    USSD Response Should Be    ${response}    You have Sold ${AMOUNT} Fcfa Airtime to ${subscriber}. Your new Balance is ${agent_bal_after_for_sms} Fcfa. Ref ${ref}
    Ucip Calls Count Should Be      5
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     RefillRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    ${expected_subs_bal}=      Format String     {:,}    ${${AMOUNT}+${SUB_INITIAL_BAL}}
    Sms Count Should Be     4
    Should Be Equal       ${b_sms}      You have Bought ${AMOUNT} Fcfa Airtime from ${agent}. Your new Balance is ${expected_subs_bal} Fcfa. Ref ${ref}
    Should Be Equal       ${a_sms}      You have Sold ${AMOUNT} Fcfa Airtime to ${subscriber}. Your new Balance is ${agent_bal_after_for_sms} Fcfa. Ref ${ref}
    Subscriber Balance Should Be    ${subscriber}   ${${AMOUNT}+${SUB_INITIAL_BAL}}
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${AMOUNT}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}


Successful sell airtime to a subscriber in English with confirmation
    [Documentation]     Sell Airtime to a subscriber successfully with both agent and subscriber langusge set to English
    ...     USSD confirmation is enabled for the agent
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable    ${AGENT_RETAILER_CONFIRM}
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${agent}    *${USSD}*${subscriber}*${AMOUNT}*${PIN}#     last=False
    Log     ${response}
    ${response}=    Send Ussd Request     ${agent}    1
    Log     ${response}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after_for_sms}=   Get From List  ${agent_bal}  2
    ${agent_bal_after}=   Get From List  ${agent_bal}  0

    ${ref}      Get Ref Number of Last Transaction via USSD    ${agent}    ${PIN}
    Log     ${ref}
    USSD Response Should Be    ${response}    You have Sold ${AMOUNT} Fcfa Airtime to ${subscriber}. Your new Balance is ${agent_bal_after_for_sms} Fcfa. Ref ${ref}
    Ucip Calls Count Should Be      5
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     RefillRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    ${expected_subs_bal}=      Format String     {:,}    ${${AMOUNT}+${SUB_INITIAL_BAL}}
    Sms Count Should Be     4
    Should Be Equal       ${b_sms}      You have Bought ${AMOUNT} Fcfa Airtime from ${agent}. Your new Balance is ${expected_subs_bal} Fcfa. Ref ${ref}
    Should Be Equal       ${a_sms}      You have Sold ${AMOUNT} Fcfa Airtime to ${subscriber}. Your new Balance is ${agent_bal_after_for_sms} Fcfa. Ref ${ref}
    Subscriber Balance Should Be    ${subscriber}   ${${AMOUNT}+${SUB_INITIAL_BAL}}
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${AMOUNT}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}


Failed sell airtime to a subscriber in English without confirmation with insufficient funds
    [Documentation]     Sell Airtime to a subscriber failed with both agent and subscriber language set to English
    ...     USSD confirmation is disabled for the agent and Agent has insufficeint funds for the transaction
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable    ${AGENT_RETAILER_INSUFF_FUNDS}
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${agent}    *${USSD}*${subscriber}*${AMOUNT}*${PIN}#
    Log     ${response}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0

    ${ref}      Get Ref Number of Last Transaction via USSD    ${agent}    ${PIN}
    Log     ${ref}
#Bug in Crediverse (Ussd response is not as per configuration)
    USSD Response Should Be    ${response}    Insufficient funds
    Ucip Calls Count Should Be      4
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     2
    Should Be Equal       ${b_sms}   "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}   "No Message Sent for the MSISDN ${agent}"
    Subscriber Balance Should Be    ${subscriber}   ${${0}+${SUB_INITIAL_BAL}}
    Should Be Equal     ${agent_bal_after}      ${agent_bal_before}


Failed sell airtime to a subscriber in English with confirmation with insufficient funds
    [Documentation]     Sell Airtime to a subscriber failed with both agent and subscriber language set to English
    ...     USSD confirmation is enabled for the agent and Agent has insufficeint funds for the transaction
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable    ${AGENT_RETAILER_INSUFF_FUNDS_CONFIRM}
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${agent}    *${USSD}*${subscriber}*${AMOUNT}*${PIN}#    last=False
    Log     ${response}
    ${response}=    Send Ussd Request     ${agent}    1
    Log     ${response}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
     ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0

    ${ref}      Get Ref Number of Last Transaction via USSD    ${agent}    ${PIN}
    Log     ${ref}
    USSD Response Should Be    ${response}    There are insufficient Balance to perform this transaction.
    Ucip Calls Count Should Be      4
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     2
    Should Be Equal       ${b_sms}   "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}   "No Message Sent for the MSISDN ${agent}"
    Subscriber Balance Should Be    ${subscriber}   ${${0}+${SUB_INITIAL_BAL}}
    Should Be Equal     ${agent_bal_after}      ${agent_bal_before}


Failed sell airtime to a subscriber in English without confirmation when subscriber is not present on AIR
    [Documentation]     Sell Airtime to a subscriber successfully with both agent and subscriber langusge set to English
    ...     USSD confirmation is disabled for the agent and subscriber is not active on AIR
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable    ${AGENT_RETAILER}
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    Delete Subscriber On Airsim     ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${agent}    *${USSD}*${subscriber}*${AMOUNT}*${PIN}#
    Log     ${response}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0

    USSD Response Should Be    ${response}    Invalid Recipient.
    Ucip Calls Count Should Be      4
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     RefillRequest     GetSubscriberInformationRequest
    Sms Count Should Be     1
    Should Be Equal       ${b_sms}   "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}   "No Message Sent for the MSISDN ${agent}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}

Failed sell airtime to a subscriber in English with confirmation when subscriber is not present on AIR
    [Documentation]     Sell Airtime to a subscriber successfully with both agent and subscriber langusge set to English
    ...     USSD confirmation is enabled for the agent and subscriber is not active on AIR
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable    ${AGENT_RETAILER_CONFIRM}
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    Delete Subscriber On Airsim     ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${agent}    *${USSD}*${subscriber}*${AMOUNT}*${PIN}#     last=False
    Log     ${response}
    ${response}=    Send Ussd Request     ${agent}    1
    Log     ${response}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0

    USSD Response Should Be    ${response}    Invalid Recipient.
    Ucip Calls Count Should Be      4
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     RefillRequest     GetSubscriberInformationRequest
    Sms Count Should Be     1
    Should Be Equal       ${b_sms}   "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}   "No Message Sent for the MSISDN ${agent}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}


Failed sell airtime to a subscriber in English without confirmation when AIR is not accessible
    [Documentation]     Sell Airtime to a subscriber successfully with both agent and subscriber langusge set to English
    ...     USSD confirmation is disabled for the agent and AIR is not accessible
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable    ${AGENT_RETAILER}
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    Stop Airsim
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${agent}    *${USSD}*${subscriber}*${AMOUNT}*${PIN}#
    Log     ${response}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    Start Airsim
    USSD Response Should Be    ${response}    A technical error has occurred. Please try again later.
    Ucip Calls Count Should Be      0
    Sms Count Should Be     1
    Should Be Equal       ${b_sms}   "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}   "No Message Sent for the MSISDN ${agent}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}


Failed sell airtime to a subscriber in English with confirmation when AIR is not accessible
    [Documentation]     Sell Airtime to a subscriber successfully with both agent and subscriber langusge set to English
    ...     USSD confirmation is enabled for the agent and AIR is not accessible
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable    ${AGENT_RETAILER_CONFIRM}
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    Stop Airsim
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${agent}    *${USSD}*${subscriber}*${AMOUNT}*${PIN}#     last=False
    Log     ${response}
    ${response}=    Send Ussd Request     ${agent}    1
    Log     ${response}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    Start Airsim
    USSD Response Should Be    ${response}    A technical error has occurred. Please try again later.
    Ucip Calls Count Should Be      0
    Sms Count Should Be     1
    Should Be Equal       ${b_sms}   "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}   "No Message Sent for the MSISDN ${agent}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}

Failed sell airtime to a subscriber in English without confirmation when Agent is Wholesaler
    [Documentation]     Sell Airtime to a subscriber successfully with both agent and subscriber langusge set to English
    ...     USSD confirmation is disabled for the agent and agent tier is wholesaler
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable    ${AGENT_WHOLESALER}
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${agent}    *${USSD}*${subscriber}*${AMOUNT}*${PIN}#
    Log     ${response}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0

    USSD Response Should Be    ${response}    You cannot transfer to this Agent
    Ucip Calls Count Should Be      3
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest
    Sms Count Should Be     1
    Should Be Equal       ${b_sms}   "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}   "No Message Sent for the MSISDN ${agent}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}

Failed sell airtime to a subscriber in English with confirmation when Agent is Wholesaler
    [Documentation]     Sell Airtime to a subscriber successfully with both agent and subscriber langusge set to English
    ...     USSD confirmation is enabled for the agent and agent tier is wholesaler
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable    ${AGENT_WHOLESALER_CONFIRM}
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${agent}    *${USSD}*${subscriber}*${AMOUNT}*${PIN}#      last=False
    Log     ${response}
    ${response}=    Send Ussd Request     ${agent}    1
    Log     ${response}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0

    USSD Response Should Be    ${response}    You cannot transfer to this Agent
    Ucip Calls Count Should Be      3
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest
    Sms Count Should Be     1
    Should Be Equal       ${b_sms}   "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}   "No Message Sent for the MSISDN ${agent}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}


Failed sell airtime to a subscriber in English without confirmation when PIN is invalid
    [Documentation]     Sell Airtime to a subscriber failed with both agent and subscriber language set to English
    ...     USSD confirmation is disabled for the agent and the pin entered in invalid
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable        ${AGENT_RETAILER}
    ${pin_invalid}=     Set Variable        5698
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${agent}    *${USSD}*${subscriber}*${AMOUNT}*${pin_invalid}#
    Log     ${response}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0

    USSD Response Should Be    ${response}    Invalid PIN.
    Ucip Calls Count Should Be      1
    Ucip Calls Should Be    GetSubscriberInformationRequest

    Sms Count Should Be     1
    Should Be Equal       ${b_sms}   "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}   "No Message Sent for the MSISDN ${agent}"
    Subscriber Balance Should Be    ${subscriber}   ${${0}+${SUB_INITIAL_BAL}}
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}

Failed sell airtime to a subscriber in English with confirmation when PIN is invalid
    [Documentation]     Sell Airtime to a subscriber failed with both agent and subscriber language set to English
    ...     USSD confirmation is enabled for the agent and the pin entered in invalid
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable        ${AGENT_RETAILER_CONFIRM}
    ${pin_invalid}=     Set Variable        5698
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${agent}    *${USSD}*${subscriber}*${AMOUNT}*${pin_invalid}#
    Log     ${response}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0

    USSD Response Should Be    ${response}    Invalid PIN.
    Ucip Calls Count Should Be      1
    Ucip Calls Should Be    GetSubscriberInformationRequest

    Sms Count Should Be     1
    Should Be Equal       ${b_sms}   "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}   "No Message Sent for the MSISDN ${agent}"
    Subscriber Balance Should Be    ${subscriber}   ${${0}+${SUB_INITIAL_BAL}}
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}

Failed sell airtime to a subscriber in English without confirmation when RC 999 received from AIR
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
    ${response}=    Send Ussd Request     ${agent}    *${USSD}*${subscriber}*${AMOUNT}*${PIN}#
    Log     ${response}
    Reset Injected Response In Airsim       Refill
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bal_after_for_sms}=   Get From List  ${agent_bal}  2
    ${agent_on_hold_bal_after}=      Get From List  ${agent_bal}  4

    ${ref}      Get Ref Number of Last Transaction via USSD    ${agent}    ${PIN}
    Log     ${ref}
    USSD Response Should Be    ${response}    Technical Error. Sale of Airtime to ${subscriber} may have failed. Your new Balance is ${agent_bal_after_for_sms} Fcfa. Please contact Customer Care to query this transaction if the customer did not receive it. Ref ${ref}
    Ucip Calls Count Should Be      5
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     RefillRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     4
#Looks like a bug - The message should be in English
    Should Be Equal       ${b_sms}   Erreur technique. L'achat de credit de ${agent} peut avoir echoue. Veuillez contacter Customer Care pour plus de details sur cette transaction si vous ne l'avez pas recue. Ref ${ref}
    Should Be Equal       ${a_sms}   Technical Error. Sale of Airtime to ${subscriber} may have failed. Your new Balance is ${agent_bal_after_for_sms} Fcfa. Please contact Customer Care to query this transaction if the customer did not receive it. Ref ${ref}
    Subscriber Balance Should Be    ${subscriber}   ${${0}+${SUB_INITIAL_BAL}}
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${AMOUNT}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    ${expected_on_hold_bal}=  Evaluate    ${agent_on_hold_bal_before} + ${AMOUNT}
    Should Be Equal     ${agent_on_hold_bal_after}      ${expected_on_hold_bal}


Failed sell airtime to a subscriber in English with confirmation when RC 999 received from AIR
    [Documentation]     Sell Airtime to a subscriber failed with both agent and subscriber language set to English
    ...     USSD confirmation is enabled for the agent and RC 999 is received from AIR for the Refill
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable        ${AGENT_RETAILER_CONFIRM}
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_on_hold_bal_before}=      Get From List  ${agent_bal}  4

    Clear Call and Sms History On Airsim
    Inject Response In Airsim       Refill      999
    ${response}=    Send Ussd Request     ${agent}    *${USSD}*${subscriber}*${AMOUNT}*${PIN}#      last=False
    Log     ${response}
    ${response}=    Send Ussd Request     ${agent}    1
    Log     ${response}
    Reset Injected Response In Airsim       Refill
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bal_after_for_sms}=   Get From List  ${agent_bal}  2
    ${agent_on_hold_bal_after}=      Get From List  ${agent_bal}  4

    ${ref}      Get Ref Number of Last Transaction via USSD    ${agent}    ${PIN}
    Log     ${ref}
    USSD Response Should Be    ${response}    Technical Error. Sale of Airtime to ${subscriber} may have failed. Your new Balance is ${agent_bal_after_for_sms} Fcfa. Please contact Customer Care to query this transaction if the customer did not receive it. Ref ${ref}
    Ucip Calls Count Should Be      5
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     RefillRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     4
#Looks like a bug - The message should be in English
    Should Be Equal       ${b_sms}   Erreur technique. L'achat de credit de ${agent} peut avoir echoue. Veuillez contacter Customer Care pour plus de details sur cette transaction si vous ne l'avez pas recue. Ref ${ref}
    Should Be Equal       ${a_sms}   Technical Error. Sale of Airtime to ${subscriber} may have failed. Your new Balance is ${agent_bal_after_for_sms} Fcfa. Please contact Customer Care to query this transaction if the customer did not receive it. Ref ${ref}
    Subscriber Balance Should Be    ${subscriber}   ${${0}+${SUB_INITIAL_BAL}}
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${AMOUNT}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    ${expected_on_hold_bal}=  Evaluate    ${agent_on_hold_bal_before} + ${AMOUNT}
    Should Be Equal     ${agent_on_hold_bal_after}      ${expected_on_hold_bal}

Failed sell airtime to a subscriber in English without confirmation when amount is less than minimum allowed
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
    ${response}=    Send Ussd Request     ${agent}    *${USSD}*${subscriber}*${amount}*${PIN}#
    Log     ${response}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bal_after_for_sms}=   Get From List  ${agent_bal}  2
    ${agent_on_hold_bal_after}=      Get From List  ${agent_bal}  4

    USSD Response Should Be    ${response}    You cannot transfer to this Agent
    Ucip Calls Count Should Be      3
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     1

    Should Be Equal       ${b_sms}   "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}   "No Message Sent for the MSISDN ${agent}"
    Subscriber Balance Should Be    ${subscriber}   ${${0}+${SUB_INITIAL_BAL}}
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    ${expected_on_hold_bal}=  Evaluate    ${agent_on_hold_bal_before} + ${0}
    Should Be Equal     ${agent_on_hold_bal_after}      ${expected_on_hold_bal}


Failed sell airtime to a subscriber in English with confirmation when amount is less than minimum allowed
    [Documentation]     Sell Airtime to a subscriber failed with both agent and subscriber language set to English
    ...     USSD confirmation is enabled for the agent and and the amount is less than the minimum allowed value
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${agent}=   Set Variable        ${AGENT_RETAILER_CONFIRM}
    ${amount}=  Set Variable      10
    Create Subscriber On Airsim     ${subscriber}   2   11     ${SUB_INITIAL_BAL}     active
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_on_hold_bal_before}=      Get From List  ${agent_bal}  4

    Clear Call and Sms History On Airsim
    ${response}=    Send Ussd Request     ${agent}    *${USSD}*${subscriber}*${amount}*${PIN}#      last=False
    Log     ${response}
    ${response}=    Send Ussd Request     ${agent}    1
    Log     ${response}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${agent_bal}=     Get Agent Balances Via Ussd   ${agent}  ${PIN}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bal_after_for_sms}=   Get From List  ${agent_bal}  2
    ${agent_on_hold_bal_after}=      Get From List  ${agent_bal}  4

    USSD Response Should Be    ${response}    You cannot transfer to this Agent
    Ucip Calls Count Should Be      3
    Ucip Calls Should Be    GetSubscriberInformationRequest     GetSubscriberInformationRequest     GetSubscriberInformationRequest

    Sms Count Should Be     1

    Should Be Equal       ${b_sms}   "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}   "No Message Sent for the MSISDN ${agent}"
    Subscriber Balance Should Be    ${subscriber}   ${${0}+${SUB_INITIAL_BAL}}
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    ${expected_on_hold_bal}=  Evaluate    ${agent_on_hold_bal_before} + ${0}
    Should Be Equal     ${agent_on_hold_bal_after}      ${expected_on_hold_bal}


#Repeat all the above tests with french language
#Refill request timeout failure
#Amount more than max limit
#Area based restrictions
#