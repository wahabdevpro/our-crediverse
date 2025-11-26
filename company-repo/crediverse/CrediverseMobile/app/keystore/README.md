# Create keys for MovIvy

```
$ keytool -genkeypair -v -keystore movivy_selfsigned_apk_keystore.jks -keyalg RSA -keysize 2048 -validity 3650 -alias movivyapk
Enter keystore password:
Re-enter new password:
What is your first and last name?
  [Unknown]:  Moov Côte d'Ivoire
What is the name of your organizational unit?
  [Unknown]:  MyMoov-Cabine
What is the name of your organization?
  [Unknown]:  Moov Côte d'Ivoire
What is the name of your City or Locality?
  [Unknown]:  Yamoussoukro
What is the name of your State or Province?
  [Unknown]:  Abidjan
What is the two-letter country code for this unit?
  [Unknown]:  CI
Is CN=Moov Côte d'Ivoire, OU=MyMoov-Cabine, O=Moov Côte d'Ivoire, L=Yamoussoukro, ST=Abidjan, C=CI correct?
  [no]:  yes

Generating 2 048 bit RSA key pair and self-signed certificate (SHA256withRSA) with a validity of 3 650 days
	for: CN=Moov Côte d'Ivoire, OU=MyMoov-Cabine, O=Moov Côte d'Ivoire, L=Yamoussoukro, ST=Abidjan, C=CI
[Storing movivy_selfsigned_apk_keystore.jks]
```

# Create keys for MovTog


```
$ keytool -genkeypair -v -keystore movtog_selfsigned_apk_keystore.jks -keyalg RSA -keysize 2048 -validity 3650 -alias movtogapk
Enter keystore password:
Re-enter new password:
What is your first and last name?
  [Unknown]:  Moov Togo
What is the name of your organizational unit?
  [Unknown]:  MyMoov-Cabine
What is the name of your organization?
  [Unknown]:  Moov Togo
What is the name of your City or Locality?
  [Unknown]:  Lomé
What is the name of your State or Province?
  [Unknown]:  Maritime
What is the two-letter country code for this unit?
  [Unknown]:  TG
Is CN=Moov Togo, OU=MyMoov-Cabine, O=Moov Togo, L=Lomé, ST=Maritime, C=TG correct?
  [no]:  yes

Generating 2 048 bit RSA key pair and self-signed certificate (SHA256withRSA) with a validity of 3 650 days
	for: CN=Moov Togo, OU=MyMoov-Cabine, O=Moov Togo, L=Lomé, ST=Maritime, C=TG
[Storing movtog_selfsigned_apk_keystore.jks]
```
