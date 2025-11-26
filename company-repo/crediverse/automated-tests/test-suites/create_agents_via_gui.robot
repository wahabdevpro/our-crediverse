*** Settings ***
Resource    ../keywords/ussd.resource
Resource    ../keywords/gui.resource
Resource    ../keywords/mysql.resource
Suite Setup     Setup For The Suite
Suite Teardown      Teardown For The Suite

*** Variables ***

${SUPPLIER}       supplier
${EINTERMED}        0140399328
${DEFAULT_PIN}      00000
${USSD_AIRTIME_SALE}    410

*** Keywords ***
Setup For The Suite
    Open Admin Gui Login Page
    Login To Admin Gui As Supplier  ${SUPP_PASS}
    Start Airsim

Teardown For The Suite
    Close All Browsers

*** Test Cases ***
TC-CA-01 Successfully add new agent
    [Documentation]     Add a new Crediverse Agent using an MSISDN which is not used by any other agent in the system.
    ${agent_msisdn}=  Generate Random String      6100000000    6200000000
    Create Subscriber On Airsim     ${agent_msisdn}   2       11      100     active
    ${buy_amount}=  Generate Random String      2000    5000
    ${subscriber}=  Generate Random String      7100000000    7200000000
    Create Subscriber On Airsim     ${subscriber}   2       11      100     active
    ${sell_amount}=  Generate Random String      500    1500
    Clear Call and Sms History On Airsim
    Create Agent Via GUI    ${agent_msisdn}     eCabine     Active
    Sms For Msisdn Should Be   ${agent_msisdn}  Your default ECDS PIN is 00000. Send *446*00000*NewPin*NewPin# to change your PIN
    Transfer Via Ussd       ${EINTERMED}  ${agent_msisdn}   ${buy_amount}    ${DEFAULT_PIN}
    Perform Airtime Sale Via Ussd   ${agent_msisdn}  ${subscriber}  ${sell_amount}  ${DEFAULT_PIN}  ${USSD_AIRTIME_SALE}


TC-CA-02 Successfully deactivate active agent
    [Documentation]     Deactivate successfully an active agent from Crediverse
    ${agent_msisdn}=  Generate Random String      6100000000    6200000000
    Create Subscriber On Airsim     ${agent_msisdn}   2       11      100     active
    ${buy_amount}=  Generate Random String      2000    5000
    ${subscriber}=  Generate Random String      7100000000    7200000000
    Create Subscriber On Airsim     ${subscriber}   2       11      100     active
    ${sell_amount}=  Generate Random String      500    1500
    Clear Call and Sms History On Airsim
    Create Agent Via GUI    ${agent_msisdn}     eCabine     Active
    Transfer Via Ussd       ${EINTERMED}  ${agent_msisdn}   ${buy_amount}    ${DEFAULT_PIN}
    Deactivate Agent Via GUI    ${agent_msisdn}
    Sms For Msisdn Should Be   ${agent_msisdn}  Your ECDS account has been deactivated.
    Perform Airtime Sale Via Ussd   ${agent_msisdn}  ${subscriber}  ${sell_amount}  ${DEFAULT_PIN}  ${USSD_AIRTIME_SALE}    Not allowed to trade in User State: Deactivated


TC-CA-03 Successfully suspend active agent
    [Documentation]     Suspend successfully an active agent from Crediverse
    ${agent_msisdn}=  Generate Random String      6100000000    6200000000
    Create Subscriber On Airsim     ${agent_msisdn}   2       11      100     active
    ${buy_amount}=  Generate Random String      2000    5000
    ${subscriber}=  Generate Random String      7100000000    7200000000
    Create Subscriber On Airsim     ${subscriber}   2       11      100     active
    ${sell_amount}=  Generate Random String      500    1500
    Clear Call and Sms History On Airsim
    Create Agent Via GUI    ${agent_msisdn}     eCabine     Active
    Transfer Via Ussd       ${EINTERMED}  ${agent_msisdn}   ${buy_amount}    ${DEFAULT_PIN}
    Suspend Agent Via GUI   ${agent_msisdn}
    Sms For Msisdn Should Be   ${agent_msisdn}  	Your ECDS account has been suspended.
    Perform Airtime Sale Via Ussd   ${agent_msisdn}  ${subscriber}  ${sell_amount}  ${DEFAULT_PIN}  ${USSD_AIRTIME_SALE}     Not allowed to trade in User State: Suspended


TC-CA-04 Successfully add new agent with MSISDN previously used by a deactivated agent
    [Documentation]     Deactivate successfully a suspended agent from Crediverse
    ${agent_msisdn}=  Generate Random String      6100000000    6200000000
    Create Subscriber On Airsim     ${agent_msisdn}   2       11      100     active
    ${buy_amount}=  Generate Random String      2000    5000
    ${subscriber}=  Generate Random String      7100000000    7200000000
    Create Subscriber On Airsim     ${subscriber}   2       11      100     active
    ${sell_amount}=  Generate Random String      500    1500
    Clear Call and Sms History On Airsim
    Create Agent Via GUI    ${agent_msisdn}     eCabine     Active
    Transfer Via Ussd       ${EINTERMED}  ${agent_msisdn}   ${buy_amount}    ${DEFAULT_PIN}
    Deactivate Agent Via GUI    ${agent_msisdn}
    Create Agent Via GUI    ${agent_msisdn}     eCabine     Active
    Sms For Msisdn Should Be   ${agent_msisdn}  Your default ECDS PIN is 00000. Send *446*00000*NewPin*NewPin# to change your PIN
    Transfer Via Ussd       ${EINTERMED}  ${agent_msisdn}   ${buy_amount}    ${DEFAULT_PIN}
    Perform Airtime Sale Via Ussd   ${agent_msisdn}  ${subscriber}  ${sell_amount}  ${DEFAULT_PIN}  ${USSD_AIRTIME_SALE}

TC-CA-05 Successfully add new agent with a group name
    [Documentation]     Add a new Crediverse Agent using an MSISDN which is not used by any other agent in the system.
    ...     Group is also assigned to the agent
    ${group_name}=      SetVariable     DGC_YAKRO_eCabinedGP
    ${agent_msisdn}=  Generate Random String      6100000000    6200000000
    Create Subscriber On Airsim     ${agent_msisdn}   2       11      100     active
    ${buy_amount}=  Generate Random String      2000    5000
    ${subscriber}=  Generate Random String      7100000000    7200000000
    Create Subscriber On Airsim     ${subscriber}   2       11      100     active
    ${sell_amount}=  Generate Random String      500    1500
    Clear Call and Sms History On Airsim
    Create Agent Via GUI    ${agent_msisdn}     eCabine     Active      group=${group_name}
    Sms For Msisdn Should Be   ${agent_msisdn}  Your default ECDS PIN is 00000. Send *446*00000*NewPin*NewPin# to change your PIN
    Transfer Via Ussd       ${EINTERMED}  ${agent_msisdn}   ${buy_amount}    ${DEFAULT_PIN}
    Perform Airtime Sale Via Ussd   ${agent_msisdn}  ${subscriber}  ${sell_amount}  ${DEFAULT_PIN}  ${USSD_AIRTIME_SALE}

TC-CA-06 Successfully add new eIntermed agent with an eMaster owner agent
    [Documentation]     Add a new Crediverse Agent using an MSISDN which is not used by any other agent in the system.
    ...     Group and the owner agent is also assigned
    ${owner_msisdn}=    Set Variable    0101655965
    ${owner_name}=      Set Variable    COCODY_eMaster
    ${group_name}=      Set Variable     GBD_COCODY_eIntermedGP
    ${b_party_msisdn}=   Set Variable    0142307679
    ${agent_msisdn}=  Generate Random String      6100000000    6200000000
    Create Subscriber On Airsim     ${agent_msisdn}   2       11      100     active
    ${buy_amount}=  Generate Random String      2000    5000
    Create Subscriber On Airsim     ${b_party_msisdn}   2       11      100     active
    ${sell_amount}=  Generate Random String      1001    1500
    Clear Call and Sms History On Airsim
    Create Agent Via GUI    ${agent_msisdn}     eIntermed     Active      group=${group_name}    owner_agent=${owner_name}
    Sms For Msisdn Should Be   ${agent_msisdn}  Your default ECDS PIN is 00000. Send *446*00000*NewPin*NewPin# to change your PIN
    Transfer Via Ussd       ${owner_msisdn}  ${agent_msisdn}   ${buy_amount}    ${DEFAULT_PIN}
    Transfer Via Ussd       ${agent_msisdn}  ${b_party_msisdn}  ${sell_amount}  ${DEFAULT_PIN}

TC-CA-07 Successfully add new eIntermed agent with an upstream agent and successfully buy from upstream agent
    [Documentation]     Add a new Crediverse Agent using an MSISDN which is not used by any other agent in the system.
    ...     Group, owner agent and upstream agent is also assigned
    ...     A successful transaction happens from the upstream agent when transfer rule is set to upstream only
    ${owner_msisdn}=    Set Variable    0103028681
    ${owner_name}=      Set Variable    DGC_eMaster
    ${upstream_msisdn}=     Set Variable    0104028690
    ${upstream_name}=       Set Variable    DGC_eMaster_Secondaire
    ${group_name}=      Set Variable     DGC_ADJAM_EINTERMEDGP
    ${agent_msisdn}=  Generate Random String      6100000000    6200000000
    Create Subscriber On Airsim     ${agent_msisdn}   2       11      100     active
    ${buy_amount}=  Generate Random String      2000    5000
    Clear Call and Sms History On Airsim
    Create Agent Via GUI    ${agent_msisdn}     eIntermed     Active      group=${group_name}
    ...     owner_agent=${owner_name}       upstream_agent=${upstream_msisdn}
    Sms For Msisdn Should Be   ${agent_msisdn}  Your default ECDS PIN is 00000. Send *446*00000*NewPin*NewPin# to change your PIN
    Transfer Via Ussd       ${owner_msisdn}  ${upstream_msisdn}   5500    ${DEFAULT_PIN}
    Transfer Via Ussd       ${upstream_msisdn}  ${agent_msisdn}   ${buy_amount}    ${DEFAULT_PIN}

TC-CA-08 Successfully add new eIntermed agent with an upstream agent and failed to buy from non-upstream agent
    [Documentation]     Add a new Crediverse Agent using an MSISDN which is not used by any other agent in the system.
    ...     Group, owner agent and upstream agent is also assigned
    ...     A failure happens when buying from the non-upstream agent when transfer rule is set to upstream only
    ${owner_msisdn}=    Set Variable    0103028681
    ${owner_name}=      Set Variable    DGC_eMaster
    ${upstream_msisdn}=     Set Variable    0104028690
    ${upstream_name}=       Set Variable    DGC_eMaster_Secondaire
    ${non_upstream_agent}=  Set Variable    0104028691
    ${group_name}=      Set Variable     DGC_ADJAM_EINTERMEDGP
    ${agent_msisdn}=  Generate Random String      6100000000    6200000000
    Create Subscriber On Airsim     ${agent_msisdn}   2       11      100     active
    ${buy_amount}=  Generate Random String      2000    5000
    Clear Call and Sms History On Airsim
    Create Agent Via GUI    ${agent_msisdn}     eIntermed     Active      group=${group_name}
    ...     owner_agent=${owner_name}       upstream_agent=${upstream_msisdn}
    Sms For Msisdn Should Be   ${agent_msisdn}  Your default ECDS PIN is 00000. Send *446*00000*NewPin*NewPin# to change your PIN
    Transfer Via Ussd       ${owner_msisdn}  ${non_upstream_agent}   5500    ${DEFAULT_PIN}
    Transfer Via Ussd       ${non_upstream_agent}  ${agent_msisdn}   ${buy_amount}    ${DEFAULT_PIN}    ussd_response=You cannot transfer to this Agent

TC-CA-09 Successfully add new eCabine agent with warning threshold and send notification upon low balance
    [Documentation]     Add a new Crediverse Agent using an MSISDN which is not used by any other agent in the system.
    ...     Group, owner agent and warning threshold is also assigned
    ...     A notification is sent to the agent when the balance goes below the warning threshold
    ${owner_msisdn}=    Set Variable    0103028681
    ${owner_name}=      Set Variable    DGC_eMaster
    ${group_name}=      Set Variable     DGC_ADJAM_ECABINEGP
    ${warning_threshold}=   Set Variable    500
    ${eIntermed_agent}=  Set Variable    0160747508
    ${agent_msisdn}=  Generate Random String      6100000000    6200000000
    Create Subscriber On Airsim     ${agent_msisdn}   2       11      100     active
    ${subscriber}=  Generate Random String      7100000000    7200000000
    Create Subscriber On Airsim     ${subscriber}   2       11      100     active
    ${buy_amount}=  Generate Random String      1900    2000
    ${sell_amount}=  Evaluate   ${buy_amount} - 400
    Clear Call and Sms History On Airsim
    Create Agent Via GUI    ${agent_msisdn}     eCabine     Active      group=${group_name}
    ...     owner_agent=${owner_name}       warning_threshold=${warning_threshold}
    Sms For Msisdn Should Be   ${agent_msisdn}  Your default ECDS PIN is 00000. Send *446*00000*NewPin*NewPin# to change your PIN
    Transfer Via Ussd       ${eIntermed_agent}  ${agent_msisdn}   ${buy_amount}    ${DEFAULT_PIN}
    Perform Airtime Sale Via Ussd   ${agent_msisdn}  ${subscriber}  ${sell_amount}  ${DEFAULT_PIN}  ${USSD_AIRTIME_SALE}
    Sleep   0.25
    ${sms}=    Sms Sent To Msisdn   ${agent_msisdn}      Agent ${agent_msisdn}'s balance has dropped below ${warning_threshold} Fcfa
    Should Be Equal     ${sms}      Agent ${agent_msisdn}'s balance has dropped below ${warning_threshold} Fcfa

TC-CA-10 Successfully add new eCabine agent with warning threshold and send notification upon low balance to agent and supplier
    [Documentation]     Add a new Crediverse Agent using an MSISDN which is not used by any other agent in the system.
    ...     Group, owner agent and warning threshold is also assigned
    ...     A notification is sent to the agent and the supplier when the balance goes below the warning threshold
    ${owner_msisdn}=    Set Variable    0103028681
    ${owner_name}=      Set Variable    DGC_eMaster
    ${group_name}=      Set Variable     DGC_ADJAM_ECABINEGP
    ${warning_threshold}=   Set Variable    800
    ${eIntermed_agent}=  Set Variable    0160747508
    ${agent_msisdn}=  Generate Random String      6100000000    6200000000
    Create Subscriber On Airsim     ${agent_msisdn}   2       11      100     active
    ${subscriber}=  Generate Random String      7100000000    7200000000
    Create Subscriber On Airsim     ${subscriber}   2       11      100     active
    ${buy_amount}=  Generate Random String      1900    2000
    ${sell_amount}=  Evaluate   ${buy_amount} - 700
    Clear Call and Sms History On Airsim
    Create Agent Via GUI    ${agent_msisdn}     eCabine     Active      group=${group_name}
    ...     owner_agent=${owner_name}   upstream_agent=${eIntermed_agent}       warning_threshold=${warning_threshold}
    Sms For Msisdn Should Be   ${agent_msisdn}  Your default ECDS PIN is 00000. Send *446*00000*NewPin*NewPin# to change your PIN
    Transfer Via Ussd       ${eIntermed_agent}  ${agent_msisdn}   ${buy_amount}    ${DEFAULT_PIN}
    Perform Airtime Sale Via Ussd   ${agent_msisdn}  ${subscriber}  ${sell_amount}  ${DEFAULT_PIN}  ${USSD_AIRTIME_SALE}
    Sleep   0.25
    ${sms}=    Sms Sent To Msisdn   ${agent_msisdn}      Agent ${agent_msisdn}'s balance has dropped below ${warning_threshold} Fcfa
    Should Be Equal     ${sms}      Agent ${agent_msisdn}'s balance has dropped below ${warning_threshold} Fcfa
    ${sms_supplier}=    Sms Sent To Msisdn   ${eIntermed_agent}      Agent ${agent_msisdn}'s balance has dropped below ${warning_threshold} Fcfa
    Should Be Equal     ${sms_supplier}      Agent ${agent_msisdn}'s balance has dropped below ${warning_threshold} Fcfa

TC-CA-11 Successfully add new eIntermed agent with an area assigned and enforce area enabled in the rule
    [Documentation]     Add a new Crediverse Agent using an MSISDN which is not used by any other agent in the system.
    ...     Group, area and the owner agent is also assigned
    ...     Transaction only allowed from the area assigned to the agent
    ${owner_msisdn}=    Set Variable    0101046224
    ${owner_name}=      Set Variable    ACB_Marcory_eMaster
    ${group_name}=      Set Variable     	ACB_MARCORY_eMaster_Secondaire
    ${area}=    Set Variable    MARCORY
    ${eIntermed}=   Set Variable    0160688493
    ${agent_msisdn}=  Generate Random String      6100000000    6200000000
    Create Subscriber On Airsim     ${agent_msisdn}   2       11      100     active
    ${buy_amount}=  Generate Random String      5000    10000
    Create Subscriber On Airsim     ${eIntermed}   2       11      100     active
    ${sell_amount}=  Generate Random String      1001    1500
    Clear Call and Sms History On Airsim
    Create Agent Via GUI    ${agent_msisdn}     eMaster Secondaire     Active      group=${group_name}
    ...     owner_agent=${owner_name}   area=${area}
    Sms For Msisdn Should Be   ${agent_msisdn}  Your default ECDS PIN is 00000. Send *446*00000*NewPin*NewPin# to change your PIN
    Set Hlr Data In Airsim      ${agent_msisdn}     612     2       101     30231
    Transfer Via Ussd       ${owner_msisdn}  ${agent_msisdn}   ${buy_amount}    ${DEFAULT_PIN}
    Transfer Via Ussd       ${agent_msisdn}  ${eIntermed}   ${sell_amount}    ${DEFAULT_PIN}


TC-CA-12 Successfully add new eIntermed agent with an area assigned and transaction fails when outside the enforced area
    [Documentation]     Add a new Crediverse Agent using an MSISDN which is not used by any other agent in the system.
    ...     Group, area and the owner agent is also assigned
    ...     Transaction fails when is agent is outside the enforced area
    ${owner_msisdn}=    Set Variable    0101046224
    ${owner_name}=      Set Variable    ACB_Marcory_eMaster
    ${group_name}=      Set Variable     	ACB_MARCORY_eMaster_Secondaire
    ${area}=    Set Variable    MARCORY
    ${eIntermed}=   Set Variable    0160688493
    ${agent_msisdn}=  Generate Random String      6100000000    6200000000
    Create Subscriber On Airsim     ${agent_msisdn}   2       11      100     active
    ${buy_amount}=  Generate Random String      5000    10000
    Create Subscriber On Airsim     ${eIntermed}   2       11      100     active
    ${sell_amount}=  Generate Random String      1001    1500
    Clear Call and Sms History On Airsim
    Create Agent Via GUI    ${agent_msisdn}     eMaster Secondaire     Active      group=${group_name}
    ...     owner_agent=${owner_name}   area=${area}
    Sms For Msisdn Should Be   ${agent_msisdn}  Your default ECDS PIN is 00000. Send *446*00000*NewPin*NewPin# to change your PIN
    Set Hlr Data In Airsim      ${agent_msisdn}     612     2       101     30135
    Transfer Via Ussd       ${owner_msisdn}  ${agent_msisdn}   ${buy_amount}    ${DEFAULT_PIN}
    Transfer Via Ussd       ${agent_msisdn}  ${eIntermed}   ${sell_amount}    ${DEFAULT_PIN}    ussd_response=Not allowed to trade in Cell 612,2,101,30135

TC-CA-13 Successfully add new eIntermed agent with an area assigned and transaction is successful when no location is received
    [Documentation]     Add a new Crediverse Agent using an MSISDN which is not used by any other agent in the system.
    ...     Group, area and the owner agent is also assigned
    ...     Transaction is successful when is agent location couldn't be fetched
    ${owner_msisdn}=    Set Variable    0101046224
    ${owner_name}=      Set Variable    ACB_Marcory_eMaster
    ${group_name}=      Set Variable     	ACB_MARCORY_eMaster_Secondaire
    ${area}=    Set Variable    MARCORY
    ${eIntermed}=   Set Variable    0160688493
    ${agent_msisdn}=  Generate Random String      6100000000    6200000000
    ${buy_amount}=  Generate Random String      5000    10000
    ${sell_amount}=  Generate Random String      1001    1500
    Clear Call and Sms History On Airsim
    Create Agent Via GUI    ${agent_msisdn}     eMaster Secondaire     Active      group=${group_name}
    ...     owner_agent=${owner_name}   area=${area}
    Sms For Msisdn Should Be   ${agent_msisdn}  Your default ECDS PIN is 00000. Send *446*00000*NewPin*NewPin# to change your PIN
    Transfer Via Ussd       ${owner_msisdn}  ${agent_msisdn}   ${buy_amount}    ${DEFAULT_PIN}
    Transfer Via Ussd       ${agent_msisdn}  ${eIntermed}   ${sell_amount}    ${DEFAULT_PIN}

#New tests
#Area
#Warning Thresholds