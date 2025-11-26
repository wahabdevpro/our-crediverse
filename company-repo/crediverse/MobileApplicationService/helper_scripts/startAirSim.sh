echo '<S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/" xmlns="http://protocol.airsim.services.hxc/"><S:Header/><S:Body><start/></S:Body></S:Envelope>' \
  | xmllint --format - \
  | tee /dev/stderr \
  | curl -H "Content-Type: text/xml" --trace-ascii /dev/stderr  --data @/dev/stdin 'http://localhost:10012/Air'
