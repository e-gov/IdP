package ee.ria.IdP;

import ee.ria.IdP.crypto.IdPKeyStore;
import ee.ria.IdP.crypto.IdPSigner;
import ee.ria.IdP.eidas.EidasIdPImpl;
import ee.ria.IdP.metadata.IdPMetadataFetcher;
import ee.ria.IdP.metadata.MetaDataConfigurationI;
import ee.ria.IdP.metadata.MetaDataConfigurationImpl;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import org.junit.Test;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import static org.junit.Assert.assertNotNull;

public class IdPEncryptionTest {

    private KeyStore keyStore;
    private IdPKeyStore idPKeyStore;
    private ProtocolEngineI protocolEngineI;
    private EidasIdPImpl eidasIdPImpl;
    private IdPSigner idPSigner;
    private IdPMetadataFetcher idPMetadataFetcher;
    private MetaDataConfigurationI metaDataConfigurationI;

    public IdPEncryptionTest() throws Exception {
        try( InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("testkeystore.jks")) {
            idPKeyStore = new IdPKeyStore(inputStream,"changeit");
        }
        IdPConfig idPConfig = new IdPConfig();
        idPSigner = idPConfig.getIdpSigner(idPKeyStore);
        idPMetadataFetcher = idPConfig.getMetaDataFetcher();

        protocolEngineI = idPConfig.getProtocolEngine(idPKeyStore, idPSigner,
                idPConfig.getEidasProtocolProcessor(idPSigner, idPMetadataFetcher));

        metaDataConfigurationI = new MetaDataConfigurationImpl();
        eidasIdPImpl = new EidasIdPImpl(protocolEngineI, idPMetadataFetcher, metaDataConfigurationI);
    }

    protected static RSAPrivateKey generatePrivateKeyFromDER(byte[] keyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

        KeyFactory factory = KeyFactory.getInstance("RSA");

        return (RSAPrivateKey)factory.generatePrivate(spec);
    }

    @Test
    public void testRequestParse() throws Exception {
        String request = "PHNhbWwycDpBdXRoblJlcXVlc3QgeG1sbnM6c2FtbDJwPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6cHJvdG9j"+
                "b2wiIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIiB4bWxuczplaWRhcz0iaHR0cDovL2VpZGFzL"+
                "mV1cm9wYS5ldS9zYW1sLWV4dGVuc2lvbnMiIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW"+
                "9uIiBDb25zZW50PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6Y29uc2VudDp1bnNwZWNpZmllZCIgRGVzdGluYXRpb249Imh"+
                "0dHA6Ly9sb2NhbGhvc3Q6ODA4MC9FaWRhc05vZGUvU2VydmljZVByb3ZpZGVyIiBGb3JjZUF1dGhuPSJ0cnVlIiBJRD0iX1otOWg4"+
                "Q19nR3dlSEtsWVBvZFR2dmRuVmVheWRsM2lzY0pQSlROeTE3NFhrSlBKLWVUcW0xNnBXTXBqRzZzayIgSXNQYXNzaXZlPSJmYWxzZ"+
                "SIgSXNzdWVJbnN0YW50PSIyMDE3LTExLTIzVDE2OjE0OjU2LjgyMFoiIFByb3ZpZGVyTmFtZT0iREVNTy1TUC1DQSIgVmVyc2lvbj"+
                "0iMi4wIj48c2FtbDI6SXNzdWVyIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOm5hbWVpZC1mb3JtYXQ6ZW50aXR"+
                "5IiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+aHR0cDovL2xvY2FsaG9zdDo4MDgw" +
                "L1NQL21ldGFkYXRhPC9zYW1sMjpJc3N1ZXI+PGRzOlNpZ25hdHVyZSB4bWxuczpkcz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wO" +
                "S94bWxkc2lnIyI+PGRzOlNpZ25lZEluZm8+PGRzOkNhbm9uaWNhbGl6YXRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3Ln" +
                "czLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz48ZHM6U2lnbmF0dXJlTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5" +
                "vcmcvMjAwMS8wNC94bWxkc2lnLW1vcmUjcnNhLXNoYTUxMiIvPjxkczpSZWZlcmVuY2UgVVJJPSIjX1otOWg4Q19nR3dlSEtsWVBv" +
                "ZFR2dmRuVmVheWRsM2lzY0pQSlROeTE3NFhrSlBKLWVUcW0xNnBXTXBqRzZzayI+PGRzOlRyYW5zZm9ybXM+PGRzOlRyYW5zZm9yb" +
                "SBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNlbnZlbG9wZWQtc2lnbmF0dXJlIi8+PGRzOlRyYW" +
                "5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjwvZHM6VHJhbnNmb3Jtcz4" +
                "8ZHM6RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjc2hhNTEyIi8+PGRzOkRp" +
                "Z2VzdFZhbHVlPjRXaiszWnRSeEZNNmZ0c1BaMXZTQWtKb016MEcvL0pGVWU0MGtJVnRHWEl3Qmk5cXd3OFVJNitYd3BlMXJnbmtHd" +
                "HVkWkgrb3JzRWRUUFdCNFcreFdnPT08L2RzOkRpZ2VzdFZhbHVlPjwvZHM6UmVmZXJlbmNlPjwvZHM6U2lnbmVkSW5mbz48ZHM6U2" +
                "lnbmF0dXJlVmFsdWU+TURBU2JIK0VCcWxXZG5lYnN0WnJlMFNENnE0RitIQnl5WDdodk93TVFmMXJPTVcxRlFlRTlzZ1lKS3Nsc1d" +
                "hSFpqM21qd2pBNVJUYWtQbXdHNDNKeG55YkpBOG5rcnlwZWw2TFYzWWNTdUVra3ZrUzRPOTdKb2NreFZFM1hMek5pZ1k3YWJ1R09x" +
                "MmVyOGg0MEJPRzZCenhkRUR4TGhSMEptWm1OVTdGZTBwbzQ2OGs5TTNSK1R1TjFMZkNRNlJWZWNBcmFQQnVqczVmT2I4N0lDQVZkM" +
                "lhycExrR2w4NG9PaHRRM21KM0grOUJaQjVmc0ErU3l4UXhmQmduOUYrUUd1Zk0wVWUzWDlYb0dwSjZ6MWxkeURJRTVHZFlKWWlyM0" +
                "tZd2x3V1JKVzlRVDgxQWVEUWRhRytPRjRKV1plMWYvTjc0OGd6TWNEUEI0VmJrd3AvUGdhOUh6MFZSbGVzMy9CQlJMRmh4VFpuaXc" +
                "zODhRalVzUTFCWExKVFhkcEhmTU50d1laTDhtemxralFRVDNtNnhiS3RQTGVrQnd0cGRSQnUwMzVVSWo2V2l4ZHFFTk0vdUluWFBk" +
                "RFp3YVVNUlV3WDNqOEhLaU1aOWlPVCtHNk1jK1lreUhOWi9XUGM4Nk9BWnhtbDc0aW1jbnRGSHk1dGE1WGJEQnhHWWUrRC9JbUxXM" +
                "1l6cUJpaTdYYkZKUE9TS1NnQjJjbER4YTNsRjNqNngrNGNzWkhuWUxFVEhaZ0Frc2s4ZVN3NXEvR2ZPVXU5WUxnQy9LTisxV3lIL0" +
                "92b3IzQXRrbVFzaS9PaG1TdkNDaFdZdndLa2paTWMvT2h1Szc1bStCeDJDVGdTUktnWUtoRDJ0TnVvNitxUDViVXlqWXF3dnAxa2Z" +
                "TSWpFY1BlclFicEU4bmM9PC9kczpTaWduYXR1cmVWYWx1ZT48ZHM6S2V5SW5mbz48ZHM6WDUwOURhdGE+PGRzOlg1MDlDZXJ0aWZp" +
                "Y2F0ZT5NSUlGUVRDQ0F5a0NCRlRJK3pNd0RRWUpLb1pJaHZjTkFRRU5CUUF3WlRFTE1Ba0dBMVVFQmhNQ1EwRXhDekFKQmdOVkJBZ" +
                "01Ba1ZWCk1Rc3dDUVlEVlFRSERBSkZWVEVMTUFrR0ExVUVDZ3dDVTFBeERqQU1CZ05WQkFzTUJWTlVUMUpMTVI4d0hRWURWUVFERE" +
                "JaemNDMWoKWVMxa1pXMXZMV05sY25ScFptbGpZWFJsTUI0WERURTFNREV5T0RFMU1EY3pNVm9YRFRFMk1ERXlPREUxTURjek1Wb3d" +
                "aVEVMTUFrRwpBMVVFQmhNQ1EwRXhDekFKQmdOVkJBZ01Ba1ZWTVFzd0NRWURWUVFIREFKRlZURUxNQWtHQTFVRUNnd0NVMUF4RGpB" +
                "TUJnTlZCQXNNCkJWTlVUMUpMTVI4d0hRWURWUVFEREJaemNDMWpZUzFrWlcxdkxXTmxjblJwWm1sallYUmxNSUlDSWpBTkJna3Foa" +
                "2lHOXcwQkFRRUYKQUFPQ0FnOEFNSUlDQ2dLQ0FnRUFsQnFpUFBBZ1YvZk1XUjV4eVhhcFVHaVE5Ym1iSGhsOXdVc3RobjQvTmFRan" +
                "VvWTRxMzJpUld2eQo2TVdRYnR4cmZKNE5CbGloNG40WmFDRDBQaUZ4SzlBWlUyaVdUcXFGSmQ0Tm9sKzBUQjhZT1A4MWE4WE9MaXU" +
                "xRTBnd2pFZTcxT3JjCnBwdzZabkFMVDYzcERaYkdtU2ZqR3pkUzBUdXJVa0hOcUxLY21hblBKamFQUU1hakh3RmxDdGVpc3dNclN1" +
                "bnc0K3dUY0hnSWwwMjgKdW9VeDcyc2gvTzY5aHVQSGJmMTI5NUhPcStwZlU5ZWRzazVyL1Z4aTNianY2c1B1dEIzalpqTVlIZmRpb" +
                "3lKZTZGMGV4Z2dOK0xxTApLMVdpVDJrL1JBa29TU1FtQzFEUXFpM3ZieFZMNFdvY0dKWVJpbFBqaGFFZmhpRUt2MzdGWjZXVjQzVT" +
                "VrZDhWM3R0L1ljRm11RU1LCmZYS0tQUXdaVFpuMUdYa1NXSG5oVERPOXpCQThuSUlsam9xM1VlNUR1VWhUNjRHRUdxR0R2b0pvQVl" +
                "0eWtlY2orcnhmdk4yZFlqS2gKYklvTnBqRHJrZGxEcTR4d2Yvd1hSaFRJV3lEUkN6TGdVNHZ4Z1hKdEJLcms0TzVnSTFwclVqUHR4" +
                "NitycTdjc2IyRFNoRG4rSEZwZAo1cm1HSzRpUG45K3NzNlgzeXpsczFOQlFhTDRPMU1JOFRTRFRnWitMdVd6U3FsQVNPd0ZFbW9RV" +
                "0daY0hvNS9aeFhCcll1UTdzdzJ4CmJ0bWZ2SGJtdnhwTy85VTlLb0tOeWpyU3AwamNGZlJLZjNpTnZSVStpWUwwQXpnUDVkTEJRUF" +
                "dWeGpBRk9HandOa2p1MVZBdGovM0gKTXUxYUJ3YnlLbWdobFF3VzVEc0NBd0VBQVRBTkJna3Foa2lHOXcwQkFRMEZBQU9DQWdFQVJ" +
                "lNTBGMjBNSlBBcWN5TmlOMytYUWl6bgp5ZVpKMU1sSm5TckhxcTF5aUJQRnZENWZJS0xBQzA3a1ByU0M5Zm5OUW50Nko3OW1DQTlJ" +
                "Ukp3VkRZT3M0U0RScndUaWtWbWsxZVZkCjFtOGZ4dnNKOFlmemg3d3FiQzZNYWY3YldlTHFGRGVxOEVneWNPcnViUk92Ukl1RU5iU" +
                "VFtV0pmb2U4aUh4MjdYY3hKUGVoUG5pblkKZ0dmUGJPS0hVZC8wOG56STNnbkpuVU1Hdkc5aE5PUnFsb00yMHdhalRXM2ZDNnhlaV" +
                "JxZy8yMVM1MXRNVzhFVEluZWpOSUU2TlkrQgo5cjhFNERwUkxpZ3hjMFhqekFkQzRMODAvQWwrSW1WT3dHZHBldm9tV0oxRldhckl" +
                "OVnRTRXcyY0R6dm5RK21vSFBNMFlrOXdyYW4wCjZ5eDZ1UWx4by83TVorbUc3d3VsUlE1dkdXZHVjdytrdE03dFVvZGxlYlRJMDFp" +
                "RzA5Kzl0YWRFUXAvcnlpbE5rYncrd1QreFBWdUYKZkNSZmhxTHhuanZ1TjZGZUpGNzYvN3k3RDVSbUJHNXdCSG84cVdueUhWekhQU" +
                "0d2VHhaUHJMdXBHQjl2enhUYlB0NnBiRE52M1ZWQQpreTAvenBxdzF2czBQM3JtRWI1S3NGbzlkd0NBUUVMVkNpcURsZTl4a0djME" +
                "pyNmVDUzZXMnFNUTRRRjRlVUhWRDhxNXROcjBkaFpBCjNuc2pMbnl1dmNXRmlUZFpuTU9zb3pYZlJpTjNBS0lJa3hNTDN0N3BwNlZ" +
                "iS2c1MExnWkd2M0d2YmJSK0hKc1JDQUN4SG04OFBTeHUKcmZObzlaVElQejI1eUpBTkRjSHpXaFFYV3hKQzNnY1l2eVc4SE92U0xW" +
                "bEF6VUZ1Q2NFPTwvZHM6WDUwOUNlcnRpZmljYXRlPjwvZHM6WDUwOURhdGE+PC9kczpLZXlJbmZvPjwvZHM6U2lnbmF0dXJlPjxzY" +
                "W1sMnA6RXh0ZW5zaW9ucz48ZWlkYXM6U1BUeXBlPnB1YmxpYzwvZWlkYXM6U1BUeXBlPjxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dG" +
                "VzPjxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJELTIwMTItMTctRVVJZGVudGlmaWVyIiBOYW1lPSJodHR" +
                "wOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbGVnYWxwZXJzb24vRC0yMDEyLTE3LUVVSWRlbnRpZmllciIgTmFtZUZvcm1h" +
                "dD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9ImZhbHNlIi8+PGVpZ" +
                "GFzOlJlcXVlc3RlZEF0dHJpYnV0ZSBGcmllbmRseU5hbWU9IkVPUkkiIE5hbWU9Imh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvYXR0cm" +
                "lidXRlcy9sZWdhbHBlcnNvbi9FT1JJIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9" +
                "ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iTEVJIiBO" +
                "YW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbGVnYWxwZXJzb24vTEVJIiBOYW1lRm9ybWF0PSJ1cm46b2Fza" +
                "XM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iTGVnYWxBZGRpdGlvbmFsQXR0cmlidXRlIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbGVnYWxwZXJzb24vTGVnYWxBZGRpdGlvbmFsQXR0cmlidXRlIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iTGVnYWxOYW1lIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbGVnYWxwZXJzb24vTGVnYWxOYW1lIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0idHJ1ZSIvPjxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJMZWdhbEFkZHJlc3MiIE5hbWU9Imh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvYXR0cmlidXRlcy9sZWdhbHBlcnNvbi9MZWdhbFBlcnNvbkFkZHJlc3MiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJmYWxzZSIvPjxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJMZWdhbFBlcnNvbklkZW50aWZpZXIiIE5hbWU9Imh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvYXR0cmlidXRlcy9sZWdhbHBlcnNvbi9MZWdhbFBlcnNvbklkZW50aWZpZXIiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJ0cnVlIi8+PGVpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZSBGcmllbmRseU5hbWU9IlNFRUQiIE5hbWU9Imh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvYXR0cmlidXRlcy9sZWdhbHBlcnNvbi9TRUVEIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iU0lDIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbGVnYWxwZXJzb24vU0lDIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iVGF4UmVmZXJlbmNlIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbGVnYWxwZXJzb24vVGF4UmVmZXJlbmNlIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iVkFUUmVnaXN0cmF0aW9uIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbGVnYWxwZXJzb24vVkFUUmVnaXN0cmF0aW9uTnVtYmVyIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iQWRkaXRpb25hbEF0dHJpYnV0ZSIgTmFtZT0iaHR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL25hdHVyYWxwZXJzb24vQWRkaXRpb25hbEF0dHJpYnV0ZSIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9ImZhbHNlIi8+PGVpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZSBGcmllbmRseU5hbWU9IkJpcnRoTmFtZSIgTmFtZT0iaHR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL25hdHVyYWxwZXJzb24vQmlydGhOYW1lIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iQ3VycmVudEFkZHJlc3MiIE5hbWU9Imh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvYXR0cmlidXRlcy9uYXR1cmFscGVyc29uL0N1cnJlbnRBZGRyZXNzIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iRmFtaWx5TmFtZSIgTmFtZT0iaHR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL25hdHVyYWxwZXJzb24vQ3VycmVudEZhbWlseU5hbWUiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJ0cnVlIi8+PGVpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZSBGcmllbmRseU5hbWU9IkZpcnN0TmFtZSIgTmFtZT0iaHR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL25hdHVyYWxwZXJzb24vQ3VycmVudEdpdmVuTmFtZSIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9InRydWUiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iRGF0ZU9mQmlydGgiIE5hbWU9Imh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvYXR0cmlidXRlcy9uYXR1cmFscGVyc29uL0RhdGVPZkJpcnRoIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0idHJ1ZSIvPjxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJHZW5kZXIiIE5hbWU9Imh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvYXR0cmlidXRlcy9uYXR1cmFscGVyc29uL0dlbmRlciIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9ImZhbHNlIi8+PGVpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZSBGcmllbmRseU5hbWU9IlBlcnNvbklkZW50aWZpZXIiIE5hbWU9Imh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvYXR0cmlidXRlcy9uYXR1cmFscGVyc29uL1BlcnNvbklkZW50aWZpZXIiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJ0cnVlIi8+PGVpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZSBGcmllbmRseU5hbWU9IlBsYWNlT2ZCaXJ0aCIgTmFtZT0iaHR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL25hdHVyYWxwZXJzb24vUGxhY2VPZkJpcnRoIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48L2VpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZXM+PC9zYW1sMnA6RXh0ZW5zaW9ucz48c2FtbDJwOk5hbWVJRFBvbGljeSBBbGxvd0NyZWF0ZT0idHJ1ZSIgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoxLjE6bmFtZWlkLWZvcm1hdDp1bnNwZWNpZmllZCIgeG1sbnM6c2FtbDJwPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6cHJvdG9jb2wiLz48c2FtbDJwOlJlcXVlc3RlZEF1dGhuQ29udGV4dCBDb21wYXJpc29uPSJtaW5pbXVtIiB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCI+PHNhbWwyOkF1dGhuQ29udGV4dENsYXNzUmVmIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj5odHRwOi8vZWlkYXMuZXVyb3BhLmV1L0xvQS9sb3c8L3NhbWwyOkF1dGhuQ29udGV4dENsYXNzUmVmPjwvc2FtbDJwOlJlcXVlc3RlZEF1dGhuQ29udGV4dD48L3NhbWwycDpBdXRoblJlcXVlc3Q+";

        IAuthenticationRequest authenticationRequest = eidasIdPImpl.parseRequest(request);
        assertNotNull(authenticationRequest);

        String consumerUrl = MetadataUtil.getAssertionConsumerUrlFromMetadata(idPMetadataFetcher,idPSigner,authenticationRequest);
        assertNotNull(consumerUrl);
    }
}
