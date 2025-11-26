*** Settings ***
Resource    ../keywords/gui.resource

*** Test Cases ***

Wait for Crediverse login page to load successfully
    [Teardown]  Close All Browsers
    [Tags]       startup
    Open Admin Gui Login Page
    Wait For Page Title     ECDS Login  200     1s

Wait for supplier to login Successfully
    [Teardown]  Close All Browsers
    [Tags]       startup
    Open Admin Gui Login Page
    Wait For Successful Supplier Login      ECDS | Dashboard  200     5s