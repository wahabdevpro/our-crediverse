# Feature Bar Support and Operating instructions 

## Context 

A customer have bought or otherwise gained the right to a feature, or the right
to a feature  needs to be revoked from a customer.

*NOTE:* Features must only be switched off after making sure that having had
the feature switched on didn't leave traces behind that can influence system
behaviour. For example if the feature allowed a configuration change that is
not possible without the feature the configuration change might need to be
reverted.

Depending on the circumstances, switching a feature off might require a manual
support activity, all cases will have to be individually evaluated to determine
the impact and feasibility of switching the feature off. 

## Support Responsibilities 

### 1. Creating and managing the Feature Bar certificates files.
*SECURITY NOTE:*  A fully functional, but *insecure*, set these files that is used for
development are created as part of the FeatureBar project and are distributed to
the docker images automatically for ease of testing.  It is *not secure* to use
the development set in production.  For the first release of the FeatureBar it
has been decided to use the development certificates and keys to ease us into
managing them. 

#### TLS Files and how to create them

#####  Certificate Authority Private Key

This file is to be used to sign server and client certificate requests.  It is
to be kept secret at all times.  The version of this file in the git repo is
for *development purposes only*. 

*Filename:* `concurrent_ca_key.pem` 

*command:* 

```openssl genrsa -out concurrent_ca_key.pem```

##### Certificate Authority Certificate

This root authority certificate is created based on the certificate authority
private key.  It is to be shiped with client and server docker images. 

*Filename:*`concurrent_ca.crt`     

*command:* 

```openssl req -new -x509 -key concurrent_ca_key.pem -sha256 -days 35600 -subj '/CN=featurebar/O=Concurrent/C=ZA' -out concurrent_ca.crt```


##### Server Private Key

This is the key the server uses to prove who it is. It is to be shipped with
server. Ideally there should be exactly one existant copy of this and it should
be in the server docker image or even beter container.

*Filename:*`server.key`            

*command:* 

```openssl genrsa -out server.key```


##### Server Certificate Request

This file is created from the server private key. It is sent to the holder
of the certificate athority private key to be used in the next step.

*Filename:*`server_reqout.txt`

*command:* 

```openssl req -new -key server.key  -subj '/CN=featurebar/O=Concurrent/C=ZA' -out server_reqout.txt```


##### Signed Server Certificate

This file is used by the server, together with the server private key, to prove
to the client that it has been authorized by the certificate authority to speak
to the client. It is shipped with server.  Like with the server private key it
would be best if the only extant copy of this file is on the server docker
image or better yet container.

*Filename:*`server.crt`

*command:* 

```openssl x509 -req -in server_reqout.txt -days 3650 -sha256 -CAcreateserial -CA concurrent_ca.crt -CAkey concurrent_ca_key.pem -extfile localhost.ext -out server.crt```

##### Client Private Key

This is the key the client uses to prove who it is. It is to be shipped with
clients. Ideally there should be exactly one existant copy of this and it should
be in the particular client docker image or even beter container.

Each client should ideally have its own key and related files.

*Filename:*`client.key`

*command:* 

```openssl genrsa -out client.key```

##### Client Private Key for Java because Java is special.

Shipped with Java clients.

*Filename:* `client.key.pem`

*command:* 

```openssl pkcs8 -topk8 -nocrypt -in client.key -out client.key.pem```

#####  Client Certificate Request           

This file is created from the client private key. It is sent to the holder
of the certificate athority private key to be used in the next step.

*Filename:*`client_reqout.txt`

*command:* 

```openssl req -new -key client.key -subj '/CN=featurebar/O=Concurrent/C=ZA' -out client_reqout.txt```

##### Signed Client Certificate                  

This file is used by the client, together with the client private key, to prove
to the server that it has been authorized by the certificate authority to speak
to the server. It is shipped with clients.  Like with the client private key it
would be best if the only extant copy of this file is on the client docker
image or better yet container.

*Filename:*`client.crt`

*command:* 

```openssl x509 -req -in client_reqout.txt -days 3650 -sha256 -CAcreateserial -CA concurrent_ca.crt -CAkey concurrent_ca_key.pem -out client.crt```

##### Certificate Authority Serial Number

Created and used by above commands. Keep it with the certificate athority private key.

*Filename:*`concurrent_ca.srl`


### 2. Switching application functionality on and off.

Features are set `true` and `false` directly in the FeatureBar etcd database.
The `etcdctl` etcd.io client is used for this. See
https://etcd.io/docs/v3.5/dev-guide/interacting_v3/ for more information about
how to interact with etcd. 

`etcdctl` can be installed from most Linux package repositories or with
[snapd](https://snapcraft.io/docs/installing-snapd).  Make sure that the
version you get supports `API_VERSION: 3.5` this can be found out by running
`etcdctl` without parameters or by running `etcdctl --version` depending on the
version.  The command line parameters might be different if you get an older
version.  Read the helptext of `etcdctl` to make sure. 

Feature names will be communicated to Support during the installation of the
client application.  The current set of settings can also be requested from the
etcd db.

Since the FeatureBar manages a secure connection between the client and the
server `etcdctl` needs to honour that.  To this it needs to be supplied the
correct key, certificate, and root authority certificate to interact with the
etcd database securly.

Familiarity with etcd and etcdctl need to be cultivated in order to effeciently
interact and diagnose issues with the FeatureBar.

The gui will respond to feature state changes after a page refresh, or at most
after logging out and back in, depending on the particular feature.  


#### Steps to switch features on and of: 
1. Get the client key, client certificate and root authority certificate files
   in a location where you can access them from the console. 

2. Portforward to the machine where the featurebar docker image is run. 
```
ssh -L 2379:localhost:2379 {{user}}@{{machine.hosting.featurebar}}
```

3. Run etcdctl:
```
etcdctl --key={{featureBar_client_certificate_directory}}/client.key.pem 
	--cert={{featureBar_client_certificate_directory}}/client.crt 
	--cacert={{featureBar_client_certificate_directory}}/concurrent_ca.crt 
	'put {{feature_name}} {{true or false}}'
```

#### Examples of how to use etcdctl:

These examples assumes that:
1. the certificates and key are in a sub directory of the current directory called `tls` 
2. the aplication being mananged is called `crediverse`
3. the featureBeingManaged is called `showTheGizmo`

**Get all the features currently managed:**
```
etcdctl --key=./tls/client.key --cert=./tls/client.crt --cacert=./tls/concurrent_ca.crt get crediverse --prefix
```

should return: 

```
crediverse.showTheGizmo
false
crediverse.shareTheBuzz
false
```


**Get a particular feature:**

```
etcdctl --key=./tls/client.key --cert=./tls/client.crt --cacert=./tls/concurrent_ca.crt get crediverse.showTheGizmo 
```

should return

```
crediverse.showTheGizmo
false
```

**Enabling a feature:**

```
etcdctl --key=./tls/client.key --cert=./tls/client.crt --cacert=./tls/concurrent_ca.crt put crediverse.showTheGizmo true
```

should return

```
OK
```

**Disabling a feature:**

```
etcdctl --key=./tls/client.key --cert=./tls/client.crt --cacert=./tls/concurrent_ca.crt put crediverse.showTheGizmo false 
```

should return

```
OK
```


