/*
 * MIT License
 *
 * Copyright (c) 2018 Estonian Information System Authority
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package ee.ria.IdP.crypto;

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.core.impl.AbstractProtocolSigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * Eidas protocol signer implementation for id provider
 */
public class IdPSigner extends AbstractProtocolSigner {
    private static final Logger LOG = LoggerFactory.getLogger(IdPSigner.class);

    public static IdPSigner getInstance(IdPKeyStore idPKeyStore) throws SamlEngineConfigurationException {
        // todo: set these two properties to true?
        return new IdPSigner(false, false, true,
                idPKeyStore.getSignatureKeyAndCertificate(),
                idPKeyStore.getTrustedCertificates(),
                "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512",
                "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256;" +
                        "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384;" +
                        "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512;" +
                        "http://www.w3.org/2001/04/xmldsig-more#rsa-ripemd160;" +
                        "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256;" +
                        "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384;" +
                        "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512;" +
                        "http://www.w3.org/2007/05/xmldsig-more#sha256-rsa-MGF1",
                idPKeyStore.getMetadataSignatureKeyAndCertificate());
    }
    private IdPSigner(boolean checkedValidityPeriod, boolean disallowedSelfSignedCertificate,
                        boolean responseSignAssertions, KeyStore.PrivateKeyEntry signatureKeyAndCertificate,
                        ImmutableSet<X509Certificate> trustedCertificates, String signatureAlgorithmVal,
                        String signatureAlgorithmWhiteListStr,
                        KeyStore.PrivateKeyEntry metadataSigningKeyAndCertificate) throws SamlEngineConfigurationException {
        super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseSignAssertions,
                signatureKeyAndCertificate, trustedCertificates, signatureAlgorithmVal, signatureAlgorithmWhiteListStr,
                metadataSigningKeyAndCertificate);
    }
}
