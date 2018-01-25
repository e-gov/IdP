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
import eu.eidas.auth.engine.core.impl.AbstractSamlEngineEncryption;
import eu.eidas.auth.engine.core.impl.CertificateValidator;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * Saml cryptographic methods implementation for id provider
 */
public class IdPEncryption extends AbstractSamlEngineEncryption {
    private static final Logger LOG = LoggerFactory.getLogger(IdPEncryption.class);

    private IdPKeyStore keyStore;

    /* values from "EncryptionModule_IdP.xml" */
    public static IdPEncryption getInstance(IdPKeyStore idPKeyStore) throws SamlEngineConfigurationException {
        // todo: need to change these two to true
        IdPEncryption result = new IdPEncryption(false, false, true,
                idPKeyStore.getDecryptionKeysAndCertificates(),
                idPKeyStore.getEncryptionCertificates(),
                "http://www.w3.org/2009/xmlenc11#aes256-gcm",
                "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p",
                null, // default is bouncycastle
                "http://www.w3.org/2009/xmlenc11#aes128-gcm;" +
                        "http://www.w3.org/2009/xmlenc11#aes256-gcm;http://www.w3.org/2009/xmlenc11#aes192-gcm");
        result.keyStore = idPKeyStore;
        return result;
    }

    private IdPEncryption(boolean checkedValidityPeriod,
                          boolean disallowedSelfSignedCertificate,
                          boolean responseEncryptionMandatory,
                          ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates,
                          ImmutableSet<X509Certificate> encryptionCertificates,
                          String dataEncryptionAlgorithm,
                          String keyEncryptionAlgorithm,
                          String jcaProviderName,
                          String encryptionAlgorithmWhiteList)
            throws SamlEngineConfigurationException {
        super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
                decryptionKeyAndCertificates, encryptionCertificates, dataEncryptionAlgorithm, keyEncryptionAlgorithm,
                jcaProviderName, encryptionAlgorithmWhiteList);
    }

    @Override
    public X509Certificate getEncryptionCertificate(String destinationCountryCode) throws EIDASSAMLEngineException {
        X509Certificate certificate = (X509Certificate) keyStore.getEncryptionCertificate(destinationCountryCode);
        if (certificate != null) {
            if (isDisallowedSelfSignedCertificate()) {
                CertificateValidator.checkCertificateIssuer(certificate);
            }
            if (isCheckedValidityPeriod()) {
                CertificateValidator.checkCertificateValidityPeriod(certificate);
            }
            return certificate;
        }
        return null;
    }

    @Override
    // is this method needed?
    public boolean isEncryptionEnabled(String s) {
        return true;
    }
}
