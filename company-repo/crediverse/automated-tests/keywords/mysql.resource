*** Settings ***
Library           DatabaseLibrary
Library           OperatingSystem
Library           Collections
Library           String


*** Variables ***
${DBHost}         127.0.0.1
${DBName}         hxc
${DBPass}         sgu9fjexsm2hy9NJQj7SKYadu
${DBPort}         3306
${DBUser}         root

*** Keywords ***
Connect To OLTP DB
    Connect To Database    pymysql    ${DBName}    ${DBUser}    ${DBPass}    ${DBHost}    ${DBPort}

Connect To SMSQ DB
    Connect To Database    pymysql    dbsmsq    ${DBUser}    ${DBPass}    ${DBHost}    ${DBPort}

Get Agent Balances From DB
    [Documentation]     To Get the balance of Agent from DB
    [Arguments]     ${msisdn}
    Connect To OLTP DB
    ${response}=    Query     SELECT balance,bonus,on_hold FROM ea_account INNER JOIN ea_agent ON ea_agent.id=ea_account.agent_id WHERE msisdn="${msisdn}";
    Log     ${response}
    @{balance}=     Create List
    ${bal}=     Evaluate        [x[0] for x in $response]
    ${bon}=     Evaluate        [x[1] for x in $response]
    ${on_hold}=     Evaluate        [x[2] for x in $response]

    ${bal}=     Get From List       ${bal}    0
    ${bon}=     Get From List       ${bon}    0
    ${on_hold}=     Get From List       ${on_hold}    0

    ${bal}=   Convert To Number   ${bal}    3
    ${bon}=   Convert To Number   ${bon}    3
    ${on_hold}=   Convert To Number   ${on_hold}    3
    Append To List   ${balance}  ${bal}
    Append To List   ${balance}  ${bon}
    Append To List   ${balance}  ${on_hold}
    Log     ${balance}
    [Return]    ${balance}


Get Last Transaction No For B Agent From DB
    [Documentation]     To Get the transaction number of the last transaction perfomed towards the Agent from DB
    [Arguments]     ${msisdn}
    Connect To OLTP DB
    ${response}=    Query     SELECT no FROM ec_transact WHERE b_msisdn="${msisdn}" ORDER BY ended DESC limit 1;
    Log     ${response}
    @{balance}=     Create List
    ${transaction_no}=     Evaluate        [x[0] for x in $response]

    ${transaction_no}=     Get From List       ${transaction_no}    0
    Log     ${transaction_no}
    [Return]    ${transaction_no}

Get Last Transaction No For A Agent From DB
    [Documentation]     To Get the transaction number of the last transaction perfomed towards the Agent from DB
    [Arguments]     ${msisdn}
    Connect To OLTP DB
    ${response}=    Query     SELECT no FROM ec_transact WHERE a_msisdn="${msisdn}" ORDER BY ended DESC limit 1;
    Log     ${response}
    @{balance}=     Create List
    ${transaction_no}=     Evaluate        [x[0] for x in $response]

    ${transaction_no}=     Get From List       ${transaction_no}    0
    Log     ${transaction_no}
    [Return]    ${transaction_no}

Get Last SMS For Target MSISDN From SMSQ DB
    [Documentation]     To Get the transaction number of the last transaction perfomed towards the Agent from DB
    [Arguments]     ${msisdn}
    Connect To SMSQ DB
    ${response}=    Query     SELECT message FROM smsq_queue WHERE destination_msisdn="${msisdn}" ORDER BY insertion_date DESC LIMIT 1;
    Log     ${response}
    ${message}=     Evaluate        [x[0] for x in $response]

    ${message}=     Get From List       ${message}    0
    Log     ${message}
    [Return]    ${message}

Get Number of Attempts For Users
    [Arguments]     ${domain_account}
    Connect To OLTP DB
    ${response}=    Query     SELECT attempts FROM ea_user WHERE domain_account="${domain_account}" LIMIT 1;
    Log     ${response}
    ${attempts}=     Evaluate        [x[0] for x in $response]

    ${attempts}=     Get From List       ${attempts}    0
    Log     ${attempts}
    [Return]    ${attempts}
