*** Settings ***
Library           OperatingSystem
Library           SoapLibrary
Library           Collections
Library           RequestsLibrary
Library           JSONLibrary
Library           String
Resource    gui.resource

*** Variables ***
${USERNAME}       supplier
${PASSWORD}       M@@v1vyEcd$2016
${BASE_URL}         http://localhost:9084/api
${TOKEN_URL}        http://ecdsclient:7a361a9c87824855a9cfba63129730af@localhost:9084
${INVALID_PASSWORD}     879798jkkjhh
${ECABINE_API_USER_MSISDN}      9000000001
${ECABINE_API_USER_USERNAME}    eCabineApiUser
${ECABINE_API_USER_PASSWORD}    %0EXU84%
${ECABINE_API_USER_CONFIRM_MSISDN}      9000000002
${ECABINE_API_USER_CONFIRM_USERNAME}    eCabineApiUserConfirm
${ECABINE_API_USER_CONFIRM_PASSWORD}    !%2hMLqZ
${ECABINE_API_USER_INSUFF_FUNDS_MSISDN}      9000000003
${ECABINE_API_USER_INSUFF_FUNDS_USERNAME}   eCabineApiUserInsuffFunds
${ECABINE_API_USER_INSUFF_FUNDS_PASSWORD}   dJZg8em~
${ECABINE_API_USER_INSUFF_FUNDS_CONFIRM_MSISDN}      9000000004
${ECABINE_API_USER_INSUFF_FUNDS_CONFIRM_USERNAME}   eCabineApiUserInsuffFundsConfirm
${ECABINE_API_USER_INSUFF_FUNDS_CONFIRM_PASSWORD}   pm7e$fDx
${WHOLESALER_API_USER_MSISDN}           9000000006
${WHOLESALER_API_USER_USERNAME}         WholesalerApiUser
${WHOLESALER_API_USER_PASSWORD}         uCs@NBi4
${WHOLESALER_API_USER_CONFIRM_MSISDN}           9000000007
${WHOLESALER_API_USER_CONFIRM_USERNAME}         WholesalerApiUserConfirm
${WHOLESALER_API_USER_CONFIRM_PASSWORD}         1TP2ikdh
${LOCKED_API_USER_MSISDN}=       9000000110
${LOCKED_API_USER_USERNAME}=    lockedApiUser
${LOCKED_API_USER_PASSWORD}=    j7TO^DQ9

${SUB_INITIAL_BAL}=     4000
${AGENT_RETAILER}   0142307792
${AGENT_RETAILER_CONFIRM}   0142307939
${AGENT_RETAILER_INSUFF_FUNDS}     0142307679
${AGENT_RETAILER_INSUFF_FUNDS_CONFIRM}     0142308147
${AGENT_RETAILER_SUSPENDED}=    0160618827
${AGENT_WHOLESALER}     0140399328
${AGENT_WHOLESALER_CONFIRM}     0140383517

${AGENT_ESTORE_API_USER_MSISDN}=    9000000010
${ESTORE_API_USER_USERNAME}=        eStoreApiUser
${ESTORE_API_USER_PASSWORD}=        3FumkQ8q
${AGENT_ESTORE_API_USER_MSISDN_CONFIRM}=    9000000020
${ESTORE_API_USER_CONFIRM_USERNAME}=        eStoreApiUserConfirm
${ESTORE_API_USER_CONFIRM_PASSWORD}=        ~DcKX^9m
${AGENT_EMASTER_BANQUE_API_USER_MSISDN}=    9000000030
${EMASTER_BANQUE_API_USER_USERNAME}=        eMasterBanqueApiUser
${AGENT_EMASTER_BANQUE_API_USER_PASSWORD}=  ~LEUc25f
${AGENT_EMASTER_API_USER_MSISDN}=       9000000040
${AGENT_EMASTER_API_USER_USERNAME}=     eMasterApiUser
${AGENT_EMASTER_API_USER_PASSWORD}=     EK8H5y5S
${AGENT_EINTERMED_API_USER_MSISDN}=     9000000050
${AGENT_EINTERMED_API_USER_USERNAME}=   eIntermedApiUser
${AGENT_EINTERMED_API_USER_PASSWORD}=   YyzRKXiH

${AGENT_EINTERMED_INSUFF_FUNDS_API_USER_MSISDN}=    9000000060
${AGENT_EINTERMED_INSUFF_FUNDS_API_USER_USERNAME}=  eIntermedInsuffFundsApiUser
${AGENT_EINTERMED_INSUFF_FUNDS_API_USER_PASSWORD}=  ss6VK%VG
${AGENT_EINTERMED_SUSPENDED_API_USER_MSISDN}=       9000000070
${AGENT_EINTERMED_SUSPENDED_API_USER_USERNAME}=     eIntermedSuspendedApiUser
${AGENT_EINTERMED_SUSPENDED_API_USER_PASSWORD}=     E~jWDKpG
${AGENT_EMASTER_DEACTIVATED_API_USER_MSISDN}=     9000000080
${AGENT_EMASTER_DEACTIVATED_API_USER_USERNAME}=     eIntermedDeactivatedApiUser
${AGENT_EMASTER_DEACTIVATED_API_USER_PASSWORD}=     vGR~i&l&

${SERVICE_USER_USERNAME}=       robot_service_user
${SERVICE_USER_PASSWORD}=       LdHIZosp
${SERVICE_USER_MSISDN}=         6000000001

*** Keywords ***
Get Access Token
    [arguments]     ${username}     ${password}		${expected_status}=200
    Create Session  ecds_token_session  ${TOKEN_URL}
   	${response}=  POST On Session      ecds_token_session      url=/oauth/token     params=grant_type=password&username=${username}&password=${password} 	expected_status=${expected_status}
    ${access_token}=    evaluate    $response.json().get("access_token")
    [Return]     ${access_token}


Create Session For API
    [arguments]     ${username}     ${password}  ${expected_status}=200
    ${access_token}=    Get Access Token    ${username}     ${password}   ${expected_status}
    ${headers}=   Create Dictionary      Authorization=Bearer ${access_token}
    Create Session  ecds_api_session  ${BASE_URL}   headers=${headers}
    [Return]      ecds_api_session

Get List Of Transactions
    [documentation]  Get the list of tranasctions
    [arguments]     ${session}
    ${response}=  GET On Session  ${session}  /account/transaction
    Status Should Be  200  ${response}  #Check Status as 200
    ${transactionNo}=    evaluate    $response.json()[1].get("transactionNo")
    Log     ${transactionNo}
    Log     ${response}

Sell airtime to a target MSISDN via API
    [documentation]  Sell airtime to a target MSISDN
    [arguments]     ${session}  ${msisdn}   ${amount}      ${expected_status}=200
    @{response_list}=   Create List
    ${body}=    Create Dictionary   targetMSISDN=${msisdn}      amount=${amount}	  ${expected_status}=200
    Log      ${body}
    ${header}=  Create Dictionary   Content-Type=application/json
    IF      ${expected_status} == 200
         ${response}=  POST On Session  ${session}  /account/transaction/airtime/sale    json=${body}    headers=${header}
    ELSE
         ${response}=  POST On Session  ${session}  /account/transaction/airtime/sale    json=${body}    headers=${header}   expected_status=${expected_status}
    END
    Status Should Be  ${expected_status}  ${response}  #Check Status
    ${transactionNo}=    evaluate    $response.json().get("transactionNo")
    ${message}=      evaluate    $response.json().get("message")
    ${status}=       evaluate    $response.json().get("status")
    Log   message:${message}, status=${status}
    Append To List    ${response_list}  ${transactionNo}
    Append To List    ${response_list}  ${message}
    Append To List    ${response_list}  ${status}
    [RETURN]     ${response_list}


Transfer to a target MSISDN via API
	[documentation]  Transfer to a target MSISDN via API
	[arguments]     ${session}  ${msisdn}   ${amount}   ${expected_status}=200
	@{response_list}=   Create List
	${body}=    Create Dictionary   targetMSISDN=${msisdn}      amount=${amount}
	Log      ${body}
	${header}=  Create Dictionary   Content-Type=application/json
	IF      ${expected_status} == 200
		${response}=  POST On Session  ${session}  /account/transaction/transfer    json=${body}    headers=${header}
	ELSE
		${response}=  POST On Session  ${session}  /account/transaction/transfer    json=${body}    headers=${header}   expected_status=${expected_status}
	END
	Status Should Be  ${expected_status}  ${response}  #Check Status
	${transactionNo}=    evaluate    $response.json().get("transactionNo")
	${message}=      evaluate    $response.json().get("message")
	${status}=       evaluate    $response.json().get("status")
	Log   message:${message}, status=${status}
	Append To List    ${response_list}  ${transactionNo}
	Append To List    ${response_list}  ${message}
	Append To List    ${response_list}  ${status}
	[RETURN]     ${response_list}

Get the account's last Transaction
    [documentation]  Get the account last transaction
    [arguments]     ${session}
    ${response}=  GET On Session  ${session}  /account/transaction/last
    Status Should Be  200  ${response}  #Check Status as 200
    ${transactionNo}=    evaluate    $response.json().get("transactionNo")
    Log     ${transactionNo}
    Log     ${response}

Get particulars of authenticated user
    [documentation]  Get the particulars of authenticated user
    [arguments]     ${session}
    ${response}=  GET On Session  ${session}  /account/user
    Status Should Be  200  ${response}  #Check Status as 200
    ${userID}=    evaluate    $response.json().get("userID")
    Log     ${userID}
    Log     ${response}

Update the authenticated user's particulars
    [documentation]  Get the authenticated user's particulars
    [arguments]     ${session}      ${title}    ${firstName}    ${surname}  ${initials}     ${language}     ${email}    ${mobileNumber}
    ${body}=    Create Dictionary   title=${title}      firstName=${firstName}  surname=${surname}  initials=${initials}   language=${language}     email=${email}      mobileNumber=${mobileNumber}
    Log      ${body}
    ${header}=  Create Dictionary   Content-Type=application/json
    ${response}=  PUT On Session  ${session}  /account/user    json=${body}    headers=${header}
    Status Should Be  200  ${response}  #Check Status as 200

Sell a bundle to a target MSISDN account via API
    [documentation]  Sell a bundle to a target MSISDN account
    [arguments]     ${session}  ${msisdn}   ${bundleid}
    ${body}=    Create Dictionary   targetMSISDN=${msisdn}      bundleid=${bundleid}
    Log      ${body}
    ${header}=  Create Dictionary   Content-Type=application/json
    ${response}=  POST On Session  ${session}  /account/transaction/bundle/sale    json=${body}    headers=${header}
    Status Should Be  200  ${response}  #Check Status as 200
    ${transactionNo}=    evaluate    $response.json().get("transactionNo")
    Log     ${response}
    [RETURN]     ${transactionNo}

Query your account details via API
    [documentation]  Query your account details via API
    [arguments]     ${session}
    ${response}=  GET On Session  ${session}  /account
    Status Should Be  200  ${response}  #Check Status as 200
    ${accountID}=    evaluate    $response.json().get("accountID")
    Log     ${accountID}
    Log     ${response}

Update the account detials
    [documentation]  update the account detials
    [arguments]     ${session}      ${title}    ${firstName}    ${surname}  ${initials}     ${language}     ${email}    ${altPhoneNumber}
    ${body}=    Create Dictionary   title=${title}      firstName=${firstName}  surname=${surname}  initials=${initials}   language=${language}     email=${email}      altPhoneNumber=${altPhoneNumber}
    Log      ${body}
    ${header}=  Create Dictionary   Content-Type=application/json
    ${response}=  PUT On Session  ${session}  /account    json=${body}    headers=${header}
    Status Should Be  200  ${response}  #Check Status as 200

Get the account balance information
    [documentation]  Get the account balance information
    [arguments]     ${session}
    ${response}=  GET On Session  ${session}  /account/balance
    Status Should Be  200  ${response}  #Check Status as 200
    ${agentID}=    evaluate    $response.json().get("agentID")
    Log     ${agentID}
    Log     ${response}

#   Non-airtime sale APIs

Perform Non-airtime Debit Via API
    [documentation]  Perform non-airtime debit transaction
    [arguments]     ${session}  ${msisdn}   ${amount}   ${pin}   ${client_transaction_id}
    ...     ${expected_status}=200      @{args}
    @{response_list}=   Create List
    ${body}=    Create Dictionary   amount=${amount}    agentPin=${pin}     clientTransactionId=${client_transaction_id}
    FOR     ${item}     IN      @{args}
        Log     ${item}
        ${split_args}=     Split String    ${item}     =
        ${key}=     Get From List   ${split_args}   0
        Log     ${key}
        ${value}=   Get From List    ${split_args}     1
        Log     ${value}
        ${body}=    Set To Dictionary   ${body}     ${key}=${value}
    END
    Log      ${body}
    ${header}=  Create Dictionary   Content-Type=application/json
    IF      ${expected_status} == 200
         ${response}=  POST On Session  ${session}  /service/agent/${msisdn}/debit    json=${body}    headers=${header}
    ELSE
         ${response}=  POST On Session  ${session}  /service/agent/${msisdn}/debit    json=${body}    headers=${header}   expected_status=${expected_status}
    END
    Status Should Be  ${expected_status}  ${response}  #Check Status
    ${client_transaction_id}=    evaluate    $response.json().get("clientTransactionId")
    ${crediverse_transaction_id}=    evaluate    $response.json().get("crediverseTransactionId")
    ${transaction_end_timestamp}=      evaluate    $response.json().get("transactionEndTimestamp")
    ${status}=       evaluate    $response.json().get("status")
    ${message}=     evaluate    $response.json().get("message")
    ${additional_info}=     evaluate    $response.json().get("additionalInformation")

#    Log   message:${message}, status=${status}
    Append To List    ${response_list}  ${client_transaction_id}
    Append To List    ${response_list}  ${crediverse_transaction_id}
    Append To List    ${response_list}  ${transaction_end_timestamp}
    Append To List    ${response_list}  ${status}
    Append To List    ${response_list}  ${message}
    Append To List    ${response_list}  ${additional_info}
    [RETURN]     ${response_list}

Perform Non-airtime Refund Via API
    [documentation]  Perform non-airtime refund transaction
    [arguments]     ${session}  ${msisdn}   ${amount}   ${pin}   ${client_transaction_id}
    ...     ${debit_client_transaction_id}=""     ${debit_crediverse_transaction_id}=""
    ...     ${expected_status}=200      @{args}
    @{response_list}=   Create List
    IF      ${debit_client_transaction_id}==""
        ${body}=    Create Dictionary   amount=${amount}    agentPin=${pin}     clientTransactionId=${client_transaction_id}
        ...     debitCrediverseTransactionId=${debit_crediverse_transaction_id}
    ELSE IF     ${debit_crediverse_transaction_id}==""
        ${body}=    Create Dictionary   amount=${amount}    agentPin=${pin}     clientTransactionId=${client_transaction_id}
        ...     debitClientTransactionId=${debit_client_transaction_id}
    ELSE
        ${body}=    Create Dictionary   amount=${amount}    agentPin=${pin}     clientTransactionId=${client_transaction_id}
        ...     debitClientTransactionId=${debit_client_transaction_id}     debitCrediverseTransactionId=${debit_crediverse_transaction_id}
    END
    FOR     ${item}     IN      @{args}
        Log     ${item}
        ${split_args}=     Split String    ${item}     =
        ${key}=     Get From List   ${split_args}   0
        Log     ${key}
        ${value}=   Get From List    ${split_args}     1
        Log     ${value}
        ${body}=    Set To Dictionary   ${body}     ${key}=${value}
    END
    Log      ${body}
    ${header}=  Create Dictionary   Content-Type=application/json
    IF      ${expected_status} == 200
         ${response}=  POST On Session  ${session}  /service/agent/${msisdn}/refund    json=${body}    headers=${header}
    ELSE
         ${response}=  POST On Session  ${session}  /service/agent/${msisdn}/refund    json=${body}    headers=${header}   expected_status=${expected_status}
    END
    Status Should Be  ${expected_status}  ${response}  #Check Status
    ${client_transaction_id}=    evaluate    $response.json().get("clientTransactionId")
    ${crediverse_transaction_id}=    evaluate    $response.json().get("crediverseTransactionId")
    ${transaction_end_timestamp}=      evaluate    $response.json().get("transactionEndTimestamp")
    ${status}=       evaluate    $response.json().get("status")
    ${message}=     evaluate    $response.json().get("message")
    ${additional_info}=     evaluate    $response.json().get("additionalInformation")
    Append To List    ${response_list}  ${client_transaction_id}
    Append To List    ${response_list}  ${crediverse_transaction_id}
    Append To List    ${response_list}  ${transaction_end_timestamp}
    Append To List    ${response_list}  ${status}
    Append To List    ${response_list}  ${message}
    Append To List    ${response_list}  ${additional_info}
    [RETURN]     ${response_list}

Get Details Of Agent Via API
    [documentation]  Get the details of a particular agent
    [arguments]     ${session}      ${msisdn}   ${expected_status}=200
    @{response_list}=   Create List
    ${header}=  Create Dictionary   Content-Type=application/json
    IF      ${expected_status} == 200
         ${response}=  GET On Session  ${session}  /service/agent/${msisdn}/details    headers=${header}
         ${agent_tier_name}=     evaluate    $response.json().get("tier").get("name")
         ${agent_tier_type}=     evaluate    $response.json().get("tier").get("type")
    ELSE
         ${response}=  GET On Session  ${session}  /service/agent/${msisdn}/details    headers=${header}   expected_status=${expected_status}
         ${agent_tier_name}=     Set Variable   None
         ${agent_tier_type}=     Set Variable   None
    END
    Status Should Be  ${expected_status}  ${response}  #Check Status
    ${agent_id}=       evaluate    $response.json().get("agentId")
    ${agent_state}=     evaluate    $response.json().get("state")
    ${status}=       evaluate    $response.json().get("status")
    ${message}=     evaluate    $response.json().get("message")

    Append To List    ${response_list}  ${agent_id}
    Append To List    ${response_list}  ${agent_tier_name}
    Append To List    ${response_list}  ${agent_tier_type}
    Append To List    ${response_list}  ${agent_state}
    Append To List    ${response_list}  ${status}
    Append To List    ${response_list}  ${message}
    [RETURN]     ${response_list}

Get Status Of Transaction Via API
    [documentation]  Get the details of a particular transaction
    [arguments]     ${session}      ${msisdn}   ${client_tid}   ${expected_status}=200
    @{response_list}=   Create List
    ${header}=  Create Dictionary   Content-Type=application/json
    IF      ${expected_status} == 200
         ${response}=  GET On Session  ${session}  /service/agent/${msisdn}/transaction/${client_tid}/status    headers=${header}
    ELSE
         ${response}=  GET On Session  ${session}  /service/agent/${msisdn}/transaction/${client_tid}/status    headers=${header}   expected_status=${expected_status}
    END
    Status Should Be  ${expected_status}  ${response}  #Check Status
    ${client_transaction_id}=    evaluate    $response.json().get("clientTransactionId")
    ${crediverse_transaction_id}=    evaluate    $response.json().get("crediverseTransactionId")
    ${transaction_end_timestamp}=      evaluate    $response.json().get("transactionEndTimestamp")
    ${status}=       evaluate    $response.json().get("status")
    ${message}=     evaluate    $response.json().get("message")
    ${additional_info}=     evaluate    $response.json().get("additionalInformation")

    Append To List    ${response_list}  ${client_transaction_id}
    Append To List    ${response_list}  ${crediverse_transaction_id}
    Append To List    ${response_list}  ${transaction_end_timestamp}
    Append To List    ${response_list}  ${status}
    Append To List    ${response_list}  ${message}
    Append To List    ${response_list}  ${additional_info}
    [RETURN]     ${response_list}