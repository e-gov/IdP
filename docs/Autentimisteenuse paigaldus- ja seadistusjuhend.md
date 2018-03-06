# Autentimisteenuse paigaldus- ja seadistusjuhend
## 1. Üldine

Autentimisteenus on mõeldud töötama Tomcat veebirakendusena. SSL ühendused lõpetatakse väljaspool Tomcati - eeldatavalt Apache veebiserveri poolt. SSL ühenduste lõpetamisel peab veebiserver kontrollima ka kliendi sertifikaatide kehtivust OCSP teenuse kaudu. Autentimisteenus ise sertifikaadi tühistust ei kontrolli.

Tomcati paigaldamine käib vastavalt juhenditele aadressil https://tomcat.apache.org/tomcat-7.0-doc/setup.html.

Eesti id provider eeldab, et tomcatile on paigaldatud eidas-node sõltuvused vastavalt eidas-node paigaldusjuhendile (https://ec.europa.eu/cefdigital/wiki/download/attachments/46992189/eIDAS-Node%20Installation%20and%20Configuration%20Guide.pdf?version=1&modificationDate=1507296786249&api=v2). Tomcat 7 puudutav osa on kirjeldatud leheküljel 17.

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
|:-|:-|
|BaseUrl|URL nii nagu autentimisteenus on nähtav lõppkasutajatele, näiteks "https://eeidp.ria.ee"|
|KeystoreFile|Autentimisteenuse võtmehoidla asukoht, võtmehoidla peab olema jks formaadis.|
|KeystorePassword|Võtmehoidla ja hoidlas olevate võtmete parool.|
|TokenExpiration|Mobiil-ID sessiooni aegumise kestus sekundites.|
|DigiDocServiceUrl|Mobiil-ID teenuse URL. Testkeskkonna url on "https://tsp.demo.sk.ee".|
|DigiDocServiceName|Mobiil-ID teenuse nimi, tesimisel saab kasutatada väärtust "Testimine".|

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

Eesti id provider on ehitatud nii, et logimine on täielikult välise konteineri poolt seadistatav. See tähendab, et väline konteiner peab kättesaadavaks tegema SLF4J api teegi (_slf4j-api-1.7.*.jar_), konteineri haldaja poolt valitud konkreetse logimislahenduse teegid (logbacki puhul näiteks _logback-core-1.2.3.jar_ ja _logback-classic-1.2.3.jar_), kui ka logimise konfiguratsiooni (logbacki puhul kättesaadav konfiguratsioonifail _logback.xml_). Näidisena kasutatav _logback.xml_ on projekti repositooriumis.
