## Demo Concepts

* One front end Application (client) with /hello endpoint.
* One Backend application (server) with /hello endpoint.
* Server has a TCP route with mutual SSL enabled
* Client use restTemplate to communicate with server through mutual SSL
* Using environment variables to store keystores

## Deep Dive

 SSL (Secure Sockets Layer) uses a public key in combination with a symmetric
 key encryption to secure a connection between the server and the client.

### The Handshake

 1. The server communicates to the client with a cert that verifies it's
  legitimacy. The authentication is done with the public key encryption
  to validate the cert.
 2. After the first step is successful, the client and the server establish
 a shared key which will be used for encryption for all further communication.
 3. The client also needs to send its cert every time a new SSL session
  is established.

### Client

* `generate_keystore` script will create the client trust certs which
 will be placed in the resources folder.


- how does client send cert to server with request
- how does the server validate that cert
- (describe handshake)
-

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
