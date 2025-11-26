*** Settings ***
Library           OperatingSystem
Library           SoapLibrary
Library           Collections
Library           RequestsLibrary
Library           JSONLibrary
Library           String
Resource    gui.resource

*** Variables ***
${AIR_SERVER}   http://127.0.0.1:10012/Air
${SAMPLE_USSD}  911
${GENET_USSD}   412
${EMOOV_USSD}   119
${USSD_TRANSFER}    413
${USSD_BAL_CHK}     414
${USSD_LAST_TX_ENQ}     445
${USERNAME}       supplier
${PASSWORD}       M@@v1vyEcd$2016
${SET_HLR_DATA_XML}     ${CURDIR}${/}setHlrData.xml

*** Keywords ***
Start Airsim
    [Documentation]     Use this keyword to start the airsim.
    ...     It is recommended to start airsim as test suite setup
    Create Soap Client    ${AIR_SERVER}?wsdl
    ${response}=    Call SOAP Method    start
    Log     ${response}
    Should Be Equal As Strings  ${response}     True

Stop Airsim
    [Documentation]     Use this keyword to stop the airsim.
    ...     It is recommended to start airsim as test suite setup
    ${response}=    Call SOAP Method    stop
    Log     ${response}

Create Subscriber On Airsim
    [Documentation]     Use this keyword to create subscriber on airsim.
    [Arguments]     ${msisdn}   ${language_id}  ${service_class}    ${amount}   ${status}
    &{response}=    Create dictionary
    ${response}=    Call SOAP Method    addSubscriber    ${msisdn}   ${language_id}  ${service_class}    ${amount}   ${status}
    Log     ${response}
    ${status}=   Get From Dictionary   ${response}   accountActivatedFlag
    Should Be Equal As Strings  ${status}     True

Delete Subscriber On Airsim
    [Documentation]     Use this keyword to delete subscriber on airsim.
    [Arguments]     ${msisdn}
    ${response}=    Call SOAP Method    deleteSubscriber     ${msisdn}
    Log     ${response}

Inject Response In Airsim
    [Documentation]     Use this keyword to inject response in airsim.
    [Arguments]     ${air_call}     ${response_code}    ${delay_ms}=0
    ${response}=    Call SOAP Method    injectResponse   ${air_call}     ${response_code}    ${delay_ms}
    Log     ${response}

Reset Injected Response In Airsim
    [Documentation]     Use this keyword to reset the injected response in airsim.
    [Arguments]     ${air_call}
    ${response}=    Call SOAP Method    resetInjectedResponse   ${air_call}
    Log     ${response}

Set Hlr Data In Airsim
    [Documentation]     Use this keyword to set the location info on the mapgw simulator in Airsim.
    [Arguments]     ${msisdn}   ${mcc}      ${mnc}    ${lac}   ${cell}
    ${file}=   Get File    ${SET_HLR_DATA_XML}
    ${file}     Replace String    ${file}   _msisdn   ${msisdn}
    ${file}     Replace String    ${file}   _mcc   ${mcc}
    ${file}     Replace String    ${file}   _mnc   ${mnc}
    ${file}     Replace String    ${file}   _lac   ${lac}
    ${file}     Replace String    ${file}   _cell   ${cell}
    Log    ${file}
    Create File     setHlrDataTemp.xml    ${file}
    ${response}=    Call SOAP Method With XML   setHlrDataTemp.xml
    Remove File     setHlrDataTemp.xml
    Log     ${response}



Ucip Calls Count Should Be
    [Documentation]     Use this keyword to verify the count of calls sent to AIR
    ...     takes as an argument a string representing the expected number of ucip calls
    [Arguments]     ${expected_count}
    ${response}=    Call SOAP Method    getCallHistory
    Log     ${response}
    ${count}=   Get Length   ${response}
    ${count}=   Convert To String   ${count}
    Log     ${count}
    Should Be Equal     ${expected_count}   ${count}


Ucip Calls Should Be
    [Documentation]     Use this keyword to verify the calls sent to AIR are valid
    ...     Takes as an argument a list of expected method calls to AIR (e.g RefillRequest  GetBalanceAndDateRequest)
    [Arguments]     @{expected_method_list}
    @{response}=    Call SOAP Method    getCallHistory
    Log     ${response}
    @{method_list}=     Create List
    FOR     ${calls}    IN     @{response}
        ${method}=   Get From Dictionary   ${calls}   method
        Log     ${method}
        Append To List      ${method_list}   ${method}
    END
    log     ${method_list}
    Lists Should Be Equal       ${method_list}      ${expected_method_list}


Sms Count Should Be
    [Documentation]     Use this keyword to verify the count of sms sent
    ...     takes as an argument a string representing the expected number of sms
    [Arguments]     ${expected_count}
    ${response}=    Call SOAP Method    getSmsHistory
    Log     ${response}
    ${count}=   Get Length   ${response}
    ${count}=   Convert To String   ${count}
    Log     ${count}
    Should Be Equal     ${expected_count}   ${count}



Sms Sent To Msisdn
    [Documentation]     Use this keyword to get the sms sent to the msisdn
    ...     Takes as an argument an MSISDN and returns the sms sent to that msisdn
    [Arguments]     ${expected_msisdn}      ${expected_message}=NULL
    ${expected_msisdn_str}=     Set Variable    ${expected_msisdn}
    ${expected_msisdn}=     Convert to Integer   ${expected_msisdn}
    @{response}=    Call SOAP Method    getSmsHistory
    Log     ${response}
    FOR     ${messages}    IN     @{response}
        ${msisdn}=   Get From Dictionary   ${messages}   fromMSISDN
        ${msg}=     Get From Dictionary   ${messages}   text
        Log     ${msg}
        ${msisdn}=      Convert to Integer   ${msisdn}
        IF  "${expected_message}"=="NULL"
            IF      ${msisdn}==${expected_msisdn}   RETURN    ${msg}
        ELSE
            IF      ${msisdn}==${expected_msisdn}
                Return From Keyword If      "${msg}"=="${expected_message}"     ${msg}
            END
        END
    END
        Return From Keyword     "No Message Sent for the MSISDN ${expected_msisdn_str}"


Get Last Sms
    [Documentation]     Use this keyword to get the last sms
    ...     returns the last sms sent
    @{response}=    Call SOAP Method    getSmsHistory
    Log     ${response}
    FOR     ${messages}    IN     @{response}
        ${msg}=     Get From Dictionary   ${messages}   text
        Log     ${msg}
        Return From Keyword     ${msg}
    END


Get Otp From Sms
    [Documentation]     Use this keyword to get the OTP sent to in the sms
    ...     Only woks for the message= Your One Time PIN is <OTP>
    ${message}=     Get Last Sms
    @{split_msg}=   Split String        ${message}
    Log     ${split_msg}
    ${length}=      Get Length     ${split_msg}
    ${index}=   Evaluate    ${length} - 1
    Log     ${index}
    ${otp}=     Get From List       ${split_msg}    ${index}
    Log     ${otp}
    [Return]       ${otp}


Get Otp From Sms At Index
    [Documentation]     Use this keyword to get the OTP sent in the sms
    ...     Takes the index as argument and retuns the nth part of message separated by whitespaces
    [Arguments]     ${index}
    ${message}=     Get Last Sms
    @{split_msg}=   Split String        ${message}
    Log     ${split_msg}
    ${otp}=     Get From List       ${split_msg}    ${index}
    Log     ${otp}
    [Return]       ${otp}

Last Sms Should Start With
    [Documentation]     Use this keyword to verify the las tsms sent is valid
    [Arguments]     ${expected_sms}
    ${sms}=     Get Last Sms
    Should Start With       ${sms}       ${expected_sms}

Sms For Msisdn Should Start With
    [Documentation]     Use this keyword to get the verify the sent to the msisdn is valid
    ...     Takes as argument an MSISDN and the expected sms
    [Arguments]     ${msisdn}   ${expected_sms}
    ${sms}=     Sms Sent To Msisdn      ${msisdn}
    Should Start With       ${sms}       ${expected_sms}


Sms For Msisdn Should Be
    [Documentation]     Use this keyword to verify the sms sent to the msisdn is valid
    ...     Takes as argument an MSISDN and the expected sms
    [Arguments]     ${msisdn}   ${expected_sms}
    ${sms}=     Sms Sent To Msisdn      ${msisdn}
    Should Be Equal       ${sms}       ${expected_sms}


Subscriber Balance Should Be
    [Documentation]     Use this keyword to verify the balance of the subscriber
    ...     takes as arguments a subscriber msisdn and expected balance
    [Arguments]     ${msisdn}   ${expected_balance}
    ${response}=    Call SOAP Method    getSubscriber   ${msisdn}
    Log     ${response}
    ${balance}=   Get From Dictionary   ${response}   accountValue1
    Should Be Equal     ${balance}      ${expected_balance}


Get Subscriber Balance
    [Documentation]     Use this keyword to get the balance of the subscriber
    ...     takes as arguments a subscriber msisdn
    [Arguments]     ${msisdn}
    @{response}=    Call SOAP Method    getSubscriber   ${msisdn}
    Log     ${response}
    ${balance}=   Get From Dictionary   ${response}   accountValue1
    [Return]    ${balance}

Agent Balance Before And After Should Be Equal
    [Documentation]     Use this keyword to verify the balance of the Agent
    ...     takes as arguments a subscriber msisdn and expected balance
    [Arguments]     ${balance_before}   ${balance_after}
    Should Be Equal     ${balance}      ${expected_balance}


Get Agent Balance
    [Documentation]     Use this keyword to get the balance of the agent
    ...     takes as arguments a agent msisdn
    [Arguments]     ${msisdn}
    Login To Admin Gui With Supplier
    Navigate To Agents Management
    Open Agent Profile From Quick Search    ${msisdn}
    ${balance}=     Get Values From Agent Profile
    Log     ${balance}
    [Return]    ${balance}



Clear Call History On Airsim
    [Documentation]     Use this keyword to clear the calls history on airsim.
    ${response}=    Call SOAP Method    clearCallHistory
    Log     ${response}

Clear Sms History On Airsim
    [Documentation]     Use this keyword to clear the sms history on airsim.
    ${response}=    Call SOAP Method    clearSmsHistory
    Log     ${response}

Clear Call and Sms History On Airsim
    [Documentation]     Use this keyword to clear the calls and sms history on airsim.
    Clear Call History On Airsim
    Clear Sms History On Airsim

Send Ussd Request
    [Documentation]     To send the USSD string use Keyword "Send Ussd Request"
    ...     which takes as arguments MSISDN and USSD shortcode (e.g *911#) returns the xml response
    ...     and returns the ussd response in XML form.
    [Arguments]     ${msisdn}   ${ussd}     ${imsi}=${NONE}     ${last}=True
    ${response}=    Call SOAP Method    injectMOUssd     ${msisdn}   ${ussd}     ${imsi}
    Log     ${response}
    ${status}=   Get From Dictionary   ${response}   last
    Should Be Equal As Strings  ${status}     ${last}
    ${ussd_response}=    Get From Dictionary   ${response}   text
    [Return]    ${ussd_response}


USSD Response Should Be
    [Documentation]     To validate the response use keyword "USSD Response Should Be"
    ...     which takes as arguments actual_repsonse as string and the expected_response as String
    [Arguments]     ${actual_response}     ${expected_response}
    Should Be Equal     ${actual_response}     ${expected_response}    strip_spaces=trailing


USSD Response Should Start With
    [Documentation]     To validate the response use keyword "USSD Response Should Be"
    ...     which takes as arguments actual_repsonse as string and the expected_response as String
    [Arguments]     ${actual_response}     ${expected_response}
    Should Start With     ${actual_response}     ${expected_response}

Transfer Via Ussd
    [Documentation]     To transfer amount between agents via USSD
    ...     which takes as arguments MSISDN and USSD shortcode (e.g *911#) returns the xml response
    ...     and returns the ussd response in XML form.
    [Arguments]     ${msisdn_a}     ${msisdn_b}     ${amount}   ${pin}   ${ussd}=${USSD_TRANSFER}   ${ussd_response}=SUCCESS
    ${response}=    Send Ussd Request   ${msisdn_a}     *${ussd}*${msisdn_b}*${amount}*${pin}#
    ${ref}      Get Ref From Ussd Response    ${response}
    Log     ${response}
    IF      "${ussd_response}" == "SUCCESS"
        ${amount}=   Convert To Integer   ${amount}
        ${amount_currency}=    Format String    {:,}    ${amount}
        Log     ${amount_currency}
        USSD Response Should Start With     ${response}     You have Transferred ${amount_currency} Fcfa to ${msisdn_b}. Your new Balance is
        Sms For Msisdn Should Start With    ${msisdn_a}   You have Transferred ${amount_currency} Fcfa to ${msisdn_b}. Your new Balance is
        Sms For Msisdn Should Start With    ${msisdn_b}     You have Received ${amount_currency} Fcfa from ${msisdn_a}. Your new Balance is
    ELSE
        USSD Response Should Start With     ${response}     ${ussd_response}
    END
    [Return]    ${ref}

Get Agent Balance Via Ussd
    [Documentation]     To Get the balance of Agent via USSD
    [Arguments]     ${msisdn}   ${pin}   ${imsi}=${NONE}
    ${response}=    Send Ussd Request   ${msisdn}     *${USSD_BAL_CHK}*${pin}#
    Log     ${msisdn}
    Log     ${response}
    ${split_res}=      Split String    ${response}
    ${bal}=     Get From List       ${split_res}    1
    ${bal}=   Replace String      ${bal}    ,   ${EMPTY}
    ${bal}=     Get Substring   ${bal}  3
    ${bal}=   Convert To Number   ${bal}    2
    Log     ${bal}
    [Return]    ${bal}

Get Agent Balances Via Ussd
    [Documentation]     To get the balance of the agent as a list
    ...     Works for the balance enquiry message Balance {Balance} , Bonus {BonusBalance}
    [Arguments]     ${msisdn}   ${pin}   ${imsi}=${NONE}
    ${response}=    Send Ussd Request   ${msisdn}     *${USSD_BAL_CHK}*${pin}#
    @{balance}=     Create List
    ${split_res}=      Split String    ${response}
    ${bal_org}=     Get From List       ${split_res}    4
    ${bon_org}=     Get From List       ${split_res}    9
    ${on_hold_org}=     Get From List       ${split_res}    17
    ${bal_modified}=   Remove Comma And Currency From Amount     ${bal_org}
    ${bon_modified}=   Remove Comma And Currency From Amount     ${bon_org}
    ${on_hold_modified}=   Remove Comma And Currency From Amount     ${on_hold_org}
    Append To List   ${balance}  ${bal_modified}
    Append To List   ${balance}  ${bon_modified}
    Append To List   ${balance}  ${bal_org}
    Append To List   ${balance}  ${bon_org}
    Append To List   ${balance}  ${on_hold_modified}
    Append To List   ${balance}  ${on_hold_org}
    Log     ${balance}
    [Return]    ${balance}

Remove Comma And Currency From Amount
    [Documentation]     Converts XOF1,000 to 1000.0
    [Arguments]     ${amount}
    ${amount}=   Replace String      ${amount}    ,   ${EMPTY}
    ${amount}=   Convert To Number   ${amount}    2
    [Return]    ${amount}

Get Ref Number of Last Transaction via USSD
    [Documentation]     To get the Ref Number of the last transaction performed by the agent
    [Arguments]     ${msisdn}   ${pin}   ${imsi}=${NONE}
    ${response}=    Send Ussd Request   ${msisdn}     *${USSD_LAST_TX_ENQ}*${pin}#
    ${split_res}=      Split String    ${response}
    ${ref}=     Get From List       ${split_res}    2
    ${ref}=   Replace String      ${ref}    :   ${EMPTY}
    Log     ${ref}
    [Return]    ${ref}


Generate Random String
    [Documentation]     To generate random numbers between a range to be used as MSISDN.
    ...     It takes as arguments the start and end of the range between which the MSISDN can be created
    [Arguments]     ${start}    ${end}
    ${random}=  Evaluate  random.sample(range(${start}, ${end}),1)   random
    Log     ${random}
    ${random}=  Get From List   ${random}   0
    ${random}=  Convert to String   ${random}
    [Return]    ${random}

Generate Random Number
    [Documentation]     To generate random numbers between a range to be used as MSISDN.
    ...     It takes as arguments the start and end of the range between which the MSISDN can be created
    [Arguments]     ${start}    ${end}
    ${random}=  Evaluate  random.sample(range(${start}, ${end}),1)   random
    Log     ${random}
    ${random}=  Get From List   ${random}   0
    [Return]    ${random}

Change Balance Format To Currency
    [Documentation]     To change the format of balances fetched from DB to currency format like 123,456,000
    ...     It takes as arguments the value from DB or any other source like 123456000.098
    [Arguments]     ${amount}
    ${amount_currency_format}=     Evaluate    round(${amount},0)
    ${amount_currency_format}=     Evaluate    math.ceil(${amount_currency_format})
    ${amount_currency_format}=     Format String     {:,}    ${amount_currency_format}
    [Return]    ${amount_currency_format}

Get Ref From Ussd Response
    [Documentation]     Use this keyword to get the Ref number in the ussd response
    ...     Only woks for the message where ref number is at the end
    [Arguments]    ${ussd_response}
    @{split_msg}=   Split String        ${ussd_response}
    Log     ${split_msg}
    ${length}=      Get Length     ${split_msg}
    ${index}=   Evaluate    ${length} - 1
    Log     ${index}
    ${ref}=     Get From List       ${split_msg}    ${index}
    Log     ${ref}
    [Return]       ${ref}

Perform Airtime Sale Via Ussd
    [Documentation]     Sell Airtime to a subscriber successfully
    ...     Arguments are agent MSISDN, subscriber MSISDN, amount, pin of agent, ussd for airtime sale
    [Arguments]     ${agent}    ${subscriber}   ${amount}   ${pin}  ${ussd}     ${ussd_response}=SUCCESS
    ${response}=    Send Ussd Request     ${agent}    *${ussd}*${subscriber}*${amount}*${pin}#
    ${ref}=      Get Ref From Ussd Response    ${response}
    ${amount}=   Convert To Integer   ${amount}
    ${amount_currency}=    Format String    {:,}    ${amount}
    IF  "${ussd_response}" == "SUCCESS"
        USSD Response Should Start With    ${response}    You have Sold ${amount_currency} Fcfa Airtime to ${subscriber}.
    ELSE
        USSD Response Should Start With    ${response}    ${ussd_response}
    END
    [Return]    ${ref}


Perform Pin Change Via Ussd
    [Documentation]     Change pin successfully
    ...     Arguments are old pin, new pin and confirm pin for pin change
    [Arguments]     ${agent}    ${USSD}    ${old_pin}   ${new_pin}   ${confirm_pin}
    ${response}=    Send Ussd Request     ${agent}    *${USSD}*${old_pin}*${new_pin}*${Confirm_PIN}#
    ${ref}      Get Ref From Ussd Response    ${response}
    USSD Response Should Start With        ${response}    You have successfully changed your PIN.
    [Return]    ${ref}

Perform Transaction Enquiry Via Ussd
    [Documentation]     Perform a transaction enquiry successfully
    ...     Argument is pin
    [Arguments]      ${agent}   ${USSD}      ${pin}
    ${response}=    Send Ussd Request     ${agent}    *${USSD}*${PIN}#
    ${ref}      Get Ref From Ussd Response    ${response}
  #  USSD Response Should Start With        ${response}    Status of
    [Return]    ${ref}

