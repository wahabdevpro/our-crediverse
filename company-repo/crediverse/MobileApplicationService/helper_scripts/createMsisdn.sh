echo '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:prot="http://protocol.airsim.services.hxc/">
   <soapenv:Header/>
   <soapenv:Body>
      <prot:addSubscriber>
         <msisdn>'$1'</msisdn><languageID>4</languageID>
         <serviceClass>11</serviceClass>
         <accountValue>'$2'</accountValue><state>active</state>
      </prot:addSubscriber>
   </soapenv:Body>
</soapenv:Envelope>' | curl -v -H "Content-Type: text/xml" --data @/dev/stdin 'http://127.0.0.1:10012/Air'
