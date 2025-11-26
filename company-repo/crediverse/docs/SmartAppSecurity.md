
# Context

## Diagram
The diagram below describes the traffic flow from the agent smartphone to Crediverse, and all the intervening components. This is expanded upon in the document below.

![Security Diagram](/docs/SecurityDiagram.drawio.png)

## Participants and interfaces

### Components 

#### Smartphone 
The handset running the Crediverse SmartApp.

#### Customer Firewall and Web Application Firewall (WAF)
It is necessary for any operator to have a security domain protecting their internal network at the perimeter, and allow or deny traffic. The SmartApp expects and supports this. 

#### Reverse Proxy and Load Balancer 
In order to provide deployment flexibility, both a Reverse Proxy and an optional Load Balancer are available. The Load Balancer allows for high availability between the Customer Firewall and the MAS. The Reverse Proxy, as a standalone component, provides a secure connection to the MAS, while at the same time providing new security features, e.g. new versions of the TLS protocol, without the need to do a full product upgrade. It is possible for the Reverse Proxy to be configured as a Web Application Firewall for the SmartApp.

The Reverse Proxy component is also utilised as a security layer by the Crediverse Agent Portal.

#### Crediverse Mobile Application Service (MAS)
The MAS serves Crediverse credit distribution functionality securely to the SmartApp.

#### Crediverse 
The operator credit distribution service.

### Interfaces

#### Crediverse Rest interface. 
* Crediverse exposes a REST API over HTTP. 
* Sessions to Crediverse are established through 2 factor authentication process (login and OTP through SMS).
* Is the authentication provider for the Mobile Application. 
* Knowledge of an active session is the authorization grant in a trusted network. 

#### MAS GRPC interface. 
* GRPC over HTTPS
* JWT based security 
* Is responsible for authorization

# Network Security
It is best practice to layer security measures, so a failure in one layer can be stopped by the layer below. This follows the principles of the Defense in Depth approach to security. To support this, the SmartApp architecture relies on the following layers:
* The SmartApp connects to the operator's network via the operator's own network edge firewall, or via a VPN provided by the operator.
* The operator's firewall sends the traffic to the Concurrent Reverse Proxy, where the SSL session is terminated. This ensures that the correct encryption policies are applied towards the SmartApp.
* The Reverse Proxy establishes a new SSL connection via the Load Balancer component (if enabled) and connects to the MAS. The original traffic from the SmartApp is then passed to the MAS for authentication and authorization.
* Having a WAF and reverse proxy in the architecture provides additional security against a malicious actor that attempts to connect directly to the MAS to exploit an undiscovered vulnerability, as the Reverse Proxy has a completely different attack surface from the MAS, making any attack more costly.

# Application Security

## Hosting Obligations
1.  There must exist a public DNS entry to point to the Mobile Application Service from the internet. 
2.  The DNS endpoint for the MAS must have a certificate signed by an Android trusted public Certificate Authority (CA).

## Security claims.

### 1. All communication to the MAS is encrypted 
The channel from the app to the MAS is using HTTPS, so communication is safe from interception. 

### 2. The Mobile Application Service is the one and only MAS!
This is achieved by having the Mobile Application service signed by a public CA.  
The public CA together with the HTTPS security built into Android will ensure that a 3rd party cannot host a fake version of the MAS.

### 3. Only registered Crediverse Agents are Authenticated and Authorized to use the SmartApp.
User authentication is enacted against Crediverse as a 2 factor process (PIN and OTP).  Once the user is authenticated as a Crediverse agent, the application will grant the user a short lived authorization token for subsequent transactions and requests against the MAS.

After being authenticated and authorized to use the MAS, Crediverse applies its user authorization to manage the authenticated user permissions.

### 4. The implementation details on the handset is protected against reverse engineering
The application software installed on agent handsets is somewhat protected against reverse engineering by processing it through the standard Android code obfuscater.

