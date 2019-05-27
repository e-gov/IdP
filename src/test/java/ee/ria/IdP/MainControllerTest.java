package ee.ria.IdP;

import com.codeborne.security.mobileid.MobileIDSession;
import ee.ria.IdP.eidas.EidasIdPImpl;
import ee.ria.IdP.exceptions.InvalidAuthData;
import ee.ria.IdP.exceptions.InvalidAuthRequest;
import ee.ria.IdP.exceptions.MobileIdError;
import ee.ria.IdP.exceptions.XroadServiceNotAvailable;
import ee.ria.IdP.model.EELegalPerson;
import ee.ria.IdP.model.EENaturalPerson;
import ee.ria.IdP.model.IdPTokenCacheItem;
import ee.ria.IdP.xroad.EBusinessRegistryService;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.joda.time.format.DateTimeFormat;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

public class MainControllerTest {

    @Rule public LogAppenderResource statisticsLogAppender = new LogAppenderResource("IdpStatistics");

    private IdPMainController idPMainController;

    private MetaDataIMock metaDataIMock;
    private EidasIdPIMock eidasIdPIMock;
    private MobileIDAuthIMock mobileIDAuthIMock;

    @Mock
    private EBusinessRegistryService businessRegistryService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private static final String idcardCert2015 = "-----BEGIN CERTIFICATE----- MIIF3jCCA8agAwIBAgIQL1neZTp6QGVZ/iP/nw1GJzANBgkqhkiG9w0BAQsFADBj MQswCQYDVQQGEwJFRTEiMCAGA1UECgwZQVMgU2VydGlmaXRzZWVyaW1pc2tlc2t1 czEXMBUGA1UEYQwOTlRSRUUtMTA3NDcwMTMxFzAVBgNVBAMMDkVTVEVJRC1TSyAy MDE1MB4XDTE3MTEwNDIwMzMwM1oXDTIxMDMxNTIxNTk1OVowgZUxCzAJBgNVBAYT AkVFMQ8wDQYDVQQKDAZFU1RFSUQxFzAVBgNVBAsMDmF1dGhlbnRpY2F0aW9uMSMw IQYDVQQDDBpBTExLSVZJLEFORFJFUywzNzAwNTA2MDMyMzEQMA4GA1UEBAwHQUxM S0lWSTEPMA0GA1UEKgwGQU5EUkVTMRQwEgYDVQQFEwszNzAwNTA2MDMyMzB2MBAG ByqGSM49AgEGBSuBBAAiA2IABCedqrnxa7v4ishkcafknXY1rOJPiBKDABHKV8H9 7LCPewUgT868tFBmIcuYcg6nTUlJH3QEDTMdRqqyU2t2G8G09QmxvXGc/OLTl1Fs ctYPvknkrNUepi6eibU816W6FaOCAgcwggIDMAkGA1UdEwQCMAAwDgYDVR0PAQH/ BAQDAgOIMFMGA1UdIARMMEowPgYJKwYBBAHOHwEBMDEwLwYIKwYBBQUHAgEWI2h0 dHBzOi8vd3d3LnNrLmVlL3JlcG9zaXRvb3JpdW0vQ1BTMAgGBgQAj3oBAjAiBgNV HREEGzAZgRdhbmRyZXMuYWxsa2l2aUBlZXN0aS5lZTAdBgNVHQ4EFgQU86yJ8ADL 7j0V/3nn13P/168NhUMwIAYDVR0lAQH/BBYwFAYIKwYBBQUHAwIGCCsGAQUFBwME MB8GA1UdIwQYMBaAFLOriLyZ1WKkhSoIzbQdcjuDckdRMGEGCCsGAQUFBwEDBFUw UzBRBgYEAI5GAQUwRzBFFj9odHRwczovL3NrLmVlL2VuL3JlcG9zaXRvcnkvY29u ZGl0aW9ucy1mb3ItdXNlLW9mLWNlcnRpZmljYXRlcy8TAkVOMGoGCCsGAQUFBwEB BF4wXDAnBggrBgEFBQcwAYYbaHR0cDovL2FpYS5zay5lZS9lc3RlaWQyMDE1MDEG CCsGAQUFBzAChiVodHRwOi8vYy5zay5lZS9FU1RFSUQtU0tfMjAxNS5kZXIuY3J0 MDwGA1UdHwQ1MDMwMaAvoC2GK2h0dHA6Ly93d3cuc2suZWUvY3Jscy9lc3RlaWQv ZXN0ZWlkMjAxNS5jcmwwDQYJKoZIhvcNAQELBQADggIBAIiXltGxhtSDaTkaELx4 4T7n8l7AxwzNE4ftk+m7AbU/xCRamxBylNhkSAzZClHESk1Y/2UCA/Fy+ry0ZQCu 9cb+wl1qiPN7fFSXY6UXYfrZEjbvh7HCcPH2J6oPOzqn2mUjcAw9wDJ7mrXwiSR+ 6Pc5IVZX0Gnp8vP4hXRWn8B2f67Ije3OUA3BNZOiF7502Chx1+GHJWCz8iOCiR0t ytybOKSGVn4moUti9bOHb6foLyVwKP9zMi2Qimzfd/9Vggf6ErmQ3rI5r9GoCdX7 uzEXI1W4JM1d4LeRkgutpoicuWObRbm+t3nnk/dTWBgIlbpLxTGmbZ2yZyPbSpHP 6pmvVbFXUhs8I3Fy2dgP1XUj6InW8dzql3zxOg1dd6gK9bMCMwL/acQw12lp/MGT hkCqVmaiOT6Mnp2jr8BP/GcWJukEa2q7unQdY0drP272OJxhnQI3oaROLuo8ZMIQ pkn5suK5+BK0IlwDS/25S7vKBLIVj3d1NPbyhANSsPlQ/ZYlDb2qiX3tl2uSpNam /+8fVokHThOIIYluM1Mc5vmy0Wh7f2FJ1Fvvlk6qBfFnaDukn8hbozI/FUJMLh7t 26jF/HmWOyyqgLB9Ao8uIQ4Z/ZlYEpNCz/EPIFFAeBsJ0PdoL0dycl3Uq75SxYHB 6xeN3wRvP0SHuVAgF/nz0ntd -----END CERTIFICATE-----";
    private static final String idcardCert2018 = "-----BEGIN CERTIFICATE-----\n"+
            "MIIERTCCA6agAwIBAgIQdYC8eVbmuPZbdADmNE9pEDAKBggqhkjOPQQDBDBgMQsw\n"+
            "CQYDVQQGEwJFRTEbMBkGA1UECgwSU0sgSUQgU29sdXRpb25zIEFTMRcwFQYDVQRh\n"+
            "DA5OVFJFRS0xMDc0NzAxMzEbMBkGA1UEAwwSVEVTVCBvZiBFU1RFSUQyMDE4MB4X\n"+
            "DTE4MDgxNTEwMzEwMloXDTIzMDgxMjIxNTk1OVowfzELMAkGA1UEBhMCRUUxKjAo\n"+
            "BgNVBAMMIUrDlUVPUkcsSkFBSy1LUklTVEpBTiwzODAwMTA4NTcxODEQMA4GA1UE\n"+
            "BAwHSsOVRU9SRzEWMBQGA1UEKgwNSkFBSy1LUklTVEpBTjEaMBgGA1UEBRMRUE5P\n"+
            "RUUtMzgwMDEwODU3MTgwdjAQBgcqhkjOPQIBBgUrgQQAIgNiAASiTQ+vcXEM1vaY\n"+
            "cJOcGCY9aT+Wt/vjmG3lhXuyfqQh9bUTLF4H1uk6lrwAxLoDDS1rwJi1a9h47dhB\n"+
            "W1B+N97ieuB/VPwS76RQah0nEbc7d4pe4STTEgh5R6O2DIeJqd6jggIEMIICADAJ\n"+
            "BgNVHRMEAjAAMA4GA1UdDwEB/wQEAwIDiDBHBgNVHSAEQDA+MDIGCysGAQQBg5Eh\n"+
            "AQIBMCMwIQYIKwYBBQUHAgEWFWh0dHBzOi8vd3d3LnNrLmVlL0NQUzAIBgYEAI96\n"+
            "AQIwHwYDVR0RBBgwFoEUMzgwMDEwODU3MThAZWVzdGkuZWUwHQYDVR0OBBYEFM7x\n"+
            "TlNCY9g1mkICg78Vc6pH3elyMGEGCCsGAQUFBwEDBFUwUzBRBgYEAI5GAQUwRzBF\n"+
            "Fj9odHRwczovL3NrLmVlL2VuL3JlcG9zaXRvcnkvY29uZGl0aW9ucy1mb3ItdXNl\n"+
            "LW9mLWNlcnRpZmljYXRlcy8TAkVOMCAGA1UdJQEB/wQWMBQGCCsGAQUFBwMCBggr\n"+
            "BgEFBQcDBDAfBgNVHSMEGDAWgBTAhJkpxE6fOwI09pnhClYACCk+ezB/BggrBgEF\n"+
            "BQcBAQRzMHEwLAYIKwYBBQUHMAGGIGh0dHA6Ly9haWEuZGVtby5zay5lZS9lc3Rl\n"+
            "aWQyMDE4MEEGCCsGAQUFBzAChjVodHRwczovL3NrLmVlL3VwbG9hZC9maWxlcy9U\n"+
            "RVNUX29mX0VTVEVJRDIwMTguZGVyLmNydDAzBgNVHR8ELDAqMCigJqAkhiJodHRw\n"+
            "Oi8vYy5zay5lZS90ZXN0X2VzdGVpZDIwMTguY3JsMAoGCCqGSM49BAMEA4GMADCB\n"+
            "iAJCAIf6HWB8HTwysJp1mT3rk6iuyhoLGP0roXK/Z/w59Lv4p2JfHt5Z/2FC3RI4\n"+
            "4t/N+6VgG6khvlhkNt6THBZaYZoVAkIBFEuOoHXKc2vGt3cbcmOeQS69TKX0zVW+\n"+
            "dzUWk8LIh9ym38adrjvobg0HSuXQAInc4VpDxzfj0+hebIhVDbA84/0=\n"+
            "-----END CERTIFICATE-----\n";

    @Before
    public void setup() {
        metaDataIMock = new MetaDataIMock();
        eidasIdPIMock = new EidasIdPIMock();
        mobileIDAuthIMock = new MobileIDAuthIMock();
        businessRegistryService = Mockito.mock(EBusinessRegistryService.class);

        idPMainController = new IdPMainController( metaDataIMock, eidasIdPIMock, mobileIDAuthIMock, businessRegistryService, 100);
    }

    @Test
    public void metadataTest() throws EIDASSAMLEngineException {
        String metaData = idPMainController.getMetaData();
        assertEquals("mocked_metadata", metaData);
    }

    @Test
    public void authSuccess() throws InvalidAuthRequest {
        Model mockModel = new ExtendedModelMap();
        String view = idPMainController.showWelcome( "not_used_by_mock", null, mockModel );
        assertEquals("welcome", view);
        assertTrue(mockModel.containsAttribute("SAMLRequest"));
        assertEquals("not_used_by_mock", mockModel.asMap().get("SAMLRequest"));

        assertEquals("statistics log contains invalid number of records", 0, statisticsLogAppender.getOutput().size());
    }

    @Test
    public void idauth2015Success() throws InvalidAuthRequest {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("SSL_CLIENT_VERIFY", "SUCCESS");
        mockRequest.addHeader( "SSL_CLIENT_CERT", idcardCert2015);

        Model mockModel = new ExtendedModelMap();
        String view = idPMainController.authenticate(mockRequest, "not_used_by_mock", null, mockModel );
        assertEquals("authorize", view);
        assertTrue(mockModel.containsAttribute("SAMLResponse"));
        assertEquals("mock_response", mockModel.asMap().get("SAMLResponse"));

        assertEquals("statistics log contains invalid number of records", 2, statisticsLogAppender.getOutput().size());
        assertEquals("{\"personType\":\"NATURAL_PERSON\",\"eventType\":\"AUTHENTICATION_STARTED\",\"authType\":\"ID_CARD\",\"country\":\"ET\"}", statisticsLogAppender.getOutput().get(0).getMessage().getFormattedMessage());
        assertEquals("{\"personType\":\"NATURAL_PERSON\",\"eventType\":\"AUTHENTICATION_SUCCESSFUL\",\"authType\":\"ID_CARD\",\"country\":\"ET\"}", statisticsLogAppender.getOutput().get(1).getMessage().getFormattedMessage());
    }

    @Test
    public void idauth2018Success() throws InvalidAuthRequest {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("SSL_CLIENT_VERIFY", "SUCCESS");
        mockRequest.addHeader( "SSL_CLIENT_CERT", idcardCert2018);

        Model mockModel = new ExtendedModelMap();
        String view = idPMainController.authenticate(mockRequest, "not_used_by_mock", null, mockModel );
        assertEquals("authorize", view);
        assertTrue(mockModel.containsAttribute("SAMLResponse"));
        assertEquals("mock_response", mockModel.asMap().get("SAMLResponse"));

        assertEquals("statistics log contains invalid number of records", 2, statisticsLogAppender.getOutput().size());
        assertEquals("{\"personType\":\"NATURAL_PERSON\",\"eventType\":\"AUTHENTICATION_STARTED\",\"authType\":\"ID_CARD\",\"country\":\"ET\"}", statisticsLogAppender.getOutput().get(0).getMessage().getFormattedMessage());
        assertEquals("{\"personType\":\"NATURAL_PERSON\",\"eventType\":\"AUTHENTICATION_SUCCESSFUL\",\"authType\":\"ID_CARD\",\"country\":\"ET\"}", statisticsLogAppender.getOutput().get(1).getMessage().getFormattedMessage());
    }

    @Test
    public void idauthSuccessLegalPerson() throws InvalidAuthRequest {

        eidasIdPIMock = Mockito.mock(EidasIdPIMock.class);
        when(eidasIdPIMock.parseRequest(any())).thenReturn(EidasIdPIMock.getMockRequest(
                ImmutableAttributeMap.builder().putAll(EidasIdPImpl.EE_LEGAL_PERSON_ATTRIBUTES.getAttributes()).build()
                ));
        when(eidasIdPIMock.buildErrorResponse(any())).thenReturn("mock_error_response");
        idPMainController = new IdPMainController( metaDataIMock, eidasIdPIMock, mobileIDAuthIMock, businessRegistryService, 100);


        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addHeader("SSL_CLIENT_VERIFY", "SUCCESS");
        httpServletRequest.addHeader( "SSL_CLIENT_CERT", idcardCert2015);

        Model mockModel = new ExtendedModelMap();
        String view = idPMainController.authenticate(httpServletRequest, "not_used_by_mock", null, mockModel );
        assertEquals("legal-person-select", view);
        assertTrue(mockModel.containsAttribute("SAMLResponse"));
        assertEquals("37005060323", ((EENaturalPerson)httpServletRequest.getSession().getAttribute("naturalPerson")).getIdCode());
        assertEquals("ANDRES", ((EENaturalPerson)httpServletRequest.getSession().getAttribute("naturalPerson")).getFirstName());
        assertEquals("ALLKIVI", ((EENaturalPerson)httpServletRequest.getSession().getAttribute("naturalPerson")).getFamilyName());
        assertEquals("1970.05.06", ((EENaturalPerson)httpServletRequest.getSession().getAttribute("naturalPerson")).getBirthDate().toString(DateTimeFormat.forPattern("yyyy.MM.dd")));
        assertNotNull("Must contain samlRequest attribute in session!",httpServletRequest.getSession().getAttribute("samlRequest"));
        assertEquals("mock_error_response", mockModel.asMap().get("SAMLResponse"));

        assertEquals("statistics log contains invalid number of records", 2, statisticsLogAppender.getOutput().size());
        assertEquals("{\"personType\":\"LEGAL_PERSON_REPRESENTATIVE\",\"eventType\":\"AUTHENTICATION_STARTED\",\"authType\":\"ID_CARD\",\"country\":\"ET\"}", statisticsLogAppender.getOutput().get(0).getMessage().getFormattedMessage());
        assertEquals("{\"personType\":\"LEGAL_PERSON_REPRESENTATIVE\",\"eventType\":\"AUTHENTICATION_SUCCESSFUL\",\"authType\":\"ID_CARD\",\"country\":\"ET\"}", statisticsLogAppender.getOutput().get(1).getMessage().getFormattedMessage());
    }

    @Test(expected = IllegalStateException.class)
    public void idauthFail() throws InvalidAuthRequest {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader( "SSL_CLIENT_CERT", idcardCert2015);

        Model mockModel = new ExtendedModelMap();
        try {
            idPMainController.authenticate(mockRequest, "not_used_by_mock", null, mockModel);
            fail("Should not reach this");
        } catch (Exception e) {
            assertEquals("statistics log contains invalid number of records", 1, statisticsLogAppender.getOutput().size());
            assertEquals("{\"personType\":\"NATURAL_PERSON\",\"eventType\":\"AUTHENTICATION_STARTED\",\"authType\":\"ID_CARD\",\"country\":\"ET\"}", statisticsLogAppender.getOutput().get(0).getMessage().getFormattedMessage());
            throw e;
        }
    }

    @Test
    public void idauthError() throws InvalidAuthRequest {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("SSL_CLIENT_VERIFY", "NONE");

        Model mockModel = new ExtendedModelMap();
        String view = idPMainController.authenticate(mockRequest, "not_used_by_mock", null, mockModel );
        assertEquals("error", view);
        assertTrue(mockModel.containsAttribute("SAMLResponse"));
        assertEquals("mock_error_response", mockModel.asMap().get("SAMLResponse"));

        assertEquals("statistics log contains invalid number of records", 2, statisticsLogAppender.getOutput().size());
        assertEquals("{\"personType\":\"NATURAL_PERSON\",\"eventType\":\"AUTHENTICATION_STARTED\",\"authType\":\"ID_CARD\",\"country\":\"ET\"}", statisticsLogAppender.getOutput().get(0).getMessage().getFormattedMessage());
        assertEquals("{\"personType\":\"NATURAL_PERSON\",\"eventType\":\"AUTHENTICATION_FAILED\",\"authType\":\"ID_CARD\",\"country\":\"ET\",\"error\":\"error.idcard.notfound\"}", statisticsLogAppender.getOutput().get(1).getMessage().getFormattedMessage());
    }

    @Test
    public void midwelcomeSuccess() throws InvalidAuthRequest {
        Model mockModel = new ExtendedModelMap();
        String view = idPMainController.showMobileIdStart("not_used_by_mock", null, mockModel );
        assertEquals("midstart", view);
        assertTrue(mockModel.containsAttribute("SAMLRequest"));
        assertEquals("not_used_by_mock", mockModel.asMap().get("SAMLRequest"));

        assertEquals("statistics log contains invalid number of records", 0, statisticsLogAppender.getOutput().size());
    }

    @Test
    public void midauthSuccess() throws InvalidAuthRequest {
        Model mockModel = new ExtendedModelMap();
        String view = idPMainController.startMobileIdAuth( "not_used_by_mock", null,
                "","+372123", mockModel );
        assertEquals("midwait", view);
        assertTrue(mockModel.containsAttribute("sessionToken"));
        assertEquals("1", mockModel.asMap().get("sessionToken"));

        assertTrue(mockModel.containsAttribute("challenge"));
        assertEquals("mock_challenge", mockModel.asMap().get("challenge"));

        assertTrue(mockModel.containsAttribute("checkUrl"));

        assertEquals("statistics log contains invalid number of records", 1, statisticsLogAppender.getOutput().size());
        assertEquals("{\"personType\":\"NATURAL_PERSON\",\"eventType\":\"AUTHENTICATION_STARTED\",\"authType\":\"MID\",\"country\":\"ET\"}", statisticsLogAppender.getOutput().get(0).getMessage().getFormattedMessage());
    }

    @Test
    public void midauthError1() throws InvalidAuthRequest {
        Model mockModel = new ExtendedModelMap();
        String view = idPMainController.startMobileIdAuth( "not_used_by_mock", null,
                "throw","not_used", mockModel );
        assertEquals("error", view);
        assertTrue(mockModel.containsAttribute("SAMLResponse"));
        assertEquals("mock_error_response", mockModel.asMap().get("SAMLResponse"));

        assertEquals("statistics log contains invalid number of records", 2, statisticsLogAppender.getOutput().size());
        assertEquals("{\"personType\":\"NATURAL_PERSON\",\"eventType\":\"AUTHENTICATION_STARTED\",\"authType\":\"MID\",\"country\":\"ET\"}", statisticsLogAppender.getOutput().get(0).getMessage().getFormattedMessage());
        assertEquals("{\"personType\":\"NATURAL_PERSON\",\"eventType\":\"AUTHENTICATION_FAILED\",\"authType\":\"MID\",\"country\":\"ET\",\"error\":\"error.mobileid\"}", statisticsLogAppender.getOutput().get(1).getMessage().getFormattedMessage());
    }

    @Test
    public void midauthError2() throws InvalidAuthRequest {
        Model mockModel = new ExtendedModelMap();
        String view = idPMainController.startMobileIdAuth( "not_used_by_mock", null,
                "not_used","throw", mockModel );
        assertEquals("error", view);
        assertTrue(mockModel.containsAttribute("SAMLResponse"));
        assertEquals("mock_error_response", mockModel.asMap().get("SAMLResponse"));

        assertEquals("statistics log contains invalid number of records", 2, statisticsLogAppender.getOutput().size());
        assertEquals("{\"personType\":\"NATURAL_PERSON\",\"eventType\":\"AUTHENTICATION_STARTED\",\"authType\":\"MID\",\"country\":\"ET\"}", statisticsLogAppender.getOutput().get(0).getMessage().getFormattedMessage());
        assertEquals("{\"personType\":\"NATURAL_PERSON\",\"eventType\":\"AUTHENTICATION_FAILED\",\"authType\":\"MID\",\"country\":\"ET\",\"error\":\"error.mobileid\"}", statisticsLogAppender.getOutput().get(1).getMessage().getFormattedMessage());
    }

    @Test
    public void midCheckFinished() throws MobileIdError {
        Model mockModel = new ExtendedModelMap();
        MobileIDSession mobileIDSession = mobileIDAuthIMock.startMobileIdAuth("not_used", "not_used");
        IdPTokenCacheItem cacheItem = new IdPTokenCacheItem( "finished",
                eidasIdPIMock.parseRequest("mock_request"), mobileIDSession);
        idPMainController.putCacheItem(cacheItem, "mock_token");
        String view = idPMainController.showMobileIdCheck( new MockHttpServletRequest(),"mock_token", null, mockModel );
        assertEquals("authorize", view);
        assertTrue(mockModel.containsAttribute("SAMLResponse"));
        assertEquals("mock_response", mockModel.asMap().get("SAMLResponse"));

        assertEquals("statistics log contains invalid number of records", 1, statisticsLogAppender.getOutput().size());
        assertEquals("{\"personType\":\"NATURAL_PERSON\",\"eventType\":\"AUTHENTICATION_SUCCESSFUL\",\"authType\":\"MID\",\"country\":\"ET\"}", statisticsLogAppender.getOutput().get(0).getMessage().getFormattedMessage());
    }

    @Test
    public void midCheckFinishedLegalPersonRequested() throws MobileIdError {
        Model mockModel = new ExtendedModelMap();
        MobileIDSession mobileIDSession = mobileIDAuthIMock.startMobileIdAuth("not_used", "not_used");
        IdPTokenCacheItem cacheItem = new IdPTokenCacheItem( "finished",
                eidasIdPIMock.parseRequest("mock_request"), mobileIDSession, true);
        idPMainController.putCacheItem(cacheItem, "mock_token");
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        String view = idPMainController.showMobileIdCheck( httpServletRequest,"mock_token", null, mockModel );
        assertEquals("legal-person-select", view);
        assertEquals("36002121234", ((EENaturalPerson)httpServletRequest.getSession().getAttribute("naturalPerson")).getIdCode());
        assertEquals("mock_firstname", ((EENaturalPerson)httpServletRequest.getSession().getAttribute("naturalPerson")).getFirstName());
        assertEquals("mock_lastname", ((EENaturalPerson)httpServletRequest.getSession().getAttribute("naturalPerson")).getFamilyName());
        assertEquals("1960.02.12", ((EENaturalPerson)httpServletRequest.getSession().getAttribute("naturalPerson")).getBirthDate().toString(DateTimeFormat.forPattern("yyyy.MM.dd")));
        assertNotNull("Must contain samlRequest attribute in session!",httpServletRequest.getSession().getAttribute("samlRequest"));
        assertEquals("mock_error_response", mockModel.asMap().get("SAMLResponse"));

        assertEquals("statistics log contains invalid number of records", 1, statisticsLogAppender.getOutput().size());
        assertEquals("{\"personType\":\"LEGAL_PERSON_REPRESENTATIVE\",\"eventType\":\"AUTHENTICATION_SUCCESSFUL\",\"authType\":\"MID\",\"country\":\"ET\"}", statisticsLogAppender.getOutput().get(0).getMessage().getFormattedMessage());
    }

    @Test
    public void midCheckAlreadyFinished() throws MobileIdError {
        Model mockModel = new ExtendedModelMap();
        MobileIDSession mobileIDSession = mobileIDAuthIMock.startMobileIdAuth("not_used", "not_used");
        IdPTokenCacheItem cacheItem = new IdPTokenCacheItem( "finished",
                eidasIdPIMock.parseRequest("mock_request"), mobileIDSession);
        cacheItem.setCompleted(null);
        idPMainController.putCacheItem(cacheItem, "mock_token");
        String view = idPMainController.showMobileIdCheck( new MockHttpServletRequest(),"mock_token", null, mockModel );
        assertEquals("authorize", view);
        assertTrue(mockModel.containsAttribute("SAMLResponse"));
        assertEquals("mock_response", mockModel.asMap().get("SAMLResponse"));

        assertEquals("statistics log contains invalid number of records", 1, statisticsLogAppender.getOutput().size());
        assertEquals("{\"personType\":\"NATURAL_PERSON\",\"eventType\":\"AUTHENTICATION_SUCCESSFUL\",\"authType\":\"MID\",\"country\":\"ET\"}", statisticsLogAppender.getOutput().get(0).getMessage().getFormattedMessage());
    }

    @Test
    public void midCheckFail() {
        Model mockModel = new ExtendedModelMap();
        String view = idPMainController.showMobileIdCheck( new MockHttpServletRequest(),"not_used_by_mock", null, mockModel );
        assertEquals("fatal_error", view);

        assertEquals("statistics log contains invalid number of records", 0, statisticsLogAppender.getOutput().size());
    }

    @Test
    public void midstatusError() {
        String status = idPMainController.getMobileIdStatus("missing");
        assertEquals("ERROR", status);

        assertEquals("statistics log contains invalid number of records", 0, statisticsLogAppender.getOutput().size());
    }

    @Test
    public void midstatusWait() throws MobileIdError {
        MobileIDSession mobileIDSession = mobileIDAuthIMock.startMobileIdAuth("not_used", "not_used");
        IdPTokenCacheItem cacheItem = new IdPTokenCacheItem( "not_finished",
                eidasIdPIMock.parseRequest("mock_request"), mobileIDSession);
        idPMainController.putCacheItem(cacheItem, "mock_token");

        String status = idPMainController.getMobileIdStatus("mock_token");
        assertEquals("WAIT", status);

        assertEquals("statistics log contains invalid number of records", 0, statisticsLogAppender.getOutput().size());
    }

    @Test
    public void midstatusOk() throws MobileIdError {
        MobileIDSession mobileIDSession = mobileIDAuthIMock.startMobileIdAuth("not_used", "not_used");
        IdPTokenCacheItem cacheItem = new IdPTokenCacheItem( "not_finished",
                eidasIdPIMock.parseRequest("mock_request"), mobileIDSession);
        cacheItem.setCompleted(null);
        idPMainController.putCacheItem(cacheItem, "mock_token");
        String status = idPMainController.getMobileIdStatus("mock_token");
        assertEquals("OK", status);

        assertEquals("statistics log contains invalid number of records", 0, statisticsLogAppender.getOutput().size());
    }

    @Test
    public void fetchLegalPersonsListPersonNotAuthenticated() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        ModelAndView modelAndView = idPMainController.fetchLegalPersonsList(mockRequest);
        assertEquals(BAD_REQUEST, modelAndView.getStatus());
        assertEquals("Authenticated representative of the legal person was not found in session", modelAndView.getModel().get("error"));
    }

    @Test
    public void fetchLegalPersonsBusinessRegistryNotAvailable() throws InvalidAuthData {
        when(businessRegistryService.executeEsindusV2Service(any())).thenThrow(new XroadServiceNotAvailable("error", new IOException()));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.getSession().setAttribute("naturalPerson", new EENaturalPerson("mock-familyname", "mock-firstname", "47101010033"));
        IdPMainController idPMainController = new IdPMainController( metaDataIMock, eidasIdPIMock, mobileIDAuthIMock, businessRegistryService, 100);
        ModelAndView modelAndView = idPMainController.fetchLegalPersonsList(mockRequest);
        assertEquals(BAD_GATEWAY, modelAndView.getStatus());
        assertEquals("Could not connect to business registry", modelAndView.getModel().get("error"));
    }

    @Test
    public void fetchLegalPersonsTechnicalError() throws InvalidAuthData {
        when(businessRegistryService.executeEsindusV2Service(any())).thenThrow(new IllegalStateException("error"));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.getSession().setAttribute("naturalPerson", new EENaturalPerson("mock-familyname", "mock-firstname", "47101010033"));
        IdPMainController idPMainController = new IdPMainController( metaDataIMock, eidasIdPIMock, mobileIDAuthIMock, businessRegistryService, 100);
        ModelAndView modelAndView = idPMainController.fetchLegalPersonsList(mockRequest);
        assertEquals(INTERNAL_SERVER_ERROR, modelAndView.getStatus());
        assertEquals("Unexpected technical exception encountered.", modelAndView.getModel().get("error"));

        assertEquals("statistics log contains invalid number of records", 0, statisticsLogAppender.getOutput().size());
    }

    @Test
    public void fetchLegalPersonsNoMatchingLegalPersons() throws InvalidAuthData {
        when(businessRegistryService.executeEsindusV2Service(any())).thenReturn(new LinkedList<>());

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.getSession().setAttribute("naturalPerson", new EENaturalPerson("mock-familyname", "mock-firstname", "47101010033"));
        IdPMainController idPMainController = new IdPMainController( metaDataIMock, eidasIdPIMock, mobileIDAuthIMock, businessRegistryService, 100);
        ModelAndView modelAndView = idPMainController.fetchLegalPersonsList(mockRequest);
        assertEquals(FORBIDDEN, modelAndView.getStatus());
        assertEquals("No related legal persons found for current user", modelAndView.getModel().get("error"));

        assertEquals("statistics log contains invalid number of records", 0, statisticsLogAppender.getOutput().size());
    }

    @Test
    public void fetchLegalPersonsSuccess() throws InvalidAuthData {
        List<EELegalPerson> results = Arrays.asList(new EELegalPerson("mock-legal-name", "mock-legal-person-identifier"));
        when(businessRegistryService.executeEsindusV2Service(any())).thenReturn(results);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.getSession().setAttribute("naturalPerson", new EENaturalPerson("mock-familyname", "mock-firstname", "47101010033"));
        IdPMainController idPMainController = new IdPMainController( metaDataIMock, eidasIdPIMock, mobileIDAuthIMock, businessRegistryService, 100);
        ModelAndView modelAndView = idPMainController.fetchLegalPersonsList(mockRequest);
        assertEquals(OK, modelAndView.getStatus());
        assertEquals(results, modelAndView.getModel().get("legalPersons"));
        assertEquals(results, (List<EELegalPerson>)mockRequest.getSession().getAttribute("legalPersons"));

        assertEquals("statistics log contains invalid number of records", 0, statisticsLogAppender.getOutput().size());
    }

    @Test
    public void confirmSelectedLegalpersonNoLegalpersonsListInSession() {
        Model mockModel = new ExtendedModelMap();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        try {
            String view = idPMainController.confirmSelectedLegalperson(mockRequest, "mock-legalperson-id", mockModel);
            fail("Should not reach this!");
        } catch (Exception e) {
            Assert.assertEquals("Cannot select a legal person. No legalPersons list found in session", e.getMessage());
            assertSessionCleanup(mockRequest);
            assertEquals("statistics log contains invalid number of records", 0, statisticsLogAppender.getOutput().size());
        }
    }

    @Test
    public void confirmSelectedLegalpersonInvalidLegalPersonSelected() {
        Model mockModel = new ExtendedModelMap();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        List<EELegalPerson> results = Arrays.asList(new EELegalPerson("mock-legal-name", "mock-legalperson-id-1"));
        mockRequest.getSession().setAttribute("legalPersons", results);
        try {
            idPMainController.confirmSelectedLegalperson(mockRequest, "mock-legalperson-id-2", mockModel);
            fail("Should not reach this!");
        } catch (Exception e) {
            Assert.assertEquals("No legal person found with this id 'mock-legalperson-id-2'", e.getMessage());
            assertSessionCleanup(mockRequest);
            assertEquals("statistics log contains invalid number of records", 0, statisticsLogAppender.getOutput().size());
        }
    }

    @Test
    public void confirmSelectedLegalpersonSuccess() throws InvalidAuthData {
        Model mockModel = new ExtendedModelMap();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        List<EELegalPerson> results = Arrays.asList(new EELegalPerson("mock-legal-name", "mock-legalperson-id-1"));
        mockRequest.getSession().setAttribute("legalPersons", results);
        mockRequest.getSession().setAttribute("samlRequest", new EidasIdPIMock().parseRequest("mock"));
        mockRequest.getSession().setAttribute("naturalPerson", new EENaturalPerson("mock-familyname", "mock-firstname", "47101010033"));

        String view = idPMainController.confirmSelectedLegalperson(mockRequest, "mock-legalperson-id-1", mockModel);
        Assert.assertEquals("authorize", view);
        assertEquals("mock_legalperson_response", mockModel.asMap().get("SAMLResponse"));
        assertEquals("https://someurl.com/", mockModel.asMap().get("responseCallback"));
        assertEquals("47101010033", mockModel.asMap().get("personalCode"));
        assertEquals("mock-familyname", mockModel.asMap().get("surName"));
        assertEquals("mock-firstname", mockModel.asMap().get("name"));
        assertEquals("01.01.1971", mockModel.asMap().get("birthDate"));
        assertEquals("mock-legal-name", mockModel.asMap().get("legalPersonName"));
        assertEquals("mock-legalperson-id-1", mockModel.asMap().get("legalPersonIdentifier"));

        assertSessionCleanup(mockRequest);

        assertEquals("statistics log contains invalid number of records", 1, statisticsLogAppender.getOutput().size());
        assertEquals("{\"personType\":\"LEGAL_PERSON_REPRESENTATIVE\",\"eventType\":\"LEGAL_PERSON_SELECTION_SUCCESSFUL\",\"authType\":\"MID\",\"country\":\"ET\"}", statisticsLogAppender.getOutput().get(0).getMessage().getFormattedMessage());
    }

    private void assertSessionCleanup(MockHttpServletRequest mockRequest) {
        assertNull(mockRequest.getSession().getAttribute("legalPersons"));
        assertNull(mockRequest.getSession().getAttribute("samlRequest"));
        assertNull(mockRequest.getSession().getAttribute("naturalPerson"));
    }
}
