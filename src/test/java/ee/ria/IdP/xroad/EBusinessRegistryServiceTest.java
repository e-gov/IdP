package ee.ria.IdP.xroad;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import ee.ria.IdP.exceptions.XroadServiceNotAvailable;
import ee.ria.IdP.model.EELegalPerson;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static freemarker.template.Configuration.VERSION_2_3_28;
import static org.hamcrest.CoreMatchers.is;


@RunWith(MockitoJUnitRunner.class)
public class EBusinessRegistryServiceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().dynamicPort()
            .dynamicHttpsPort().keystorePassword("changeit")
            .keystorePath("src/test/resources/mock-xroad-keystore.jks"));
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private  EBusinessRegistryService eBusinessRegistryService;

    private Configuration configuration;

    @Before
    public void setUp() throws Exception {
        System.setProperty("javax.net.ssl.trustStore", "src/test/resources/mock-xroad-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

        configuration = new freemarker.template.Configuration(VERSION_2_3_28);
        configuration.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setDefaultEncoding("UTF-8");

        eBusinessRegistryService = new EBusinessRegistryService(configuration, getXroadConfiguration());
    }

    @Test
    public void getSoapRequestSuccessful() {
        String nonce = UUID.randomUUID().toString();
        Assert.assertEquals("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xro=\"http://x-road.eu/xsd/xroad.xsd\" xmlns:iden=\"http://x-road.eu/xsd/identifiers\" xmlns:prod=\"http://arireg.x-road.eu/producer/\">\r\n" +
                "   <soapenv:Header>\r\n" +
                "      <xro:protocolVersion>4.0</xro:protocolVersion>\r\n" +
                "      <xro:userId>EE$&lt;&gt;&quot;&apos;12345678-90</xro:userId>\r\n" +
                "      <xro:id>" + nonce + "</xro:id>\r\n" +
                "      <xro:service iden:objectType=\"SERVICE\">\r\n" +
                "         <iden:xRoadInstance>ee-dev</iden:xRoadInstance>\r\n" +
                "         <iden:memberClass>GOV</iden:memberClass>\r\n" +
                "         <iden:memberCode>70000310</iden:memberCode>\r\n" +
                "         <iden:subsystemCode>arireg</iden:subsystemCode>\r\n" +
                "         <iden:serviceCode>esindus_v2</iden:serviceCode>\r\n" +
                "         <iden:serviceVersion>v1</iden:serviceVersion>\r\n" +
                "      </xro:service>\r\n" +
                "      <xro:client iden:objectType=\"SUBSYSTEM\">\r\n" +
                "         <iden:xRoadInstance>ee-dev</iden:xRoadInstance>\r\n" +
                "         <iden:memberClass>GOV</iden:memberClass>\r\n" +
                "         <iden:memberCode>70006317</iden:memberCode>\r\n" +
                "         <iden:subsystemCode>idp</iden:subsystemCode>\r\n" +
                "      </xro:client>\r\n" +
                "   </soapenv:Header>\r\n" +
                "   <soapenv:Body>\r\n" +
                "      <prod:esindus_v2>\r\n" +
                "         <prod:keha>\r\n" +
                "                <prod:fyysilise_isiku_kood>$&lt;&gt;&quot;&apos;12345678-90</prod:fyysilise_isiku_kood>\r\n" +
                "                <prod:fyysilise_isiku_koodi_riik>EST</prod:fyysilise_isiku_koodi_riik>\r\n" +
                "         </prod:keha>\r\n" +
                "      </prod:esindus_v2>\r\n" +
                "   </soapenv:Body>\r\n" +
                "</soapenv:Envelope>",
                eBusinessRegistryService.getEsindusV2Request("$<>\"'12345678-90", nonce));
    }

    @Test
    public void executeEsindusV2ServiceShouldFailWhenNoIdCode() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("idCode is required!");
        eBusinessRegistryService.executeEsindusV2Service(null);
    }

    @Test
    public void executeEsindusV2ServiceShouldFailWhenInvalidIdCode() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("idCode has invalid format! Must contain only numbers");
        eBusinessRegistryService.executeEsindusV2Service("EE38001085718");
    }

    @Test
    public void executeEsindusV2ServiceSuccessfulWithNoMatch() {

        wireMockRule.stubFor(post(urlEqualTo("/cgi-bin/consumer_proxy"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/xml;charset=utf-8\n")
                        .withStatus(200)
                        .withBodyFile("ok-no-match.xml")));


        List<EELegalPerson> companies = eBusinessRegistryService.executeEsindusV2Service("47101010033");
        Assert.assertTrue(CollectionUtils.isEmpty(companies));
    }

    @Test
    public void executeEsindusV2ServiceShouldFailWhenSoapFault() {

        exception.expect(IllegalStateException.class);
        exception.expectMessage("XRoad service returned a soap fault: faultcode = 'SOAP-ENV:Server', faultstring = 'Sisendparameetrid vigased: palun sisestage kas äriregistri kood, isikukood või isiku ees- ja perekonnanimi.'");

        wireMockRule.stubFor(post(urlEqualTo("/cgi-bin/consumer_proxy"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/xml;charset=utf-8\n")
                        .withStatus(200)
                        .withBodyFile("nok-soapfault.xml")));


        List<EELegalPerson> companies = eBusinessRegistryService.executeEsindusV2Service("47101010033");
        Assert.assertTrue(CollectionUtils.isEmpty(companies));
    }

    @Test
    public void executeEsindusV2ServiceShouldFailWhenOtherThanHttp200() {

        exception.expect(IllegalStateException.class);
        exception.expectMessage("Failed to extract data from response: Server returned HTTP response code: 500 for URL: https://");

        wireMockRule.stubFor(post(urlEqualTo("/cgi-bin/consumer_proxy"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/xml;charset=utf-8\n")
                        .withStatus(500)));


        List<EELegalPerson> companies = eBusinessRegistryService.executeEsindusV2Service("47101010033");
        Assert.assertTrue(CollectionUtils.isEmpty(companies));
    }

    @Test
    public void executeEsindusV2ServiceShouldFailWhenTimeout() {

        exception.expect(XroadServiceNotAvailable.class);
        exception.expectMessage("Connection failed: Read timed out");

        wireMockRule.stubFor(post(urlEqualTo("/cgi-bin/consumer_proxy"))
                .willReturn(aResponse()
                        .withFixedDelay(4000)
                        .withHeader("Content-Type", "text/xml;charset=utf-8\n")
                        .withBodyFile("ok-single-match.xml")));

        XroadConfigurationImpl conf = getXroadConfiguration();
        conf.setXroadServerConnectTimeoutInMilliseconds(1);
        conf.setXroadServerReadTimeoutInMilliseconds(1);

        new EBusinessRegistryService(configuration, conf).executeEsindusV2Service("47101010033");
    }

    @Test
    public void executeEsindusV2ServiceSuccessfulWithSingleMatch() {

        wireMockRule.stubFor(post(urlEqualTo("/cgi-bin/consumer_proxy"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/xml;charset=utf-8\n")
                        .withStatus(200)
                        .withBodyFile("ok-single-match.xml")));


        List<EELegalPerson> companies = eBusinessRegistryService.executeEsindusV2Service("47101010033");
        MatcherAssert.assertThat(companies, is(Arrays.asList(
                new EELegalPerson("12341234", "Acme INC OÜ")
        )));
    }

    @Test
    public void executeEsindusV2ServiceSuccessfulWithMultipleMatches() {

        wireMockRule.stubFor(post(urlEqualTo("/cgi-bin/consumer_proxy"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/xml;charset=utf-8\n")
                        .withStatus(200)
                        .withBodyFile("ok-multiple-matches.xml")));


        List<EELegalPerson> companies = eBusinessRegistryService.executeEsindusV2Service("47101010033");
        MatcherAssert.assertThat(companies, is(Arrays.asList(
                new EELegalPerson("12341234-1", "Acme INC OÜ 1"),
                new EELegalPerson("12341234-2", "Acme INC UÜ 2"),
                new EELegalPerson("12341234-3", "Acme INC TÜ 3"),
                new EELegalPerson("12341234-4", "Acme INC AS 4"),
                new EELegalPerson("12341234-5", "Acme INC TÜH 5"),
                new EELegalPerson("12341234-6", "Acme INC SA 6"),
                new EELegalPerson("12341234-7", "Acme INC MTÜ 7")
        )));
    }

    private XroadConfigurationImpl getXroadConfiguration() {
        XroadConfigurationImpl conf = new XroadConfigurationImpl();
        conf.setXroadServerUrl(wireMockRule.url("/cgi-bin/consumer_proxy"));
        conf.setXroadServerConnectTimeoutInMilliseconds(3000);
        conf.setXroadServerReadTimeoutInMilliseconds(3000);
        conf.setXRoadEsindusv2AllowedTypes(new String[] {"TÜ", "UÜ", "OÜ", "AS", "TÜH", "SA", "MTÜ"});

        conf.setXRoadServiceRoadInstance("ee-dev");
        conf.setXRoadServiceMemberClass("GOV");
        conf.setXRoadServiceMemberCode("70000310");
        conf.setXRoadServiceSubsystemCode("arireg");

        conf.setXRoadClientSubSystemRoadInstance("ee-dev");
        conf.setXRoadClientSubSystemMemberClass("GOV");
        conf.setXRoadClientSubSystemMemberCode("70006317");
        conf.setXRoadClientSubSystemSubsystemCode("idp");
        return conf;
    }
}
