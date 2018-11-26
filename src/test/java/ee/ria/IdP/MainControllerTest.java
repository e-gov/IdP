package ee.ria.IdP;

import com.codeborne.security.mobileid.MobileIDSession;
import ee.ria.IdP.exceptions.InvalidAuthRequest;
import ee.ria.IdP.exceptions.MobileIdError;
import ee.ria.IdP.model.IdPTokenCacheItem;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MainControllerTest {
    private IdPMainController idPMainController;

    private MetaDataIMock metaDataIMock;
    private EidasIdPIMock eidasIdPIMock;
    private MobileIDAuthIMock mobileIDAuthIMock;

    private static final String idcardCert = "-----BEGIN CERTIFICATE----- MIIF3jCCA8agAwIBAgIQL1neZTp6QGVZ/iP/nw1GJzANBgkqhkiG9w0BAQsFADBj MQswCQYDVQQGEwJFRTEiMCAGA1UECgwZQVMgU2VydGlmaXRzZWVyaW1pc2tlc2t1 czEXMBUGA1UEYQwOTlRSRUUtMTA3NDcwMTMxFzAVBgNVBAMMDkVTVEVJRC1TSyAy MDE1MB4XDTE3MTEwNDIwMzMwM1oXDTIxMDMxNTIxNTk1OVowgZUxCzAJBgNVBAYT AkVFMQ8wDQYDVQQKDAZFU1RFSUQxFzAVBgNVBAsMDmF1dGhlbnRpY2F0aW9uMSMw IQYDVQQDDBpBTExLSVZJLEFORFJFUywzNzAwNTA2MDMyMzEQMA4GA1UEBAwHQUxM S0lWSTEPMA0GA1UEKgwGQU5EUkVTMRQwEgYDVQQFEwszNzAwNTA2MDMyMzB2MBAG ByqGSM49AgEGBSuBBAAiA2IABCedqrnxa7v4ishkcafknXY1rOJPiBKDABHKV8H9 7LCPewUgT868tFBmIcuYcg6nTUlJH3QEDTMdRqqyU2t2G8G09QmxvXGc/OLTl1Fs ctYPvknkrNUepi6eibU816W6FaOCAgcwggIDMAkGA1UdEwQCMAAwDgYDVR0PAQH/ BAQDAgOIMFMGA1UdIARMMEowPgYJKwYBBAHOHwEBMDEwLwYIKwYBBQUHAgEWI2h0 dHBzOi8vd3d3LnNrLmVlL3JlcG9zaXRvb3JpdW0vQ1BTMAgGBgQAj3oBAjAiBgNV HREEGzAZgRdhbmRyZXMuYWxsa2l2aUBlZXN0aS5lZTAdBgNVHQ4EFgQU86yJ8ADL 7j0V/3nn13P/168NhUMwIAYDVR0lAQH/BBYwFAYIKwYBBQUHAwIGCCsGAQUFBwME MB8GA1UdIwQYMBaAFLOriLyZ1WKkhSoIzbQdcjuDckdRMGEGCCsGAQUFBwEDBFUw UzBRBgYEAI5GAQUwRzBFFj9odHRwczovL3NrLmVlL2VuL3JlcG9zaXRvcnkvY29u ZGl0aW9ucy1mb3ItdXNlLW9mLWNlcnRpZmljYXRlcy8TAkVOMGoGCCsGAQUFBwEB BF4wXDAnBggrBgEFBQcwAYYbaHR0cDovL2FpYS5zay5lZS9lc3RlaWQyMDE1MDEG CCsGAQUFBzAChiVodHRwOi8vYy5zay5lZS9FU1RFSUQtU0tfMjAxNS5kZXIuY3J0 MDwGA1UdHwQ1MDMwMaAvoC2GK2h0dHA6Ly93d3cuc2suZWUvY3Jscy9lc3RlaWQv ZXN0ZWlkMjAxNS5jcmwwDQYJKoZIhvcNAQELBQADggIBAIiXltGxhtSDaTkaELx4 4T7n8l7AxwzNE4ftk+m7AbU/xCRamxBylNhkSAzZClHESk1Y/2UCA/Fy+ry0ZQCu 9cb+wl1qiPN7fFSXY6UXYfrZEjbvh7HCcPH2J6oPOzqn2mUjcAw9wDJ7mrXwiSR+ 6Pc5IVZX0Gnp8vP4hXRWn8B2f67Ije3OUA3BNZOiF7502Chx1+GHJWCz8iOCiR0t ytybOKSGVn4moUti9bOHb6foLyVwKP9zMi2Qimzfd/9Vggf6ErmQ3rI5r9GoCdX7 uzEXI1W4JM1d4LeRkgutpoicuWObRbm+t3nnk/dTWBgIlbpLxTGmbZ2yZyPbSpHP 6pmvVbFXUhs8I3Fy2dgP1XUj6InW8dzql3zxOg1dd6gK9bMCMwL/acQw12lp/MGT hkCqVmaiOT6Mnp2jr8BP/GcWJukEa2q7unQdY0drP272OJxhnQI3oaROLuo8ZMIQ pkn5suK5+BK0IlwDS/25S7vKBLIVj3d1NPbyhANSsPlQ/ZYlDb2qiX3tl2uSpNam /+8fVokHThOIIYluM1Mc5vmy0Wh7f2FJ1Fvvlk6qBfFnaDukn8hbozI/FUJMLh7t 26jF/HmWOyyqgLB9Ao8uIQ4Z/ZlYEpNCz/EPIFFAeBsJ0PdoL0dycl3Uq75SxYHB 6xeN3wRvP0SHuVAgF/nz0ntd -----END CERTIFICATE----- ";

    public MainControllerTest() {
        metaDataIMock = new MetaDataIMock();
        eidasIdPIMock = new EidasIdPIMock();
        mobileIDAuthIMock = new MobileIDAuthIMock();

        idPMainController = new IdPMainController( metaDataIMock, eidasIdPIMock, mobileIDAuthIMock, 100);
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
    }

    @Test
    public void idauthSuccess() throws InvalidAuthRequest {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("SSL_CLIENT_VERIFY", "SUCCESS");
        mockRequest.addHeader( "SSL_CLIENT_CERT", idcardCert);

        Model mockModel = new ExtendedModelMap();
        String view = idPMainController.authenticate(mockRequest, "not_used_by_mock", null, mockModel );
        assertEquals("authorize", view);
        assertTrue(mockModel.containsAttribute("SAMLResponse"));
        assertEquals("mock_response", mockModel.asMap().get("SAMLResponse"));
    }

    @Test(expected = IllegalStateException.class)
    public void idauthFail() throws InvalidAuthRequest {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader( "SSL_CLIENT_CERT", idcardCert);

        Model mockModel = new ExtendedModelMap();
        String view = idPMainController.authenticate(mockRequest, "not_used_by_mock", null, mockModel );
        assertEquals("authorize", view);
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
    }

    @Test
    public void midwelcomeSuccess() throws InvalidAuthRequest {
        Model mockModel = new ExtendedModelMap();
        String view = idPMainController.showMobileIdStart("not_used_by_mock", null, mockModel );
        assertEquals("midstart", view);
        assertTrue(mockModel.containsAttribute("SAMLRequest"));
        assertEquals("not_used_by_mock", mockModel.asMap().get("SAMLRequest"));
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
    }

    @Test
    public void midauthError1() throws InvalidAuthRequest {
        Model mockModel = new ExtendedModelMap();
        String view = idPMainController.startMobileIdAuth( "not_used_by_mock", null,
                "throw","not_used", mockModel );
        assertEquals("error", view);
        assertTrue(mockModel.containsAttribute("SAMLResponse"));
        assertEquals("mock_error_response", mockModel.asMap().get("SAMLResponse"));
    }

    @Test
    public void midauthError2() throws InvalidAuthRequest {
        Model mockModel = new ExtendedModelMap();
        String view = idPMainController.startMobileIdAuth( "not_used_by_mock", null,
                "not_used","throw", mockModel );
        assertEquals("error", view);
        assertTrue(mockModel.containsAttribute("SAMLResponse"));
        assertEquals("mock_error_response", mockModel.asMap().get("SAMLResponse"));
    }

    @Test
    public void midCheckFinished() throws MobileIdError {
        Model mockModel = new ExtendedModelMap();
        MobileIDSession mobileIDSession = mobileIDAuthIMock.startMobileIdAuth("not_used", "not_used");
        IdPTokenCacheItem cacheItem = new IdPTokenCacheItem( "finished",
                eidasIdPIMock.parseRequest("mock_request"), mobileIDSession);
        idPMainController.putCacheItem(cacheItem, "mock_token");
        String view = idPMainController.showMobileIdCheck( "mock_token", null, mockModel );
        assertEquals("authorize", view);
        assertTrue(mockModel.containsAttribute("SAMLResponse"));
        assertEquals("mock_response", mockModel.asMap().get("SAMLResponse"));
    }

    @Test
    public void midCheckAlreadyFinished() throws MobileIdError {
        Model mockModel = new ExtendedModelMap();
        MobileIDSession mobileIDSession = mobileIDAuthIMock.startMobileIdAuth("not_used", "not_used");
        IdPTokenCacheItem cacheItem = new IdPTokenCacheItem( "finished",
                eidasIdPIMock.parseRequest("mock_request"), mobileIDSession);
        cacheItem.setCompleted(null);
        idPMainController.putCacheItem(cacheItem, "mock_token");
        String view = idPMainController.showMobileIdCheck( "mock_token", null, mockModel );
        assertEquals("authorize", view);
        assertTrue(mockModel.containsAttribute("SAMLResponse"));
        assertEquals("mock_response", mockModel.asMap().get("SAMLResponse"));
    }

    @Test
    public void midCheckFail() {
        Model mockModel = new ExtendedModelMap();
        String view = idPMainController.showMobileIdCheck( "not_used_by_mock", null, mockModel );
        assertEquals("fatal_error", view);
    }

    @Test
    public void midstatusError() {
        String status = idPMainController.getMobileIdStatus("missing");
        assertEquals("ERROR", status);
    }

    @Test
    public void midstatusWait() throws MobileIdError {
        MobileIDSession mobileIDSession = mobileIDAuthIMock.startMobileIdAuth("not_used", "not_used");
        IdPTokenCacheItem cacheItem = new IdPTokenCacheItem( "not_finished",
                eidasIdPIMock.parseRequest("mock_request"), mobileIDSession);
        idPMainController.putCacheItem(cacheItem, "mock_token");

        String status = idPMainController.getMobileIdStatus("mock_token");
        assertEquals("WAIT", status);
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
    }

}
