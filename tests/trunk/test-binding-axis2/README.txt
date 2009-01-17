WSS X509Token tests - Axis2 tests have demonstration of X509token WSS. These tests use self-signed X509 certificate from axis2test.jks.
This certificate is only valid for 3 months from the date of the creation. below are some examples to recreate it.

Create certificate - 

$ keytool -genkey -keystore axis2test1.jks -storepass changeit -keypass changeit -keyalg RSA -alias axis2test
What is your first and last name?
  [Unknown]:  Axis2 Test
What is the name of your organizational unit?
  [Unknown]:  
What is the name of your organization?
  [Unknown]:  fabric3.org
What is the name of your City or Locality?
  [Unknown]:  London
What is the name of your State or Province?
  [Unknown]:  
What is the two-letter country code for this unit?
  [Unknown]:  GB
Is CN=Axis2 Test, OU=Unknown, O=fabric3.org, L=London, ST=Unknown, C=GB correct?
  [no]:  yes

View certificate - 

$ keytool -list -keystore axis2test.jks -storepass changeit -v

Keystore type: jks
Keystore provider: SUN

Your keystore contains 1 entry

Alias name: axis2test
Creation date: Jan 17, 2009
Entry type: keyEntry
Certificate chain length: 1
Certificate[1]:
Owner: CN=Axis2 Test, OU=Unknown, O=fabric3.org, L=London, ST=Unknown, C=GB
Issuer: CN=Axis2 Test, OU=Unknown, O=fabric3.org, L=London, ST=Unknown, C=GB
Serial number: 4971e124
Valid from: Sat Jan 17 13:46:12 GMT 2009 until: Fri Apr 17 14:46:12 BST 2009
Certificate fingerprints:
	 MD5:  B4:55:9E:EB:8A:FA:E8:33:35:2C:D9:B7:B8:9E:9B:96
	 SHA1: 11:36:8D:2C:D1:13:7E:C0:62:46:85:D1:2C:89:B1:E0:C2:A6:AC:F4


*******************************************
*******************************************

