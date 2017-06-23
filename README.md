## Demo Concepts

* One front end Application (client) with /hello endpoint.
* One Backend application (server) with /hello endpoint.
* Server has a TCP route with mutual SSL enabled
* Client use restTemplate to communicate with server through mutual SSL
* Using environment variables to store keystores

## Deep Dive

 SSL (Secure Sockets Layer) uses a public key in combination with a symmetric
 key encryption to secure a connection between the server and the client.

### Components:

 1. Keystore
 A keystore is basically a database of keys. It will contain the certificate
 or chain of certificates that we will use for authentication.
 Keystore information can be grouped into two categories:
 key entries and trusted certificate entries.
 A key entry consists of an entity's identity and its private key, and
 can be used for a variety of cryptographic purposes.
 In contrast, a trusted certificate entry contains only a public key in
 addition to the entity's identity. Thus, a trusted certificate entry
 cannot be used where a private key is required, such as in a
 javax.net.ssl.KeyManager. In the JDK implementation of JKS, a keystore
 may contain both key entries and trusted certificate entries.

 2. Truststore
 A truststore is a keystore that is used when making decisions about what
 to trust. If you receive data from an entity that you already trust, and
 if you can verify that the entity is the one that it claims to be, then
 you can assume that the data really came from that entity.
 An entry should only be added to a truststore if the user trusts that
 entity. By either generating a key pair or by importing a certificate,
 the user gives trust to that entry. Any entry in the truststore is
 considered a trusted entry.

### The Handshake

 1. The server communicates to the client with a cert that verifies it's
  legitimacy. The authentication is done with the public key encryption
  to validate the cert.
 2. After the first step is successful, the client and the server establish
 a shared key which will be used for encryption for all further communication.
 3. The client also needs to send its cert every time a new SSL session
  is established.

 To see the handshake process for the deployed app:
 Tail the client/server logs by going to the PCF apps manager instance,
 `console.run.pivotal.io` if you are running it in PWS. Now search for "*** "
  in the logs. You will see the handshake process in action:
   - ClientHello
   - ServerHello
   - CertificateRequest
   - ServerHelloDone
   - ClientKeyExchange
   - CertificateVerify
   - Finished

  There is a lot of other information logged during the handshake which
  shows the above described handshake in detail such as the keys used,
  the type of encryption, validity of the cert etc.

  Once the handshake is successful, the client can now send requests to
  the server.

  ![Handshake explained](http://www.websequencediagrams.com/cgi-bin/cdraw?lz=bm90ZSBvdmVyIENsaWVudCBLZXlTdG9yZSwACQdUcnVzdAAMBgAcBzoAIwhsb2FkcwAWCyBjb250YWluaW5nIHRydXN0ZWQgc2VydmVyIGNlcnRzXG4ALA0AYggAKwxzaWduZWQgYwCBBgYAMQUKAIETBi0-K1MARwU6IFJlcXVlc3RzIHByb3RlY3RlZCByZXNvdXJjZQoAHgYtPi0AgSQIUHJlc2VudHMAfgwASQoAgVQROiBWZXJpZmllAB0UAIICCwBXC1ZhbGlkYXRlZABGEQCCRAgAgSgLAIFPCwBNCACCaggAgSoLUmV0dXJuABoUAIF8CwCBUQkASAwAgXkHAIF2C0Fja25vd2xlZGdlcyB2AIEmBmlvbgCCTgkAgk4IQWNjZXNzZQCCQhQ&s=default
)

### Client

* `generate_keystore` script will create the client trust certs which
 will be placed in the resources folder.
* When we push the client app, looking at the logs we will see that it
 downloads the certificate trust store and adds the generated certs from
 above in it.
 <pre>
 [...]
 Downloaded app package (12.3M)
 -----> Java Buildpack Version: v3.15 (offline) | https://github.com/cloudfoundry/java-buildpack.git#a3a9e61
 -----> Downloading Open Jdk JRE 1.8.0_121 from https://java-buildpack.cloudfoundry.org/openjdk/trusty/x86_64/openjdk-1.8.0_121.tar.gz (found in cache)
        Expanding Open Jdk JRE to .java-buildpack/open_jdk_jre (1.1s)
 -----> Downloading Open JDK Like Memory Calculator 2.0.2_RELEASE from https://java-buildpack.cloudfoundry.org/memory-calculator/trusty/x86_64/memory-calculator-2.0.2_RELEASE.tar.gz (found in cache)
        Memory Settings: -Xss349K -Xmx681574K -XX:MaxMetaspaceSize=104857K -Xms681574K -XX:MetaspaceSize=104857K
 -----> <b>Downloading Container Certificate Trust Store </b>2.0.0_RELEASE from https://java-buildpack.cloudfoundry.org/container-certificate-trust-store/container-certificate-trust-store-2.0.0_RELEASE.jar (found in cache)
        <b>Adding certificates to .java-buildpack/container_certificate_trust_store/truststore.jks </b>(0.4s)
 -----> Downloading Spring Auto Reconfiguration 1.10.0_RELEASE from https://java-buildpack.cloudfoundry.org/auto-reconfiguration/auto-reconfiguration-1.10.0_RELEASE.jar (found in cache)
 Exit status 0
 [...]
 </pre>

* Once the app is started, we can make the requests to the endpoint by
 `curl https://mutualssl-client.cfapps.io/hello` and we should see the
 "Hello SpringBoot" response.
* In the handshake we saw that the client verifies that the host name in
  the cert matches the server host; thus when running the demo locally,
  we will get a `SSLHandshakeException`. We can get around this problem
  by overriding HostnameVerifier to return true when it sees a server
  hosted locally making a request. We would of course not do this in production.



### Server
* The application.yml file looks like this:
  <pre>
  server:
    port: 8081
    ssl:
      "key-store" : 'classpath:server.jks'
      "key-store-password" : password
      "key-password" : password
      "trust-store" : 'classpath:server_trust.jks'
      "trust-store-password" : password
      <b>"client-auth" : "need"</b>
  </pre>
  The last line in the file indicates that only the clients that have
  been authenticated will get a response from the server.


## Steps to run the demo

1. Have tcp domain/routes enabled and point the DNS record to tcp load balancer

    ```
    $ cf domains
    Getting domains in org test as admin...
    name            status   type
    cfapps.io       shared
    cf-tcpapps.io   shared   tcp
    ```

2. Generate Certificates

 This script generates client and server keystores, trust-stores, certificates and places them in src/main/resources folders


    ```
    generate_keystore.sh [SERVER_DOMAIN] [CLIENT_DOMAIN]

    E.g ./generate_keystore.sh cf-tcpapps.io cfapps.io
    ```

3.  Build the project

    ```
    mvn clean package
    ```

4.  a. Configure the backend server on manifest.yml

    ```
    ---
    applications:
    - name: mutualssl-client
      memory: 512M
      path: client/target/client-0.0.1-SNAPSHOT.jar
      env:
        BACKEND_SERVER: https://cf-tcpapps.io:3398/hello
    ```

    b. Configure the client keystores

    ```
    applications:
    - name: mutualssl-client
      memory: 512M
      path: client/target/client-0.0.1-SNAPSHOT.jar
      env:
        BACKEND_SERVER: https://cf-tcpapps.io:3398/hello
        KEY_STORE: /home/vcap/app/BOOT-INF/classes/client.jks
        KEY_STORE_PASSWORD: s3cr3t
        TRUST_STORE: /home/vcap/app/BOOT-INF/classes/client_trust.jks
        TRUST_STORE_PASSWORD: s3cr3t
        BACK_END: ENV
    ```

5.  Push both the server and client using cf push. As both the apps are specified in the `manifest.yml` you should not specify app name.

    ```
    cf push
    ```

6.  Verify both the apps are running

    ```
    $ cf apps
    Getting apps in org test / space staging as admin...
    OK

    name               requested state   instances   memory   disk   urls
    mutualssl-client   started           1/1         512M     1G     mutualssl-client.cfapps.io
    mutualssl-server   started           1/1         512M     1G     cf-tcpapps.io:3398
    ```

## Verification

* Access client app through browser or curl

    ```
    https://mutualssl-client.cfapps.io
    ```
* It returns the message from the backend server

* Watch the ssl handshake on both client and server side logs

* If access server directly through curl or browser. It will fail (Watch logs see the client cert request failure)

### References and further reading

http://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html#RelsTM_KM

https://github.com/spring-projects/spring-boot/tree/master/spring-boot-samples/spring-boot-sample-tomcat-ssl

https://www.codenotfound.com/2017/04/spring-ws-https-client-server-example.html

https://github.com/datianshi/mutualssl

