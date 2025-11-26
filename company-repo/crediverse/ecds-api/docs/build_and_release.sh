#!/bin/bash
#Release notes:
what="ecds-api"
version=$(ssh donovanh@warehouse.concurrent.systems -C 'find /var/opt/webdav/warehouse.concurrent.co.za/releases/ecds-api/staging/  -printf "%T+\t%p\n" -maxdepth 1' | tail -1 | sed 's~.*staging/\(.*\)$~\1~g')
release_notes="ecds-api-release-${version}"
api_guide="api-getting-started-guide-${version}"

echo "Running for version ${version}"
echo "Creating warehouse directories..."
ssh donovanh@warehouse.concurrent.systems "mkdir -p /var/opt/webdav/warehouse.concurrent.co.za/releases/ecds-api/staging/${version}/release_notes/"
ssh donovanh@warehouse.concurrent.systems "mkdir -p /var/opt/webdav/warehouse.concurrent.co.za/releases/ecds-api/staging/${version}/documentation/"


echo "Generating pdf files..."
asciidoctor -r asciidoctor-pdf -b pdf --attribute pdf-stylesdir=asciidoctor-common/pdf-theme/ --attribute pdf-fontsdir=asciidoctor-common/pdf-theme/ --attribute pdf-style=definition.yml --attribute pdf-page-size=A4 --attribute doctype=book --out-file "${release_notes}.pdf" "release.asciidoc"
asciidoctor -r asciidoctor-pdf -b pdf --attribute pdf-stylesdir=asciidoctor-common/pdf-theme/ --attribute pdf-fontsdir=asciidoctor-common/pdf-theme/ --attribute pdf-style=definition.yml --attribute pdf-page-size=A4 --attribute doctype=book --out-file "${api_guide}.pdf"  "api-getting-started-guide.asciidoc"  

echo "Copying to warehouse..."
scp ${release_notes}.{pdf,html} donovanh@warehouse.concurrent.systems:/var/opt/webdav/warehouse.concurrent.co.za/releases/ecds-api/staging/${version}/release_notes/
scp ${api_guide}.{pdf,html} donovanh@warehouse.concurrent.systems:/var/opt/webdav/warehouse.concurrent.co.za/releases/ecds-api/staging/${version}/documentation/

ga_version=$(ssh donovanh@warehouse.concurrent.systems -C 'find /var/opt/webdav/warehouse.concurrent.co.za/releases/'${what}'/generally_available/  -printf "%T+\t%p\n" -maxdepth 1' | grep -E "[1-9]+\.[0-9]+\.[0-9]+-[a-zA-Z]+-[0-9]+" | tail -1 | sed 's~.*generally_available/\(.*\)$~\1~g')

echo "Generally available version ${ga_version}"
if [ "${ga_version}" = "${version}" ]; then
	echo "Latest staging release already in Generally Available. Syncing changes to generally available."
	echo "Creating warehouse directories in generally available for release notes and product documentation..."
	ssh donovanh@warehouse.concurrent.systems "mkdir -p /var/opt/webdav/warehouse.concurrent.co.za/releases/${what}/generally_available/${version}/release_notes/"
	ssh donovanh@warehouse.concurrent.systems "mkdir -p /var/opt/webdav/warehouse.concurrent.co.za/releases/${what}/generally_available/${version}/documentation/"

	echo "Copying release notes and product documentation to warehouse..."
	scp ${release_notes}.{html,pdf} donovanh@warehouse.concurrent.systems:/var/opt/webdav/warehouse.concurrent.co.za/releases/${what}/generally_available/${version}/release_notes/
	find . -name "*.pdf" | grep -vE "(${release_notes}|moov.ci-mysql-restore-replication.pdf)" | xargs -I{} scp {} donovanh@warehouse.concurrent.systems:/var/opt/webdav/warehouse.concurrent.co.za/releases/${what}/generally_available/${version}/documentation/
	find . -name "*.html" | grep -vE "(${release_notes}|moov.ci-mysql-restore-replication.html)" | xargs -I{} scp {} donovanh@warehouse.concurrent.systems:/var/opt/webdav/warehouse.concurrent.co.za/releases/${what}/generally_available/${version}/documentation/
else
	echo "Generally available version (${ga_version}) not equal to staging version (${version})"
fi


echo "Done."
echo "evince ${release_notes}"
echo "evince ${api_guide}"
