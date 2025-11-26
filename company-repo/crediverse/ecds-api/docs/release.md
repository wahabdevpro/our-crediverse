Revision History
================

<table>
<colgroup>
<col style="width: 20%" />
<col style="width: 27%" />
<col style="width: 52%" />
</colgroup>
<thead>
<tr class="header">
<th>Version</th>
<th>Date</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><p>1.2.2-1.0</p></td>
<td><p>2020-08-05</p></td>
<td><p>Updated and published details of software version: 1.2.2</p></td>
</tr>
</tbody>
</table>

Preface
=======

Software Overview
-----------------

Concurrent Systems' Credit4U Electronic Credit Distribution System - API
is an end-to-end pin-less airtime distribution and sales system which
encompasses all business processes, from the initial credit creation to
the final sale of airtime to the subscriber.

> **Important**
>
> This Release note refers specifically to the ECDS-API release.

Document Overview
-----------------

This document describes version 1.2.2 of Concurrent Systems' Credit4U
Electronic Credit Distribution System - API. It contains the following
information:

-   Supported System Configurations.

-   Inventory of released artifacts.

-   Features that added, enhanced, changed or removed.

-   Any other enhancements or changes.

-   Corrections.

-   Impacts on compatibility and support.

-   Any special considerations.

This document should be used to determine the suitability of a specific
version of this software and to determine impacts of upgrading from one
version to another.

Supported System Configurations
===============================

This section describes the supported system configurations for Credit4U
Electronic Credit Distribution System - API 1.2.2. All aspects of system
configuration described here is mandatory unless explicitly indicated
otherwise.

> **Important**
>
> Any system configuration not adhering to the aspects described here
> will not be supported.

Operating System and ISA  
Any of the following operating systems are supported:

-   Red Hat 7 from 7.2 onwards for x86-64

Software  
-   Java Platform. Any of the following implementations:

    -   Oracle Java Development Kit 7

Database  
Any of the following databases:

-   MySQL 5.5 or later

Web Browser  
Any of the following:

-   Google Chrome 50+

-   Microsoft Internet Explorer 11+

Charging System  
Any of the following charging systems are supported:

-   Ericsson Charging System with the following interfaces:

    -   UCIP versions 5.0

    -   EMA version 15

Core Network  
-   MAP Phase 2+

SMSC  
-   SMPP 3.4

NMS  
-   SNMP 2

Inventory of Released Artefacts
===============================

-   Red Hat 7 package for Intel x86-64 architecture.

    File name  
    `ecds-api-{software-version}.noarch.rpm`

Release Description
===================

WARNING!
========

**Since 2020-Aug-6 we are using only one release notes document for
Crediverse TS, GUI and API:**

<https://gitlab.com/csys/products/ecds/ecds-ts-doc/-/blob/master/docs/released-artefacts.asciidoc>

**Please do not add any release notes here!**

For now, current document will remain here for historical purposes. A
reference to it will be added as a comment in the main release notes
mentioned above.

Release 1.2.2 - 2020-08-04
--------------------------

-   No changes since 1.2.2-rc-5 - 2020-07-02

Release 1.2.2-rc-5 - 2020-07-02
-------------------------------

-   Implemented /account/transaction/last

-   Implemented /account/transaction/bundle/sale

-   Fixes in Swagger file.

Release 1.2.1-rc-0 - 2018-07-19
-------------------------------

### Note

-   Version 1.2.1-rc-0 released to synchronize with changes made to the
    Crediverse backend server.

Release 1.2.0-rc-0 - 2018-07-19
-------------------------------

### Note

-   Version 1.2.0-rc-0 released to synchronize with changes made to the
    Crediverse backend server.

Release 1.1.0-rc-1 - 2018-03-02
-------------------------------

### Note

-   Version 1.1.0-rc-1 released to synchronize with changes made to the
    Crediverse backend server.

Initial Release 1.0.0-rc-1 - 2018-03-02
---------------------------------------

### Features

-   F1661 Crediverse API provides functionality to query account status,
    account transactions, user and agent profile information and
    bundles. The API also provides functionality to transfer credit and
    sell airtime.
