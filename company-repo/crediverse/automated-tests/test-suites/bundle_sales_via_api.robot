*** Settings ***
Resource    ../keywords/ussd.resource
Resource    ../keywords/gui.resource
Resource    ../keywords/api.resource
Suite Setup     Setup For The Suite
Suite Teardown      Teardown For The Suite

*** Variables ***

${PIN}=  00000
${SUB_INITIAL_BAL}      7000
${AGENT_RETAILER}   0142307792
${AGENT_RETAILER_CONFIRM}   0142307939
${AGENT_RETAILER_INSUFF_FUNDS}     0142307679
${AGENT_RETAILER_INSUFF_FUNDS_CONFIRM}     0142308147
${AGENT_RETAILER_SUSPENDED}     0160618827
${AGENT_RETAILER_DEACTIVATED}   0160629658
${AGENT_RETAILER_PIN_LOCK}      0142308503
${AGENT_WHOLESALER}     0140399328
${AGENT_WHOLESALER_CONFIRM}     0140383517

*** Keywords ***
Setup For The Suite
    Start Airsim
    Connect To OLTP DB

Teardown For The Suite
    Disconnect From Database

*** Test Cases ***
TC-BSA-01 Successful non-airtime debit transaction from a retailer agent with required fields only
    [Documentation]     Perform non-airtime debit via API successfully from a retailer agent
    ...     using only the required fields
    ${subscriber}=  Set Variable        null
    ${amount}=      Generate Random Number        100    1500
    ${client_tid}=      Generate Random Number        100    9000000
    ${bundle_name}=     Set Variable        null
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_RETAILER}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_onhold_before}=   Get From List  ${agent_bal}  2
    ${agent_bal_before_for_sms}=   Change Balance Format To Currency    ${agent_bal_before}
    Clear Call and Sms History On Airsim

    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Perform Non-airtime Debit Via API    ${api_session}     ${agent}    ${amount}      ${pin}
    ...     ${client_tid}   200
    Log     ${response}
    ${client_trans_id}=      Get From List   ${response}     0
    ${cred_trans_id}=      Get From List   ${response}     1
    ${status}=      Get From List   ${response}     3

    ${cred_tid}=    Get Last Transaction No For A Agent From DB    ${agent}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_onhold_after}=   Get From List  ${agent_bal}  2
    ${agent_bal_after_for_sms}=   Change Balance Format To Currency    ${agent_bal_after}
    Ucip Calls Count Should Be      1
    Ucip Calls Should Be    GetSubscriberInformationRequest
    Sms Count Should Be     2
    ${a_sms}=   Sms Sent To Msisdn    ${agent}      You offered the ${bundle_name} package to the number ${subscriber}. Your current EVD balance is ${agent_bal_after_for_sms} Fcfa.
    ${a_sms_2}=   Sms Sent To Msisdn    ${agent}    Your balance was ${agent_bal_before_for_sms} Fcfa and now your balance is ${agent_bal_after_for_sms} Fcfa.

    Should Be Equal       ${a_sms}      You offered the ${bundle_name} package to the number ${subscriber}. Your current EVD balance is ${agent_bal_after_for_sms} Fcfa.
    Should Be Equal       ${a_sms_2}      Your balance was ${agent_bal_before_for_sms} Fcfa and now your balance is ${agent_bal_after_for_sms} Fcfa.
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${amount}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    Should Be Equal As Strings    ${agent_bon_before}      ${agent_bon_after}
    Should Be Equal As Strings     ${agent_onhold_before}      ${agent_onhold_after}
    Should Be Equal As Strings    ${client_trans_id}      ${client_tid}
    Should Be Equal As Strings    ${cred_trans_id}      ${cred_tid}
    Should Be Equal As Strings    ${status}       SUCCESS

TC-BSA-02 Successful non-airtime debit transaction from a retailer agent with all optional fields
    [Documentation]     Perform non-airtime debit via API successfully from a retailer agent
    ...     using all the required and optional fields in the request body
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${amount}=      Generate Random Number        100    1500
    ${gross_sales_amount}=      Evaluate    ${amount}*1.01
    ${client_tid}=      Generate Random Number        100    9000000
    ${bundle_name}=     Set Variable        BSA-02
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_RETAILER_CONFIRM}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_onhold_before}=   Get From List  ${agent_bal}  2
    ${agent_bal_before_for_sms}=   Change Balance Format To Currency    ${agent_bal_before}
    Clear Call and Sms History On Airsim

    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Perform Non-airtime Debit Via API    ${api_session}     ${agent}    ${amount}      ${pin}
    ...     ${client_tid}   200   itemDescription=${bundle_name}     consumerMsisdn=${subscriber}
    ...     imsi=111222333      grossSalesAmount=${gross_sales_amount}
    Log     ${response}
    ${client_trans_id}=      Get From List   ${response}     0
    ${cred_trans_id}=      Get From List   ${response}     1
    ${status}=      Get From List   ${response}     3

    ${cred_tid}=    Get Last Transaction No For A Agent From DB    ${agent}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_onhold_after}=   Get From List  ${agent_bal}  2
    ${agent_bal_after_for_sms}=   Change Balance Format To Currency    ${agent_bal_after}
    Ucip Calls Count Should Be      0
    Sms Count Should Be     2
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}      You offered the ${bundle_name} package to the number ${subscriber}. Your current EVD balance is ${agent_bal_after_for_sms} Fcfa.
    ${a_sms_2}=   Sms Sent To Msisdn    ${agent}    Your balance was ${agent_bal_before_for_sms} Fcfa and now your balance is ${agent_bal_after_for_sms} Fcfa.

    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}      You offered the ${bundle_name} package to the number ${subscriber}. Your current EVD balance is ${agent_bal_after_for_sms} Fcfa.
    Should Be Equal       ${a_sms_2}      Your balance was ${agent_bal_before_for_sms} Fcfa and now your balance is ${agent_bal_after_for_sms} Fcfa.
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${amount}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    Should Be Equal As Strings    ${agent_bon_before}      ${agent_bon_after}
    Should Be Equal As Strings     ${agent_onhold_before}      ${agent_onhold_after}
    Should Be Equal As Strings    ${client_trans_id}      ${client_tid}
    Should Be Equal As Strings    ${cred_trans_id}      ${cred_tid}
    Should Be Equal As Strings    ${status}       SUCCESS

TC-BSA-03 Successful non-airtime debit transaction from a retailer agent and then refund with required fields only and using client TID
    [Documentation]     Perform non-airtime debit via API successfully from a retailer agent
    ...     using only the required fields in the refund request body and client TID is used to identify the debit
    ${subscriber}=  Set Variable    null
    ${amount}=      Generate Random Number        100    1500
    ${imsi}=    Set Variable    111222333
    ${gross_sales_amount}=      Evaluate    ${amount}*1.01
    ${client_tid}=      Generate Random Number        100    9000000
    ${bundle_name}=     Set Variable        null
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_RETAILER}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_onhold_before}=   Get From List  ${agent_bal}  2
    ${agent_bal_before_for_sms}=   Change Balance Format To Currency    ${agent_bal_before}
    Clear Call and Sms History On Airsim

    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Perform Non-airtime Debit Via API    ${api_session}     ${agent}    ${amount}      ${pin}
    ...     ${client_tid}   200   itemDescription=${bundle_name}     consumerMsisdn=${subscriber}
    ...     imsi=${imsi}      grossSalesAmount=${gross_sales_amount}
    Log     ${response}
    Sleep   2s
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after_debit}=   Get From List  ${agent_bal}  0
    ${agent_bal_after_debit_for_sms}=   Change Balance Format To Currency    ${agent_bal_after_debit}
    Clear Call and Sms History On Airsim
    ${client_tid_refund}=      Generate Random Number        100    9000000
    ${response}   Perform Non-airtime Refund Via API    ${api_session}     ${agent}    ${amount}      ${pin}
    ...     ${client_tid_refund}   ${client_tid}
    ${client_trans_id}=      Get From List   ${response}     0
    ${cred_trans_id}=      Get From List   ${response}     1
    ${status}=      Get From List   ${response}     3

    ${cred_tid}=    Get Last Transaction No For A Agent From DB    ${agent}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_onhold_after}=   Get From List  ${agent_bal}  2
    ${agent_bal_after_for_sms}=   Change Balance Format To Currency    ${agent_bal_after}
    Ucip Calls Count Should Be      1
    Ucip Calls Should Be    GetSubscriberInformationRequest
    Sms Count Should Be     2
    ${a_sms}=   Sms Sent To Msisdn    ${agent}      Your account has been credited with ${amount_currency_format} Fcfa for ${bundle_name} not given to ${subscriber}. Your current EVD balance is ${agent_bal_before_for_sms} Fcfa.
    ${a_sms_2}=   Sms Sent To Msisdn    ${agent}    Your balance was ${agent_bal_after_debit_for_sms} Fcfa and now your balance is ${agent_bal_before_for_sms} Fcfa.

    Should Be Equal       ${a_sms}      Your account has been credited with ${amount_currency_format} Fcfa for ${bundle_name} not given to ${subscriber}. Your current EVD balance is ${agent_bal_before_for_sms} Fcfa.
    Should Be Equal       ${a_sms_2}      Your balance was ${agent_bal_after_debit_for_sms} Fcfa and now your balance is ${agent_bal_before_for_sms} Fcfa.
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${amount}
    Should Be Equal     ${agent_bal_after}      ${agent_bal_before}
    Should Be Equal As Strings    ${agent_bon_before}      ${agent_bon_after}
    Should Be Equal As Strings     ${agent_onhold_before}      ${agent_onhold_after}
    Should Be Equal As Strings    ${client_trans_id}      ${client_tid_refund}
    Should Be Equal As Strings    ${cred_trans_id}      ${cred_tid}
    Should Be Equal As Strings    ${status}       SUCCESS


TC-BSA-04 Successful non-airtime debit transaction from a retailer agent and then refund with required fields only and using Crediverse TID
    [Documentation]     Perform non-airtime debit via API successfully from a retailer agent
    ...     using only the required fields in the refund request body and crediverse TID is used to identify the debit
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${amount}=      Generate Random Number        100    1500
    ${imsi}=    Set Variable    111222333
    ${gross_sales_amount}=      Evaluate    ${amount}*1.01
    ${client_tid}=      Generate Random Number        100    9000000
    ${bundle_name}=     Set Variable        null
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_RETAILER_CONFIRM}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_onhold_before}=   Get From List  ${agent_bal}  2
    ${agent_bal_before_for_sms}=   Change Balance Format To Currency    ${agent_bal_before}
    Clear Call and Sms History On Airsim

    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Perform Non-airtime Debit Via API    ${api_session}     ${agent}    ${amount}      ${pin}
    ...     ${client_tid}   200   itemDescription=${bundle_name}     consumerMsisdn=${subscriber}
    ...     imsi=${imsi}      grossSalesAmount=${gross_sales_amount}
    Log     ${response}
    Sleep   2s
    ${client_trans_id}=      Get From List   ${response}     0
    ${cred_trans_id}=      Get From List   ${response}     1
    ${status}=      Get From List   ${response}     3
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after_debit}=   Get From List  ${agent_bal}  0
    ${agent_bal_after_debit_for_sms}=   Change Balance Format To Currency    ${agent_bal_after_debit}
    Clear Call and Sms History On Airsim
    ${client_tid_refund}=      Generate Random Number        100    9000000
    ${response}   Perform Non-airtime Refund Via API    ${api_session}     ${agent}    ${amount}      ${pin}
    ...     ${client_tid_refund}   debit_crediverse_transaction_id=${cred_trans_id}
    ${client_trans_id}=      Get From List   ${response}     0
    ${cred_trans_id}=      Get From List   ${response}     1
    ${status}=      Get From List   ${response}     3

    ${cred_tid}=    Get Last Transaction No For A Agent From DB    ${agent}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_onhold_after}=   Get From List  ${agent_bal}  2
    ${agent_bal_after_for_sms}=   Change Balance Format To Currency    ${agent_bal_after}
    Ucip Calls Count Should Be      1
    Ucip Calls Should Be    GetSubscriberInformationRequest
    Sms Count Should Be     2
    ${a_sms}=   Sms Sent To Msisdn    ${agent}      Your account has been credited with ${amount_currency_format} Fcfa for ${bundle_name} not given to ${subscriber}. Your current EVD balance is ${agent_bal_before_for_sms} Fcfa.
    ${a_sms_2}=   Sms Sent To Msisdn    ${agent}    Your balance was ${agent_bal_after_debit_for_sms} Fcfa and now your balance is ${agent_bal_before_for_sms} Fcfa.

    Should Be Equal       ${a_sms}      Your account has been credited with ${amount_currency_format} Fcfa for ${bundle_name} not given to ${subscriber}. Your current EVD balance is ${agent_bal_before_for_sms} Fcfa.
    Should Be Equal       ${a_sms_2}      Your balance was ${agent_bal_after_debit_for_sms} Fcfa and now your balance is ${agent_bal_before_for_sms} Fcfa.
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${amount}
    Should Be Equal     ${agent_bal_after}      ${agent_bal_before}
    Should Be Equal As Strings    ${agent_bon_before}      ${agent_bon_after}
    Should Be Equal As Strings     ${agent_onhold_before}      ${agent_onhold_after}
    Should Be Equal As Strings    ${client_trans_id}      ${client_tid_refund}
    Should Be Equal As Strings    ${cred_trans_id}      ${cred_tid}
    Should Be Equal As Strings    ${status}       SUCCESS

TC-BSA-05 Non-airtime debit transaction from a retailer agent failed when agent has insufficient funds
    [Documentation]     Non-airtime debit via API failed when the retailer agent has insufficient funds
    ...     using all the required and optional fields in the request body
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${amount}=      Generate Random Number        100    1500
    ${gross_sales_amount}=      Evaluate    ${amount}*1.01
    ${client_tid}=      Generate Random Number        100    9000000
    ${bundle_name}=     Set Variable        BSA-05
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_RETAILER_INSUFF_FUNDS}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_onhold_before}=   Get From List  ${agent_bal}  2
    ${agent_bal_before_for_sms}=   Change Balance Format To Currency    ${agent_bal_before}
    Clear Call and Sms History On Airsim

    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Perform Non-airtime Debit Via API    ${api_session}     ${agent}    ${amount}      ${pin}
    ...     ${client_tid}   412   itemDescription=${bundle_name}     consumerMsisdn=${subscriber}
    ...     imsi=111222333      grossSalesAmount=${gross_sales_amount}
    Log     ${response}
    ${client_trans_id}=      Get From List   ${response}     0
    ${cred_trans_id}=      Get From List   ${response}     1
    ${status}=      Get From List   ${response}     3
    ${message}=      Get From List   ${response}     4
    ${additional_info}=     Get From List   ${response}     5

    ${cred_tid}=    Get Last Transaction No For A Agent From DB    ${agent}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_onhold_after}=   Get From List  ${agent_bal}  2
    ${agent_bal_after_for_sms}=   Change Balance Format To Currency    ${agent_bal_after}
    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${agent}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    Should Be Equal As Strings    ${agent_bon_before}      ${agent_bon_after}
    Should Be Equal As Strings     ${agent_onhold_before}      ${agent_onhold_after}
    Should Be Equal As Strings    ${status}       PRECONDITION_FAILED
    Should Be Equal As Strings    ${message}       INSUFFICIENT_FUNDS
    Should Be Equal As Strings    ${additional_info}       Insufficient funds

TC-BSA-06 Non-airtime debit transaction from a retailer agent failed when invalid pin is used
    [Documentation]     Non-airtime debit via API failed when the invalid pin is used
    ...     using all the required and optional fields in the request body
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${amount}=      Generate Random Number        100    1500
    ${imsi}=        Set Variable    111222333
    ${gross_sales_amount}=      Evaluate    ${amount}*1.01
    ${client_tid}=      Generate Random Number        100    9000000
    ${bundle_name}=     Set Variable        BSA-05
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_RETAILER}
    ${pin_invalid}=     Set Variable    85210
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_onhold_before}=   Get From List  ${agent_bal}  2
    ${agent_bal_before_for_sms}=   Change Balance Format To Currency    ${agent_bal_before}
    Clear Call and Sms History On Airsim

    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Perform Non-airtime Debit Via API    ${api_session}     ${agent}    ${amount}      ${pin_invalid}
    ...     ${client_tid}   412   itemDescription=${bundle_name}     consumerMsisdn=${subscriber}
    ...     imsi=${imsi}      grossSalesAmount=${gross_sales_amount}
    Log     ${response}
    ${client_trans_id}=      Get From List   ${response}     0
    ${cred_trans_id}=      Get From List   ${response}     1
    ${status}=      Get From List   ${response}     3
    ${message}=      Get From List   ${response}     4
    ${additional_info}=     Get From List   ${response}     5

    ${cred_tid}=    Get Last Transaction No For A Agent From DB    ${agent}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_onhold_after}=   Get From List  ${agent_bal}  2
    ${agent_bal_after_for_sms}=   Change Balance Format To Currency    ${agent_bal_after}
    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${agent}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    Should Be Equal As Strings    ${agent_bon_before}      ${agent_bon_after}
    Should Be Equal As Strings     ${agent_onhold_before}      ${agent_onhold_after}
    Should Be Equal As Strings    ${status}       PRECONDITION_FAILED
    Should Be Equal As Strings    ${message}       INVALID_PIN
    Should Be Equal As Strings    ${additional_info}       agentPin is invalid.

TC-BSA-07 Non-airtime refund transaction failed when invalid client TID is used
    [Documentation]     Perform non-airtime refund via API with a invalid client TID
    ...     using only the required fields in the refund request body and client TID is used to identify the debit
    ${subscriber}=  Set Variable    null
    ${amount}=      Generate Random Number        100    1500
    ${imsi}=    Set Variable    111222333
    ${gross_sales_amount}=      Evaluate    ${amount}*1.01
    ${client_tid}=      Generate Random Number        100    9000000
    ${client_tid_invalid}=      Generate Random Number        100    9000000
    ${bundle_name}=     Set Variable        null
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_RETAILER_CONFIRM}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_onhold_before}=   Get From List  ${agent_bal}  2
    ${agent_bal_before_for_sms}=   Change Balance Format To Currency    ${agent_bal_before}
    Clear Call and Sms History On Airsim

    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Perform Non-airtime Debit Via API    ${api_session}     ${agent}    ${amount}      ${pin}
    ...     ${client_tid}   200   itemDescription=${bundle_name}     consumerMsisdn=${subscriber}
    ...     imsi=${imsi}      grossSalesAmount=${gross_sales_amount}
    Log     ${response}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after_debit}=   Get From List  ${agent_bal}  0
    ${agent_bal_after_debit_for_sms}=   Change Balance Format To Currency    ${agent_bal_after_debit}
    Clear Call and Sms History On Airsim
    ${client_tid_refund}=      Generate Random Number        100    9000000
    ${response}   Perform Non-airtime Refund Via API    ${api_session}     ${agent}    ${amount}      ${pin}
    ...     ${client_tid_refund}   ${client_tid_invalid}    expected_status=412
    ${client_trans_id}=      Get From List   ${response}     0
    ${cred_trans_id}=      Get From List   ${response}     1
    ${status}=      Get From List   ${response}     3
    ${message}=      Get From List   ${response}     4
    ${additional_info}=     Get From List   ${response}     5

    ${cred_tid}=    Get Last Transaction No For A Agent From DB    ${agent}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_onhold_after}=   Get From List  ${agent_bal}  2
    ${agent_bal_after_for_sms}=   Change Balance Format To Currency    ${agent_bal_after}
    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${agent}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${amount}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    Should Be Equal As Strings    ${agent_bon_before}      ${agent_bon_after}
    Should Be Equal As Strings     ${agent_onhold_before}      ${agent_onhold_after}
    Should Be Equal As Strings    ${client_trans_id}      None
    Should Be Equal As Strings    ${cred_trans_id}      None
    Should Be Equal As Strings    ${status}       PRECONDITION_FAILED
    Should Be Equal As Strings    ${message}       TX_NOT_FOUND
    Should Be Equal As Strings    ${additional_info}       There is no debit transaction with clientTransactionId: ${client_tid_invalid}

TC-BSA-08 Non-airtime debit transaction from a wholesaler agent failed
    [Documentation]     Non-airtime debit via API failed when the invalid pin is used
    ...     using all the required and optional fields in the request body
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${amount}=      Generate Random Number        100    1500
    ${imsi}=        Set Variable    111222333
    ${gross_sales_amount}=      Evaluate    ${amount}*1.01
    ${client_tid}=      Generate Random Number        100    9000000
    ${bundle_name}=     Set Variable        BSA-05
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_WHOLESALER}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_onhold_before}=   Get From List  ${agent_bal}  2
    ${agent_bal_before_for_sms}=   Change Balance Format To Currency    ${agent_bal_before}
    Clear Call and Sms History On Airsim

    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Perform Non-airtime Debit Via API    ${api_session}     ${agent}    ${amount}      ${pin}
    ...     ${client_tid}   412   itemDescription=${bundle_name}     consumerMsisdn=${subscriber}
    ...     imsi=${imsi}      grossSalesAmount=${gross_sales_amount}
    Log     ${response}
    ${client_trans_id}=      Get From List   ${response}     0
    ${cred_trans_id}=      Get From List   ${response}     1
    ${status}=      Get From List   ${response}     3
    ${message}=      Get From List   ${response}     4
    ${additional_info}=     Get From List   ${response}     5

    ${cred_tid}=    Get Last Transaction No For A Agent From DB    ${agent}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_onhold_after}=   Get From List  ${agent_bal}  2
    ${agent_bal_after_for_sms}=   Change Balance Format To Currency    ${agent_bal_after}
    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${agent}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    Should Be Equal As Strings    ${agent_bon_before}      ${agent_bon_after}
    Should Be Equal As Strings     ${agent_onhold_before}      ${agent_onhold_after}
    Should Be Equal As Strings    ${status}       PRECONDITION_FAILED
    Should Be Equal As Strings    ${message}       NO_TRANSFER_RULE
    Should Be Equal As Strings    ${additional_info}       You cannot transfer to this Agent

TC-BSA-09 Non-airtime debit transaction from a retailer agent failed when the agent is suspended
    [Documentation]     Non-airtime debit via API failed when the agent is suspended
    ...     using all the required and optional fields in the request body
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${amount}=      Generate Random Number        100    1500
    ${imsi}=        Set Variable    111222333
    ${gross_sales_amount}=      Evaluate    ${amount}*1.01
    ${client_tid}=      Generate Random Number        100    9000000
    ${bundle_name}=     Set Variable        BSA-05
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_RETAILER_SUSPENDED}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_onhold_before}=   Get From List  ${agent_bal}  2
    ${agent_bal_before_for_sms}=   Change Balance Format To Currency    ${agent_bal_before}
    Clear Call and Sms History On Airsim

    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Perform Non-airtime Debit Via API    ${api_session}     ${agent}    ${amount}      ${pin}
    ...     ${client_tid}   403   itemDescription=${bundle_name}     consumerMsisdn=${subscriber}
    ...     imsi=${imsi}      grossSalesAmount=${gross_sales_amount}
    Log     ${response}
    ${client_trans_id}=      Get From List   ${response}     0
    ${cred_trans_id}=      Get From List   ${response}     1
    ${status}=      Get From List   ${response}     3
    ${message}=      Get From List   ${response}     4
    ${additional_info}=     Get From List   ${response}     5

    ${cred_tid}=    Get Last Transaction No For A Agent From DB    ${agent}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_onhold_after}=   Get From List  ${agent_bal}  2
    ${agent_bal_after_for_sms}=   Change Balance Format To Currency    ${agent_bal_after}
    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${agent}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    Should Be Equal As Strings    ${agent_bon_before}      ${agent_bon_after}
    Should Be Equal As Strings     ${agent_onhold_before}      ${agent_onhold_after}
    Should Be Equal As Strings    ${status}       FORBIDDEN
    Should Be Equal As Strings    ${message}       INVALID_STATE
    Should Be Equal As Strings    ${additional_info}       Not allowed to trade in User State: Suspended

TC-BSA-10 Non-airtime debit transaction from a retailer agent failed when the agent is deactivated
    [Documentation]     Non-airtime debit via API failed when the agent is deactivated
    ...     using all the required and optional fields in the request body
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${amount}=      Generate Random Number        100    1500
    ${imsi}=        Set Variable    111222333
    ${gross_sales_amount}=      Evaluate    ${amount}*1.01
    ${client_tid}=      Generate Random Number        100    9000000
    ${bundle_name}=     Set Variable        BSA-05
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_RETAILER_DEACTIVATED}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_onhold_before}=   Get From List  ${agent_bal}  2
    ${agent_bal_before_for_sms}=   Change Balance Format To Currency    ${agent_bal_before}
    Clear Call and Sms History On Airsim

    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Perform Non-airtime Debit Via API    ${api_session}     ${agent}    ${amount}      ${pin}
    ...     ${client_tid}   403   itemDescription=${bundle_name}     consumerMsisdn=${subscriber}
    ...     imsi=${imsi}      grossSalesAmount=${gross_sales_amount}
    Log     ${response}
    ${client_trans_id}=      Get From List   ${response}     0
    ${cred_trans_id}=      Get From List   ${response}     1
    ${status}=      Get From List   ${response}     3
    ${message}=      Get From List   ${response}     4
    ${additional_info}=     Get From List   ${response}     5

    ${cred_tid}=    Get Last Transaction No For A Agent From DB    ${agent}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_onhold_after}=   Get From List  ${agent_bal}  2
    ${agent_bal_after_for_sms}=   Change Balance Format To Currency    ${agent_bal_after}
    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${agent}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    Should Be Equal As Strings    ${agent_bon_before}      ${agent_bon_after}
    Should Be Equal As Strings     ${agent_onhold_before}      ${agent_onhold_after}
    Should Be Equal As Strings    ${status}       FORBIDDEN
    Should Be Equal As Strings    ${message}       INVALID_STATE
    Should Be Equal As Strings    ${additional_info}       Not allowed to trade in User State: Deactivated

TC-BSA-11 Non-airtime debit transaction from a retailer agent failed when the agent is not present
    [Documentation]     Non-airtime debit via API failed when the agent is not present
    ...     using all the required and optional fields in the request body
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${amount}=      Generate Random Number        100    1500
    ${imsi}=        Set Variable    111222333
    ${gross_sales_amount}=      Evaluate    ${amount}*1.01
    ${client_tid}=      Generate Random Number        100    9000000
    ${bundle_name}=     Set Variable        BSA-05
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    123654
    Clear Call and Sms History On Airsim
    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Perform Non-airtime Debit Via API    ${api_session}     ${agent}    ${amount}      ${pin}
    ...     ${client_tid}   404   itemDescription=${bundle_name}     consumerMsisdn=${subscriber}
    ...     imsi=${imsi}      grossSalesAmount=${gross_sales_amount}
    Log     ${response}
    ${client_trans_id}=      Get From List   ${response}     0
    ${cred_trans_id}=      Get From List   ${response}     1
    ${status}=      Get From List   ${response}     3
    ${message}=      Get From List   ${response}     4
    ${additional_info}=     Get From List   ${response}     5
    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${agent}"
    Should Be Equal As Strings    ${status}       NOT_FOUND
    Should Be Equal As Strings    ${message}       ACC_NOT_FOUND
    Should Be Equal As Strings    ${additional_info}       ${agent} is not a valid Agent msisdn

TC-BSA-12 Get details of an active retailer agent via API
    [Documentation]     Get the details of an active retailer agent
    ${agent}=   Set Variable    ${AGENT_RETAILER_CONFIRM}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_onhold_before}=   Get From List  ${agent_bal}  2
    Clear Call and Sms History On Airsim
    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Get Details Of Agent Via API    ${api_session}     ${agent}
    Log     ${response}
    ${agent_id}=      Get From List   ${response}     0
    ${agent_tier_name}=      Get From List   ${response}     1
    ${agent_tier_type}=      Get From List   ${response}     2
    ${agent_state}=      Get From List   ${response}     3

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_onhold_after}=   Get From List  ${agent_bal}  2

    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${agent}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    Should Be Equal As Strings    ${agent_bon_before}      ${agent_bon_after}
    Should Be Equal As Strings     ${agent_onhold_before}      ${agent_onhold_after}
    Should Be Equal As Strings    ${agent_tier_name}       eCabine
    Should Be Equal As Strings    ${agent_tier_type}       R
    Should Be Equal As Strings    ${agent_state}       ACTIVE

TC-BSA-13 Get details of an active Wholesaler agent via API
    [Documentation]     Get the details of an active wholesaler agent
    ${agent}=   Set Variable    ${AGENT_WHOLESALER}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_onhold_before}=   Get From List  ${agent_bal}  2
    Clear Call and Sms History On Airsim
    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Get Details Of Agent Via API    ${api_session}     ${agent}
    Log     ${response}
    ${agent_id}=      Get From List   ${response}     0
    ${agent_tier_name}=      Get From List   ${response}     1
    ${agent_tier_type}=      Get From List   ${response}     2
    ${agent_state}=      Get From List   ${response}     3

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_onhold_after}=   Get From List  ${agent_bal}  2

    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${agent}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    Should Be Equal As Strings    ${agent_bon_before}      ${agent_bon_after}
    Should Be Equal As Strings     ${agent_onhold_before}      ${agent_onhold_after}
    Should Be Equal As Strings    ${agent_tier_name}       eIntermed
    Should Be Equal As Strings    ${agent_tier_type}       W
    Should Be Equal As Strings    ${agent_state}       ACTIVE

TC-BSA-14 Get details of a suspended retailer agent via API
    [Documentation]     Get the details of a suspended retailer agent
    ${agent}=   Set Variable    ${AGENT_RETAILER_SUSPENDED}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_onhold_before}=   Get From List  ${agent_bal}  2
    Clear Call and Sms History On Airsim
    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Get Details Of Agent Via API    ${api_session}     ${agent}
    Log     ${response}
    ${agent_id}=      Get From List   ${response}     0
    ${agent_tier_name}=      Get From List   ${response}     1
    ${agent_tier_type}=      Get From List   ${response}     2
    ${agent_state}=      Get From List   ${response}     3

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_onhold_after}=   Get From List  ${agent_bal}  2

    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${agent}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    Should Be Equal As Strings    ${agent_bon_before}      ${agent_bon_after}
    Should Be Equal As Strings     ${agent_onhold_before}      ${agent_onhold_after}
    Should Be Equal As Strings    ${agent_tier_name}       eCabine
    Should Be Equal As Strings    ${agent_tier_type}       R
    Should Be Equal As Strings    ${agent_state}       SUSPENDED

TC-BSA-15 Get details of a deactivated retailer agent via API
    [Documentation]     Get the details of a deactivated retailer agent
    ${agent}=   Set Variable    ${AGENT_RETAILER_DEACTIVATED}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_onhold_before}=   Get From List  ${agent_bal}  2
    Clear Call and Sms History On Airsim
    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Get Details Of Agent Via API    ${api_session}     ${agent}
    Log     ${response}
    ${agent_id}=      Get From List   ${response}     0
    ${agent_tier_name}=      Get From List   ${response}     1
    ${agent_tier_type}=      Get From List   ${response}     2
    ${agent_state}=      Get From List   ${response}     3

    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_onhold_after}=   Get From List  ${agent_bal}  2

    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${agent}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    Should Be Equal As Strings    ${agent_bon_before}      ${agent_bon_after}
    Should Be Equal As Strings     ${agent_onhold_before}      ${agent_onhold_after}
    Should Be Equal As Strings    ${agent_tier_name}       eCabine
    Should Be Equal As Strings    ${agent_tier_type}       R
    Should Be Equal As Strings    ${agent_state}       DEACTIVATED

TC-BSA-16 Get details of an agent fails when agent not present
    [Documentation]     Get the details of an agent fails when agent not present
    ${agent}=   Set Variable    111220
    Clear Call and Sms History On Airsim
    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Get Details Of Agent Via API    ${api_session}     ${agent}   404
    Log     ${response}
    ${agent_id}=      Get From List   ${response}     0
    ${agent_tier_name}=      Get From List   ${response}     1
    ${agent_tier_type}=      Get From List   ${response}     2
    ${agent_state}=      Get From List   ${response}     3
    ${status}=      Get From List   ${response}     4
    ${message}=      Get From List   ${response}     5

    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${agent}"
    Should Be Equal As Strings    ${agent_tier_name}       None
    Should Be Equal As Strings    ${agent_tier_type}       None
    Should Be Equal As Strings    ${agent_state}       None
    Should Be Equal As Strings    ${status}       NOT_FOUND
    Should Be Equal As Strings    ${message}       404 Not Found

TC-BSA-17 Get Status of a successful debit transaction via API
    [Documentation]     Get the status of a successful debit transaction using the client tid
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${amount}=      Generate Random Number        100    1500
    ${gross_sales_amount}=      Evaluate    ${amount}*1.01
    ${client_tid}=      Generate Random Number        100    9000000
    ${bundle_name}=     Set Variable        BSA-17
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_RETAILER}
    Clear Call and Sms History On Airsim

    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Perform Non-airtime Debit Via API    ${api_session}     ${agent}    ${amount}      ${pin}
    ...     ${client_tid}   200   itemDescription=${bundle_name}     consumerMsisdn=${subscriber}
    ...     imsi=111222333      grossSalesAmount=${gross_sales_amount}

    Clear Call and Sms History On Airsim
    ${response}=    Get Status Of Transaction Via API   ${api_session}     ${agent}     ${client_tid}
    ${client_trans_id}=      Get From List   ${response}     0
    ${cred_trans_id}=      Get From List   ${response}     1
    ${status}=      Get From List   ${response}     3

    ${cred_tid}=    Get Last Transaction No For A Agent From DB    ${agent}
    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${agent}"
    Should Be Equal As Strings    ${client_trans_id}      ${client_tid}
    Should Be Equal As Strings    ${cred_trans_id}      ${cred_tid}
    Should Be Equal As Strings    ${status}       SUCCESS

TC-BSA-18 Get Status of a successful refund transaction via API
    [Documentation]     Get the status of a successful refund transaction using the client tid
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${amount}=      Generate Random Number        100    1500
    ${gross_sales_amount}=      Evaluate    ${amount}*1.01
    ${client_tid}=      Generate Random Number        100    9000000
    ${bundle_name}=     Set Variable        BSA-18
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_RETAILER}
    Clear Call and Sms History On Airsim

    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Perform Non-airtime Debit Via API    ${api_session}     ${agent}    ${amount}      ${pin}
    ...     ${client_tid}   200   itemDescription=${bundle_name}     consumerMsisdn=${subscriber}
    ...     imsi=111222333      grossSalesAmount=${gross_sales_amount}

    Sleep   2s
    Clear Call and Sms History On Airsim
    ${client_tid_refund}=      Generate Random Number        100    9000000
    ${response}   Perform Non-airtime Refund Via API    ${api_session}     ${agent}    ${amount}      ${pin}
    ...     ${client_tid_refund}   ${client_tid}

    Clear Call and Sms History On Airsim
    ${response}=    Get Status Of Transaction Via API   ${api_session}     ${agent}     ${client_tid_refund}
    ${client_trans_id}=      Get From List   ${response}     0
    ${cred_trans_id}=      Get From List   ${response}     1
    ${status}=      Get From List   ${response}     3

    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    ${cred_tid}=    Get Last Transaction No For A Agent From DB    ${agent}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${agent}"
    Should Be Equal As Strings    ${client_trans_id}      ${client_tid_refund}
    Should Be Equal As Strings    ${cred_trans_id}      ${cred_tid}
    Should Be Equal As Strings    ${status}       SUCCESS

TC-BSA-19 Get Status of a failed debit transaction via API
    [Documentation]     Get the status of a failed debit transaction using the client tid
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${amount}=      Generate Random Number        100    1500
    ${gross_sales_amount}=      Evaluate    ${amount}*1.01
    ${client_tid}=      Generate Random Number        100    9000000
    ${bundle_name}=     Set Variable        BSA-17
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_RETAILER}
    ${pin_invalid}=     Set Variable    22211
    Clear Call and Sms History On Airsim

    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${response}   Perform Non-airtime Debit Via API    ${api_session}     ${agent}    ${amount}      ${pin_invalid}
    ...     ${client_tid}   412   itemDescription=${bundle_name}     consumerMsisdn=${subscriber}
    ...     imsi=111222333      grossSalesAmount=${gross_sales_amount}

    Clear Call and Sms History On Airsim
    ${response}=    Get Status Of Transaction Via API   ${api_session}     ${agent}     ${client_tid}   404
    ${client_trans_id}=      Get From List   ${response}     0
    ${cred_trans_id}=      Get From List   ${response}     1
    ${status}=      Get From List   ${response}     3
    ${message}=      Get From List   ${response}     4

    ${cred_tid}=    Get Last Transaction No For A Agent From DB    ${agent}
    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${agent}"
    Should Be Equal As Strings    ${client_trans_id}      None
    Should Be Equal As Strings    ${cred_trans_id}      None
    Should Be Equal As Strings    ${status}       NOT_FOUND
    Should Be Equal As Strings    ${message}       404 Not Found

TC-BSA-20 Agent pin locks out after 3 invalid attempts and agent can't transact with valid pin
    [Documentation]     Agent PIN gets locked out when 3 invlaid pin are attempted
    ...     Agent is unable to transact with a valid pin after lock out
    ${subscriber}=  Generate Random String      7000000000    7100000000
    ${amount}=      Generate Random Number        100    1500
    ${imsi}=        Set Variable    111222333
    ${gross_sales_amount}=      Evaluate    ${amount}*1.01
    ${bundle_name}=     Set Variable        BSA-05
    ${amount_currency_format}=      Format String     {:,}    ${amount}
    ${agent}=   Set Variable    ${AGENT_RETAILER_PIN_LOCK}
    ${pin_invalid}=     Set Variable    88522
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_before}=   Get From List  ${agent_bal}  0
    ${agent_bon_before}=   Get From List  ${agent_bal}  1
    ${agent_onhold_before}=   Get From List  ${agent_bal}  2
    ${agent_bal_before_for_sms}=   Change Balance Format To Currency    ${agent_bal_before}
    Clear Call and Sms History On Airsim

    ${api_session}=     Create Session For API      ${SERVICE_USER_USERNAME}      ${SERVICE_USER_PASSWORD}
    ${client_tid}=      Generate Random Number        100    9000000
    ${response}   Perform Non-airtime Debit Via API    ${api_session}     ${agent}    ${amount}      ${pin_invalid}
    ...     ${client_tid}   412   itemDescription=${bundle_name}     consumerMsisdn=${subscriber}
    ...     imsi=${imsi}      grossSalesAmount=${gross_sales_amount}

    ${client_tid}=      Generate Random Number        100    9000000
    ${response}   Perform Non-airtime Debit Via API    ${api_session}     ${agent}    ${amount}      ${pin_invalid}
    ...     ${client_tid}   412   itemDescription=${bundle_name}     consumerMsisdn=${subscriber}
    ...     imsi=${imsi}      grossSalesAmount=${gross_sales_amount}

    ${client_tid}=      Generate Random Number        100    9000000
    ${response}   Perform Non-airtime Debit Via API    ${api_session}     ${agent}    ${amount}      ${pin_invalid}
    ...     ${client_tid}   412   itemDescription=${bundle_name}     consumerMsisdn=${subscriber}
    ...     imsi=${imsi}      grossSalesAmount=${gross_sales_amount}

    ${client_tid}=      Generate Random Number        100    9000000
    ${response}   Perform Non-airtime Debit Via API    ${api_session}     ${agent}    ${amount}      ${pin}
    ...     ${client_tid}   412   itemDescription=${bundle_name}     consumerMsisdn=${subscriber}
    ...     imsi=${imsi}      grossSalesAmount=${gross_sales_amount}

    Log     ${response}
    ${client_trans_id}=      Get From List   ${response}     0
    ${cred_trans_id}=      Get From List   ${response}     1
    ${status}=      Get From List   ${response}     3
    ${message}=      Get From List   ${response}     4
    ${additional_info}=     Get From List   ${response}     5

    ${cred_tid}=    Get Last Transaction No For A Agent From DB    ${agent}
    ${agent_bal}=     Get Agent Balances From DB   ${agent}
    ${agent_bal_after}=   Get From List  ${agent_bal}  0
    ${agent_bon_after}=   Get From List  ${agent_bal}  1
    ${agent_onhold_after}=   Get From List  ${agent_bal}  2
    ${agent_bal_after_for_sms}=   Change Balance Format To Currency    ${agent_bal_after}
    Ucip Calls Count Should Be      0
    Sms Count Should Be     0
    ${b_sms}=   Sms Sent To Msisdn    ${subscriber}
    ${a_sms}=   Sms Sent To Msisdn    ${agent}
    Should Be Equal       ${b_sms}      "No Message Sent for the MSISDN ${subscriber}"
    Should Be Equal       ${a_sms}      "No Message Sent for the MSISDN ${agent}"
    ${expected_agent_bal}=  Evaluate    ${agent_bal_before} - ${0}
    Should Be Equal     ${agent_bal_after}      ${expected_agent_bal}
    Should Be Equal As Strings    ${agent_bon_before}      ${agent_bon_after}
    Should Be Equal As Strings     ${agent_onhold_before}      ${agent_onhold_after}
    Should Be Equal As Strings    ${status}       PRECONDITION_FAILED
    Should Be Equal As Strings    ${message}       PIN_LOCKOUT
    Should Be Equal As Strings    ${additional_info}       agentPin lockout.