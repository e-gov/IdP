# Autentimisteenuse paigaldus- ja seadistusjuhend
## 1. Üldine

Autentimisteenus on mõeldud töötama Tomcat veebirakendusena. SSL ühendused lõpetatakse väljaspool Tomcati - eeldatavalt Apache veebiserveri poolt. SSL ühenduste lõpetamisel peab veebiserver kontrollima ka kliendi sertifikaatide kehtivust OCSP teenuse kaudu. Autentimisteenus ise sertifikaadi tühistust ei kontrolli.

Tomcati paigaldamine käib vastavalt juhenditele aadressil https://tomcat.apache.org/tomcat-8.5-doc/setup.html.

Eesti id provider eeldab, et tomcatile on paigaldatud eidas-node sõltuvused vastavalt eidas-node paigaldusjuhendile (https://ec.europa.eu/cefdigital/wiki/download/attachments/68330478/eIDAS-Node%20Installation%20Manual%20v1.4.3.pdf?version=1&modificationDate=1536823008963&api=v2). Tomcat 8 puudutav osa on kirjeldatud leheküljel 19.

## 2. Sertifikaadi info edastamine autentimisteenusele

Autentimisteenus eeldab, et sertifikaadi info edastatakse talle HTTP päringu päiste (header) kaudu. Headereid eeldatakse ID-kaardiga autentimise harus peale seda, kui kasutaja on valinud ID-kaardiga autentimise. Vastavaid päiseid tuleks kaitsta kliendipoolsete võltsimiskatsete (_spoofing_) vastu.

Sellele tegevusele vastab url lõpuga "_/idauth_". Selleks, et autentimisteenus saaks kuvada infolehte peaks kliendi sertifikaat olema soovitav (_required_) aga mitte nõutav.

Apache konfiguratsioonis vastab sellele korraldus:

	SSLVerifyClient optional

SSL verifitseerimise tulemust ootab autentimisteenus HTTP päisest "_SSL_CLIENT_VERIFY_". Töödeldavad väärtused on: "_NONE, SUCCESS, GENEROUS, FAILED:reason_":

| Väärtus | Kirjeldus |
|:-|:-|
|NONE|Klient ei saatnud sertifikaati (ID-kaart pole lugejas jne.)|
|SUCCESS|Autentimise protsessi õnnestumine.|
|GENEROUS|Klient ei saatnud sertifikaati (ID-kaart pole lugejas jne.)|
|FAILED|Viga ("_reason_" on veatekst mis logitakse).|

Kõik teised väärtused saavad käsitletud üldise veana.

Kui SSL verifitseerimine õnnestus (päise _SSL_VERIFY_CLIENT_ väärtus _SUCCESS_), siis ootab autentimisteenus kliendi sertifikaati HTTP päises "_SSL_CLIENT_CERT_". Apache konfiguratsioonis on selleks korraldus:

	 SSLOptions  +ExportCertData

Autentimisteenus ootab päises _SSL_CLIENT_CERT_ kliendi sertifikaati PEM kodeeringus (see on Apache veebiserveri puhul vaikeväärtus).

## 3. Autentimisteenuse konfiguratsiooniparameetrid

Autentimisteenust konfigureeritakse kontekstiparameetrite kaudu. Tomcatile sobiv näidiskonfiguratsioon on failis IdP.xml. Konfiguratsiooniparameetrid on järgmised:

| Parameeter | Kirjeldus |
|:----|:----|
|BaseUrl|URL nii nagu autentimisteenus on nähtav lõppkasutajatele, näiteks "https://eeidp.ria.ee"|
|KeystoreFile|Autentimisteenuse võtmehoidla asukoht, võtmehoidla peab olema jks formaadis.|
|KeystorePassword|Võtmehoidla ja hoidlas olevate võtmete parool.|
|TokenExpiration|Mobiil-ID sessiooni aegumise kestus sekundites.|
|DigiDocServiceUrl|Mobiil-ID teenuse URL. Testkeskkonna url on "https://tsp.demo.sk.ee".|
|DigiDocServiceName|Mobiil-ID teenuse nimi, testimisel saab kasutatada väärtust "Testimine".|

Järgnevate parameetrite väärtuseid kasutatakse autentimisteenuse metaandmetes. Kõik väärtused on nõutavad.

	TechnicalContactGivenName
	TechnicalContactSurname
	TechnicalContactPhone
	TechnicalContactEmail
	TechnicalContactCompanyName

	SupportContactGivenName
	SupportContactSurname
	SupportContactPhone
	SupportContactEmail
	SupportContactCompanyName
	
	OrganizationName
	OrganizationDisplayName
	OrganizationUrl
	

Järgnevate parameetrite väärtuseid kasutatakse juriidiliste isikute pärimiseks üle X-tee. Kõik parameetrid on kohustuslikud, kui ei ole kirjelduses väidetud vastupidist.

| Parameeter | Kirjeldus |
|:----|:----|
|XroadServerUrl| X-tee turvaserveri URL. |
|XroadServerConnectTimeoutInMilliseconds| X-tee turvaserveriga ühendumise aegumise piirmäär. Mittekohustuslik parameeter, vaikimisi 3000 |
|XroadServerReadTimeoutInMilliseconds| X-tee turvaserverist vastuse lugemise aegumise piirmäär. Mittekohustuslik parameeter, vaikimisi 3000 |
|XRoadClientSubSystemRoadInstance| Liituja X-tee keskkonna kood. |
|XRoadClientSubSystemMemberClass| Liituja X-tee liikmeklass. |
|XRoadClientSubSystemMemberCode| Liituja asutuse X-tee registrikood.  |
|XRoadClientSubSystemSubsystemCode| Liituja asutuse alamsüsteemi nimi. |
|XRoadServiceRoadInstance| Kasutatava X-tee teenuse pakkuja keskkonna kood. |
|XRoadServiceMemberClass| Kasutatava X-tee teenuse pakkuja liikmeklass |
|XRoadServiceMemberCode| Kasutatava X-tee teenuse pakkuja liikme registrikood. |
|XRoadServiceSubsystemCode| Kasutatava X-tee teenuse alamsüsteemi nimi. |
|XRoadEsindusv2AllowedTypes| Lubatud juriidiliste isikute tüübid komaga eraldatult (näiteks: OÜ,AS). <br><br>Koodid peavad vastama äriregistri esindus_v2 vastuses leitud juriidiliste isikute tüübi piirangutele (oiguslik_vorm elemendi sisu alusel). <br><br>Mittekohustuslik parameeter. Vaikimisi kasutatakse järgmist nimekirja: TÜ, UÜ, OÜ, AS, TÜH, SA, MTÜ |


## 4. Autentimisteenuse kasutatavad krüptovõtmed ja sertifikaadid

Autentimisteenuse võtmeid ja sertifikaate hoitakse konfigureerimisparameetriga "_KeysToreFile_" määratud failis. Võtmete ja võtmepaaride aliased peavad olema järgmised:

| Alias | Kirjeldus |
|:-|:-|
|eidas_signature|Vastuste signeerimiseks kasutatav võtmepaar koos sertifikaadiga.|
|metadata_signature|Metaandmete signeerimiseks kasutatav võtmepaar koos sertifikaadiga.|

Kõiki muude aliastega võtmepaare kasutatakse saabuvate sõnumite dekrüpteerimiseks. Selleks, et võtmeid oleks võimalik levitada partneritele võib selliseid võtmeid olla mitu.

Ilma privaatvõtmeta sertifikaadid mille aliased on kujul:

_eidas_encrypt_CC_, kus CC  on ISO 3166-1 alpha-2 alusel vastava riigi kood - vastavasse riiki saadetavate sõnumite krüpteerimiseks mõeldud sertifikaadid. Tasub meeles pidada, et krüpteerimissertifikaat võib olla määratud ka teise osapoole metaandmetes, võtmehoidlas olevaid võtmeid kasutatakse ainult siis, kui metaandmetes pole sertifikaati määratud.

## 5. Logimine

Logimiseks kasutatakse SLF4J teeki. Logi konfiguratsioon tuleb ette anda väljastpoolt. Vajalikud teegid ja konfiguratsioonifailid ei sisaldu tarnitavas _war_ failis. eIDASe klasside paki nimed algavad "_eu.eidas_" ja id provideri enda klasside pakid ee.ria.IdP.

### 5.1 Seotud sõltuvused

Eesti id provider on ehitatud nii, et logimine on täielikult välise konteineri poolt seadistatav. See tähendab, et väline konteiner peab kättesaadavaks tegema SLF4J api teegi (_slf4j-api-1.7.*.jar_), konteineri haldaja poolt valitud konkreetse logimislahenduse teegid (log4j2 puhul näiteks _log4j-api-2.11.1.jar_, _log4j-core-2.11.1.jar_ ja _log4j-slf4j-impl-2.11.1.jar_), kui ka logimise konfiguratsiooni (log4j2 puhul kättesaadav konfiguratsioonifail _log4j2.xml_). Näidisena kasutatav _log4j2.xml_ on projekti repositooriumis.

### 5.2 Näidiskonfiguratsioon

Projekti repositooriumis on näidiskonfiguratsioonifail [log4j2.xml](../log4j2.xml), mis seadistab log4j2 logima vaikimisi _/opt/tomcat/logs/IdP.log_ faili paketis _ee.ria.IdP_ aset leidvad sündmused `INFO` tasemel ning kõik muu `WARN` tasemel.
Logifailide _roll-over_ toimub iga päev, faili mahupiirangu (100 MB) ületatamisel või siis, kui rakendus taaskäivitatakse. Eelnevad failid pakitakse `gz` formaati ning tõstetakse kausta, mille nimi vastab `yyyy-MM` kuupäevaformaadile.

Logikirjed vormindatakse JSON kujul eraldatuna reavahesümboliga `\n` ning nad sisaldavad järgmisi välju:

| Väli         | Kirjeldus | Alati olemas |
| :----------- | :-------- | :----------- |
| **date** | Sündmuse kuupäev ja kellaaeg ISO-8601 formaadis. Näide: `2018-09-24T11:38:16,278+0000` | Jah |
| **level** | Logisündmuse tase. Võimalikud väärtused (vähim tõsisest kõige tõsisemani): `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `FATAL` | Jah |
| **request** | Päringu meetod ja URL varjestatuna JSON-_escaping_'uga. Puudub, kui logisündmus ei ole väljastatud päringu käigus. Näide: `POST http://eidas-idp.dev:8082/IdP/auth` | Ei |
| **requestId** | Päringut identifitseeriv juhugenereeritud 16 sümboliline tähtede-numbrite kombinatsioon. Puudub, kui logisündmus ei ole väljastatud päringu käigus. | Ei |
| **sessionId** | Päringu sessiooni ID-st genereeritud **sha256** räsi base64 kujul. Puudub, kui logisündmus ei ole väljastatud päringu käigus. | Ei |
| **logger** | Logija nimi. | Jah |
| **thread** | Lõime nimi. | Jah |
| **instance** | Logiva rakenduse instatsi ID. | Jah |
| **message** | Logisõnum varjestatuna JSON-_escaping_'uga. | Jah |
| **throwable** | Vea _stack trace_ varjestatuna JSON-_escaping_'uga. | Ei |

Näide:

```
{"date":"2018-09-24T11:38:16,278+0000", "level":"INFO", "request":"POST http://eidas-idp.dev:8082/IdP/auth", "requestId":"3RXX7MBW4XAUNS9B", "sessionId":"blhd-BvpmW3usNv7BQGd4WwdHRQVN4Afp2hgZ10uc4Q=", "logger":"ee.ria.IdP.eidas.EidasIdPImpl", "thread":"http-nio-8082-exec-2", "instance":"IdP-instance-1", "message":"SAML request ID: _2KLlLNJPWRNP2FjAhWsHpMEVFSoLQhEVEsh6efd0UnTFcnhk3ie_-WgHr.wMpQH"}
```

Näidiskonfiguratsiooni on võimalik seadistada järgnevate parameetrite abil:

| Parameeter        | Kirjeldus | Vaikeväärtus |
| :---------------- | :---------- | :----------------|
| `idp.log.instanceId` | Rakenduse instantsi ID. **NB:** rakenduse paigaldamisel mitme instantsina on soovitatav see parameeter iga instantsi puhul määrata, et erinevatest instantsidest pärit logikirjed oleksid eristatavad. | `eeIdP` |
| `idp.log.dir` | IdP logide baaskaust. | `/opt/tomcat/logs` |
| `idp.log.pattern` | Logisündmuse muster (vt. [Log4j2 Pattern Layout](https://logging.apache.org/log4j/2.x/manual/layouts.html#PatternLayout)). | `{"date":"%d{yyyy-MM-dd'T'HH:mm:ss,SSSZ}", "level":"%level"%notEmpty{, "request":"%enc{%X{request}}{JSON}"}%notEmpty{, "requestId":"%X{requestId}"}%notEmpty{, "sessionId":"%X{sessionId}"}, "logger":"%logger", "thread":"%thread", "instance":"${sys:idp.log.instanceId}", "message":"%enc{%message}{JSON}"%notEmpty{, "throwable":"%enc{%throwable}{JSON}"}}%n` |

Nende parameetrite vaikeväärtusi on võimalik muuta rakenduse käivitamisel etteantavate süsteemiparameetrite abil, näiteks:

```
export JAVA_OPTS="-Didp.log.instanceId=IdP-instance-1 -Didp.log.dir=/var/logs/idp -Didp.log.pattern=%m%n"
```

Logimisel saadaolevad **MDC** (_Mapped Diagnostic Context_) atribuudid:

| Atribuut          | Kirjeldus |
| :---------------- | :-------- |
| `request` | Päringu meetod ja URL. Väärtustamata, kui logisündmus ei ole väljastatud päringu käigus. Näide: `POST http://eidas-idp.dev:8082/IdP/auth` |
| `requestId` | Päringut identifitseeriv juhugenereeritud 16 sümboliline tähtede-numbrite kombinatsioon. Väärtustamata, kui logisündmus ei ole väljastatud päringu käigus. |
| `sessionId` | Päringu sessiooni ID-st genereeritud **sha256** räsi base64 kujul. Väärtustamata, kui logisündmus ei ole väljastatud päringu käigus. |

Et näidiskonfiguratsioon (või ka ise kirjutatud seadistus) rakenduks, tuleb konfiguratsioonifaili asukoht rakendusele süsteemiparameetrite abil ette anda, näiteks:

```
export JAVA_OPTS="-Dlog4j2.configurationFile=file:/opt/tomcat/conf/log4j2.xml"
```

### 5.3 Statistikalogi


Statistikalogi eesmärk on koguda andmeid IdP kasutusstatistika koostamiseks. Statistikalogisse ei salvestata isikuandmeid.

Logikirjes sisalduva `logger` elemendi väärtus on alati `IdpStatistics` ning `message` elemendi sisu logitakse json kujul.

Logitava JSON kirje formaat on järgmine:

| Atribuut          | Kirjeldus |
| :---------------- | :-------- |
| `personType` | Autenditava isiku tüüp. Võimalikud väärtused: `NATURAL_PERSON` - füüsiline isik või `LEGAL_PERSON_REPRESENTATIVE` - füüsiline isik, kes esindab juriidilist isikut  |
| `eventType` | Sündmuse liik. Võimalikud väärtused: `AUTHENTICATION_STARTED`, `AUTHENTICATION_SUCCESSFUL`, `AUTHENTICATION_FAILED`, `LEGAL_PERSON_SELECTION_SUCCESSFUL` |
| `authType` | Kasutaja tuvastamiseks kasutatud autentimisvahendi tüüp. Võimalikud väärtused: `ID_CARD`, `MID` |
| `country` | Autentimise algatanud riigi kood. Peab vastama mustrile `^A-Z{2,2}$` |
| `error` | Vea kood. Täidetud ainult juhul kui `eventType` väärtus on `AUTHENTICATION_FAILED` |


Näide: edukas ID-kaardiga autentimine
````
{"date":"2019-05-16T13:36:23,380+0000", "level":"INFO", "logger":"IdpStatistics", "thread":"localhost-startStop-1", "instance":"IdP-instance-1", "message":"{\"personType\":\"NATURAL_PERSON\",\"eventType\":\"AUTHENTICATION_STARTED\",\"authType\":\"ID_CARD\",\"country\":\"ET\"}"}
{"date":"2019-05-16T13:37:19,110+0000", "level":"INFO", "logger":"IdpStatistics", "thread":"localhost-startStop-1", "instance":"IdP-instance-1", "message":"{\"personType\":\"NATURAL_PERSON\",\"eventType\":\"AUTHENTICATION_SUCCESSFUL\",\"authType\":\"ID_CARD\",\"country\":\"ET\"}"}
````

Näide: viga ID-kaardiga autentimisel
````
{"date":"2019-05-16T13:36:23,380+0000", "level":"INFO", "logger":"IdpStatistics", "thread":"localhost-startStop-1", "instance":"IdP-instance-1", "message":"{\"personType\":\"NATURAL_PERSON\",\"eventType\":\"AUTHENTICATION_STARTED\",\"authType\":\"ID_CARD\",\"country\":\"ET\"}"}
{"date":"2019-05-16T13:36:23,380+0000", "level":"INFO", "logger":"IdpStatistics", "thread":"localhost-startStop-1", "instance":"IdP-instance-1", "message":"{\"personType\":\"NATURAL_PERSON\",\"eventType\":\"AUTHENTICATION_FAILED\",\"authType\":\"ID_CARD\",\"country\":\"ET\",\"error\":\"error.idcard.notfound\"}"}

````