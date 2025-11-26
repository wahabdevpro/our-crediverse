MSISDN recycling is linked to card https://www.wrike.com/open.htm?id=440878173

The functionality is designed to allow for agent msisdn's to be re-used as currently there is
a unique constraint restriction on msisdn in the ea_agent table.

The functionality was complete based on the spec but then put on hold. The instructions
below explain how to re-instate this functionality

First find the scripts below in services/creditDistributionService/pkgextra.share-config/sql

 - Rename to statndar upgrade and downgrade naming.
 - Then change teh upgrade and down grade version numbers to match the current state of the project

upgrade_ecds_olap-MSISDN-RECYCLING-ONHOLD.sql
upgrade_ecds_oltp-MSISDN-RECYCLING-ONHOLD.sql
downgrade_ecds_oltp-MSISDN-RECYCLING-ONHOLD.sql
downgrade_ecds_olap-MSISDN-RECYCLING-ONHOLD.sql

Next search the entire code base for the text
"Functionality on hold MSISDN-RECYCLING"

and uncomment any code below it or follow specific instructions if there are any.

IN the gui code open the file
/projects/ecds-gui/src/main/resources/static/js/app/Dashboard.handlerbars

search for the text ---- > "Functionality on hold MSISDN-RECYCLING"

and uncomment the menu item below it.

That is it, you should be done after running the upgrade scripts