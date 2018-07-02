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
        String request = "PHNhbWwycDpBdXRoblJlcXVlc3QgeG1sbnM6c2FtbDJwPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6cHJvdG9jb" +
                "2wiIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIiB4bWxuczplaWRhcz0iaHR0cDovL2VpZGFzLmV" +
                "1cm9wYS5ldS9zYW1sLWV4dGVuc2lvbnMiIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uI" +
                "iBDb25zZW50PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6Y29uc2VudDp1bnNwZWNpZmllZCIgRGVzdGluYXRpb249Imh0dHA" +
                "6Ly9laWRhcy1jb25uZWN0b3ItdGMxYS5hcmVuZHVzLmtpdDo4MDgwL0VpZGFzTm9kZS9TZXJ2aWNlUHJvdmlkZXIiIEZvcmNlQXV0a" +
                "G49InRydWUiIElEPSJfRy5Ub2JkNTZlbWNodlFTUEJraTJfRGE0ZHF3bWNySEQubm5vVFhZZDB4ai5CVm9OZmZoTEplX1JkaXBlUWx" +
                "oIiBJc1Bhc3NpdmU9ImZhbHNlIiBJc3N1ZUluc3RhbnQ9IjIwMTgtMDYtMjhUMTA6NDk6MTAuODg1WiIgUHJvdmlkZXJOYW1lPSJER" +
                "U1PLVNQLUVFIiBWZXJzaW9uPSIyLjAiPjxzYW1sMjpJc3N1ZXIgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6bmF" +
                "tZWlkLWZvcm1hdDplbnRpdHkiPmh0dHA6Ly9laWRhcy10b21jYXQtMWEuYXJlbmR1cy5raXQ6ODA4MC9TUC9tZXRhZGF0YTwvc2Ftb" +
                "DI6SXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPjxkczpTaWd" +
                "uZWRJbmZvPjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZ" +
                "XhjLWMxNG4jIi8+PGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZHNpZy1" +
                "tb3JlI3JzYS1zaGE1MTIiLz48ZHM6UmVmZXJlbmNlIFVSST0iI19HLlRvYmQ1NmVtY2h2UVNQQmtpMl9EYTRkcXdtY3JIRC5ubm9UW" +
                "FlkMHhqLkJWb05mZmhMSmVfUmRpcGVRbGgiPjxkczpUcmFuc2Zvcm1zPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d" +
                "3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwO" +
                "i8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz48L2RzOlRyYW5zZm9ybXM+PGRzOkRpZ2VzdE1ldGhvZCBBbGdvcml" +
                "0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jI3NoYTUxMiIvPjxkczpEaWdlc3RWYWx1ZT5pa2JWNE9Fd1RzOUlyY" +
                "WIxWVNMa2p1UWc4dUNNUVBJQzB2Zk40YzJxRXlkYi8rYW1uUkFCSzlxeVcwV0JGZGVwM1loK3RuZTVnaHIvOVBRN0RIQTRoZz09PC9" +
                "kczpEaWdlc3RWYWx1ZT48L2RzOlJlZmVyZW5jZT48L2RzOlNpZ25lZEluZm8+PGRzOlNpZ25hdHVyZVZhbHVlPkhZSEI5ZGZTd3FNd" +
                "kJiK29sTUVzU0ZVQkVKLzR1RFNqbGNRS1hTOTZMSUZ1aDNiaHhrQ094NUxIZDVkbTBDVkNMdnExaDBTaHZhWk5OOXBxbTViZzVxS2E" +
                "xOGM4VFVlQWorYkNzZm1ZajczeDYyTDd4bERpSDIreWpPamp3ZG1VdWs3S1J2eUZRcExZcEpBcjZCcS9JU0FwNkREdnVWSzZZZVJqa" +
                "XVWVjQyKytEdWZ1VmE5ZDIwTzdMRXdCNXpQS0hzV0ZnR3Erc2VzVGErKzhmYzRxd3hQaUQwcXNJWFV5Y0lUMXU0ckhLaFljMDFVQTU" +
                "2WTluSXZlb1owVGxnT05Cc1hTRElhRUdCdUJtSmorYTBKcy80YjI4NHZvNENyS0E3Z0w3eTE0MThFTENOZWczRFFFRFpyaVlVSjFWa" +
                "HJuVTdZa3lieWJaSWVsc1o2NytOVy9sZz09PC9kczpTaWduYXR1cmVWYWx1ZT48ZHM6S2V5SW5mbz48ZHM6WDUwOURhdGE+PGRzOlg" +
                "1MDlDZXJ0aWZpY2F0ZT5NSUlEYVRDQ0FsR2dBd0lCQWdJRUg1UDg2akFOQmdrcWhraUc5dzBCQVFzRkFEQmxNUXN3Q1FZRFZRUUdFd" +
                "0pGUlRFTE1Ba0dBMVVFCkNCTUNSVlV4Q3pBSkJnTlZCQWNUQWtWVk1Rc3dDUVlEVlFRS0V3SlRVREVPTUF3R0ExVUVDeE1GVTFSUFV" +
                "rc3hIekFkQmdOVkJBTVQKRm5Od0xXVmxMV1JsYlc4dFkyVnlkR2xtYVdOaGRHVXdIaGNOTVRjd05URTVNVEkxTURBM1doY05NVGt3T" +
                "lRBNU1USTFNREEzV2pCbApNUXN3Q1FZRFZRUUdFd0pGUlRFTE1Ba0dBMVVFQ0JNQ1JWVXhDekFKQmdOVkJBY1RBa1ZWTVFzd0NRWUR" +
                "WUVFLRXdKVFVERU9NQXdHCkExVUVDeE1GVTFSUFVrc3hIekFkQmdOVkJBTVRGbk53TFdWbExXUmxiVzh0WTJWeWRHbG1hV05oZEdVd" +
                "2dnRWlNQTBHQ1NxR1NJYjMKRFFFQkFRVUFBNElCRHdBd2dnRUtBb0lCQVFDcVpDN0NKaTNXVkN6c1NoM21WOUZOb1ZZZ2VpT0lSNjV" +
                "FQU9hcHl5dDdYZ3FjaG4xcwpTcHBxM2hzekNiamdROG1LMWh1RGt3OGlMNXFtWGc0cmQ2VFVWNG54UHVFVmJYMzQ4UTJQN0pINlVtS" +
                "GNlZFdFbFl2emJJMll3Mzg4CnYvZE93enA3anphOERhS0J0QUpsazhoWTFyaVBiZTRDZmlPcjVhVFlicHlHMENzYm5velJYcGlqNTY" +
                "yU05Wa2J2V3o0RWRXLzNDNVcKaTczdkhOUnp2SW9NZ3dGMjhZekNwZjZERk1rNVFwZWpTYytGNnpzWWVLMXVNcU50SlZ4R3liR2hsc" +
                "TYxQkJtR014RkJtdDBMZExXdApVT256anFnQjUvWThjTlNoRWsreXhUL1FCTUo4QmJPOHZnTzJJbmhGVXlFQmxieHpxR3N2ZFA2Qlp" +
                "KMzdsekJmQWdNQkFBR2pJVEFmCk1CMEdBMVVkRGdRV0JCU0YrTm1FZ2pEblZTTnVjbjljRldVMjh4SEc4REFOQmdrcWhraUc5dzBCQ" +
                "VFzRkFBT0NBUUVBUC9jSlQrdGkKSFRKN2FHRVNmU291S1VjY25zUzg5VktZNHp1MUNqNkl1R0JPdHNpN09SeDhpQmlkM21GZVgzYlM" +
                "5WGNuSzBrVjN2YklFZlhyMlU5TApZSHRBZUVSTndNazExMXkwc1UycG53SHBXYWU1WVg3Y0NCamJFZDcyQ1Y3QlE1Y1BFeFVFZE9SR" +
                "3JwSHJFRTQ0NW8yTEM3TmlmMFF4CmtPLzJCRk1sS0pXc3IySHljY1lYV1NGZHlpZTNhcjFIemtNR2JlYnlLN2NtUlZUSHFvaE53UHR" +
                "WbVMrYkxjeWpZNU9pTC9OVkFyR1IKVlA2RFNlcDNoKy9HNkdubXJwZVF4c0x3b2xoQVNOTFFieWxpZkE4djZFM3RvSHU5ZGl0eDlxe" +
                "W5GRm45Q2VEVDNnMUxLaHdRa0I2LwpHQlZ0S3ZGRUFDNCtPNEFQdnRuTXZqS2hBQnBPT2c9PTwvZHM6WDUwOUNlcnRpZmljYXRlPjw" +
                "vZHM6WDUwOURhdGE+PC9kczpLZXlJbmZvPjwvZHM6U2lnbmF0dXJlPjxzYW1sMnA6RXh0ZW5zaW9ucz48ZWlkYXM6U1BUeXBlPnB1Y" +
                "mxpYzwvZWlkYXM6U1BUeXBlPjxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGVzPjxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5" +
                "kbHlOYW1lPSJBZGRpdGlvbmFsQXR0cmlidXRlIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbmF0dXJhb" +
                "HBlcnNvbi9BZGRpdGlvbmFsQXR0cmlidXRlIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWU" +
                "tZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iQmlyd" +
                "GhOYW1lIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbmF0dXJhbHBlcnNvbi9CaXJ0aE5hbWUiIE5hbWV" +
                "Gb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJmYWxzZSIvP" +
                "jxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJDdXJyZW50QWRkcmVzcyIgTmFtZT0iaHR0cDovL2VpZGFzLmV" +
                "1cm9wYS5ldS9hdHRyaWJ1dGVzL25hdHVyYWxwZXJzb24vQ3VycmVudEFkZHJlc3MiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lc" +
                "zp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJmYWxzZSIvPjxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ" +
                "1dGUgRnJpZW5kbHlOYW1lPSJGYW1pbHlOYW1lIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbmF0dXJhb" +
                "HBlcnNvbi9DdXJyZW50RmFtaWx5TmFtZSIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZ" +
                "vcm1hdDp1cmkiIGlzUmVxdWlyZWQ9InRydWUiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iRmlyc3ROY" +
                "W1lIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbmF0dXJhbHBlcnNvbi9DdXJyZW50R2l2ZW5OYW1lIiB" +
                "OYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0idHJ1Z" +
                "SIvPjxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJEYXRlT2ZCaXJ0aCIgTmFtZT0iaHR0cDovL2VpZGFzLmV" +
                "1cm9wYS5ldS9hdHRyaWJ1dGVzL25hdHVyYWxwZXJzb24vRGF0ZU9mQmlydGgiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0Y" +
                "zpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJ0cnVlIi8+PGVpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZSB" +
                "GcmllbmRseU5hbWU9IkdlbmRlciIgTmFtZT0iaHR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL25hdHVyYWxwZXJzb24vR" +
                "2VuZGVyIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJ" +
                "lZD0iZmFsc2UiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iUGVyc29uSWRlbnRpZmllciIgTmFtZT0ia" +
                "HR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL25hdHVyYWxwZXJzb24vUGVyc29uSWRlbnRpZmllciIgTmFtZUZvcm1hdD0" +
                "idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9InRydWUiLz48ZWlkYXM6U" +
                "mVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iUGxhY2VPZkJpcnRoIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F" +
                "0dHJpYnV0ZXMvbmF0dXJhbHBlcnNvbi9QbGFjZU9mQmlydGgiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuM" +
                "DphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJmYWxzZSIvPjwvZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlcz48L3NhbWw" +
                "ycDpFeHRlbnNpb25zPjxzYW1sMnA6TmFtZUlEUG9saWN5IEFsbG93Q3JlYXRlPSJ0cnVlIiBGb3JtYXQ9InVybjpvYXNpczpuYW1lc" +
                "zp0YzpTQU1MOjEuMTpuYW1laWQtZm9ybWF0OnVuc3BlY2lmaWVkIi8+PHNhbWwycDpSZXF1ZXN0ZWRBdXRobkNvbnRleHQgQ29tcGF" +
                "yaXNvbj0ibWluaW11bSI+PHNhbWwyOkF1dGhuQ29udGV4dENsYXNzUmVmPmh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvTG9BL2xvdzwvc" +
                "2FtbDI6QXV0aG5Db250ZXh0Q2xhc3NSZWY+PC9zYW1sMnA6UmVxdWVzdGVkQXV0aG5Db250ZXh0Pjwvc2FtbDJwOkF1dGhuUmVxdWVzdD4=";

        IAuthenticationRequest authenticationRequest = eidasIdPImpl.parseRequest(request);
        assertNotNull(authenticationRequest);

        String consumerUrl = MetadataUtil.getAssertionConsumerUrlFromMetadata(idPMetadataFetcher,idPSigner,authenticationRequest);
        assertNotNull(consumerUrl);
    }
}
