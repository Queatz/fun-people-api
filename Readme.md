Setup
=====

Configure Arango
--------------

Install from
https://www.arangodb.com/download-major/ubuntu/

```shell
echo '{"default":"en_US.UTF-8"}' > /var/lib/arangodb3/LANGUAGE

arangosh --server.username root --server.password root
arangosh> const users = require('@arangodb/users')
arangosh> users.save('fun', 'fun');
arangosh> db._createDatabase('fun')
arangosh> users.grantDatabase('fun', 'fun', 'rw')
```

Configure CertBot
---------------

```shell
apt install certbot default-jre
certbot certonly

export DOMAIN="api.hangoutville.com"
export ALIAS="fun"
openssl pkcs12 -export -out /etc/letsencrypt/live/$DOMAIN/keystore.p12 -inkey /etc/letsencrypt/live/$DOMAIN/privkey.pem -in /etc/letsencrypt/live/$DOMAIN/fullchain.pem -name $ALIAS
keytool -importkeystore -alias $ALIAS -destkeystore /etc/letsencrypt/live/$DOMAIN/keystore.jks -srcstoretype PKCS12 -srckeystore /etc/letsencrypt/live/$DOMAIN/keystore.p12
```

Create secrets.json
---------------

See `Secrets.kt` for the json structure

Run
===

```shell
PORT=80 nohup java -jar api.jar > log.txt 2> errors.txt < /dev/null &
PID=$!
echo $PID > pid.txt
```
