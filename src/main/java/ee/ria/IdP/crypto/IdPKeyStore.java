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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.security.auth.DestroyFailedException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

/**
 * class holding the cryptographic keys and certificates
 */
@Service
public class IdPKeyStore {
    private static final Logger LOG = LoggerFactory.getLogger(IdPKeyStore.class);

    private static final String COUNTRY_CERTIFICATE_ALIAS_PREFIX = "eidas_encrypt_";
    private static final String SIGNATURE_SERTIFICATE_NAME = "eidas_signature";
    private static final String METADATA_SIGNATURE_SERTIFICATE_NAME = "metadata_signature";

    private KeyStore keyStore;
    private ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeysAndCertificates;
    private ImmutableSet<X509Certificate> encryptionCertificates;

    private KeyStore.PrivateKeyEntry signatureKeyAndCertificate;
    private KeyStore.PrivateKeyEntry metadataSignatureKeyAndCertificate;

    private ImmutableSet<X509Certificate> trustedCertificates;

    /**
     *
     * @param keyStoreFileName name of keystore file, must be in jks format
     * @param keyStorePassword password for keystore file and individual entries (must have the same password)
     * @throws SamlEngineConfigurationException
     * @throws IOException
     */
    @Autowired
    public IdPKeyStore(@Value("${KeystoreFile}") String keyStoreFileName,
              @Value("${KeystorePassword}") String keyStorePassword) throws SamlEngineConfigurationException, IOException {
        try(InputStream keyStoreStream = new FileInputStream(keyStoreFileName)) {
            LOG.debug("Loading keystore file \"" + keyStoreFileName + "\"");
            loadKeyStore(keyStoreStream, keyStorePassword);
            LOG.debug("Loaded keystore file \"" + keyStoreFileName + "\"");
            loadKeyStoreContent(keyStorePassword);
        }
    }

    public IdPKeyStore(InputStream keyStoreStream, String keyStorePassword) throws SamlEngineConfigurationException {
        loadKeyStore(keyStoreStream, keyStorePassword);
        loadKeyStoreContent(keyStorePassword);
    }

    private void loadKeyStore(InputStream keyStoreStream, String keyStorePassword) throws SamlEngineConfigurationException {
        try {
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keyStoreStream, keyStorePassword.toCharArray());
        } catch (Exception e) {
            LOG.error("Unable to load keyStore: " + e, e);
            throw new SamlEngineConfigurationException("203016", "Invalid keystore", e);
            // samlengine.invalid.keystore.code=203016
        }
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public ImmutableSet<KeyStore.PrivateKeyEntry> getDecryptionKeysAndCertificates() {
        return decryptionKeysAndCertificates;
    }

    public ImmutableSet<X509Certificate> getEncryptionCertificates() {
        return encryptionCertificates;
    }

    public KeyStore.PrivateKeyEntry getSignatureKeyAndCertificate() {
        return signatureKeyAndCertificate;
    }
    public KeyStore.PrivateKeyEntry getMetadataSignatureKeyAndCertificate() {
        return metadataSignatureKeyAndCertificate;
    }

    public ImmutableSet<X509Certificate> getTrustedCertificates() {
        // lets use the same certs for now
        return encryptionCertificates;
        //return trustedCertificates;
    }

    public X509Certificate getEncryptionCertificate(String destinationCountryCode)  {
        X509Certificate certificate = null;
        try {
            certificate = (X509Certificate) keyStore.getCertificate(COUNTRY_CERTIFICATE_ALIAS_PREFIX + destinationCountryCode);
        } catch (KeyStoreException e) {
            LOG.error("Certificate with alias {} not found", COUNTRY_CERTIFICATE_ALIAS_PREFIX + destinationCountryCode);
        }
        if (certificate != null) {
            if (!checkCertificateIssuer(certificate)) {
                return null;
            }
            if(!checkCertificateValidityPeriod(certificate)) {
                return null;
            }
            return certificate;
        }
        return null;
    }

    public static boolean checkCertificateIssuer(X509Certificate certificate) {
        return certificate.getSubjectX500Principal().equals(certificate.getIssuerX500Principal());
    }

    public static boolean checkCertificateValidityPeriod(X509Certificate certificate) {
        Date notBefore = certificate.getNotBefore();
        Date notAfter = certificate.getNotAfter();
        Date currentDate = Calendar.getInstance().getTime();
        if (currentDate.before(notBefore) || currentDate.after(notAfter)) {
            LOG.error("ERROR : The certificate with reference '{}' failed check (out of date) [notBefore={}, notAfter={}]", new Object[]{certificate.getIssuerDN(), notBefore, notAfter});
            return false;
        }
        return true;
    }

    private void loadKeyStoreContent(String keyStorePassword) throws SamlEngineConfigurationException {
        char [] password = keyStorePassword.toCharArray();
        try {
            ImmutableSet.Builder<KeyStore.PrivateKeyEntry> privateKeys = ImmutableSet.builder();
            ImmutableSet.Builder<X509Certificate> certificates = ImmutableSet.builder();
            // if the keyStore contains other keys with different passwords, ignore them:
            UnrecoverableEntryException wrongPasswordException = null;
            for (Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements(); ) {
                String alias = aliases.nextElement();
                if (keyStore.isKeyEntry(alias)) {
                    KeyStore.PrivateKeyEntry privateKeyEntry = null;
                    try {
                        privateKeyEntry = getPrivateKeyEntry(keyStore, alias, password);
                    } catch (UnrecoverableEntryException wrongPassword) {
                        wrongPasswordException = wrongPassword;
                    }
                    if (null != privateKeyEntry) {
                        if(SIGNATURE_SERTIFICATE_NAME.equals(alias)) {
                            signatureKeyAndCertificate = privateKeyEntry;
                        }
                        else if(METADATA_SIGNATURE_SERTIFICATE_NAME.equals(alias)) {
                            metadataSignatureKeyAndCertificate = privateKeyEntry;
                        }
                        else {
                            privateKeys.add(privateKeyEntry);
                            certificates.add((X509Certificate) privateKeyEntry.getCertificate());
                            for (final Certificate certificate : privateKeyEntry.getCertificateChain()) {
                                certificates.add((X509Certificate) certificate);
                            }
                        }
                    }
                } else {
                    X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
                    if (null != certificate) {
                        certificates.add(certificate);
                    }
                    Certificate[] certificateChain = keyStore.getCertificateChain(alias);
                    if (null != certificateChain) {
                        for (final Certificate cert : certificateChain) {
                            certificates.add((X509Certificate) cert);
                        }
                    }
                }
            }
            decryptionKeysAndCertificates = privateKeys.build();
            if (decryptionKeysAndCertificates.isEmpty() && null != wrongPasswordException) {
                // If there is only keys with different passwords, the given password is probably incorrect:
                throw wrongPasswordException;
            }
            encryptionCertificates = certificates.build();
        } catch (Exception e) {
            throw new SamlEngineConfigurationException("203016", "Invalid keystore", e);
            // samlengine.invalid.keystore.code=203016
        }
    }

    public static ImmutableSet<KeyStore.PrivateKeyEntry> getPrivateKeyEntries(KeyStore keyStore, char[] password)
            throws SamlEngineConfigurationException {
        try {
            ImmutableSet.Builder<KeyStore.PrivateKeyEntry> keys = ImmutableSet.builder();
            // if the keyStore contains other keys with different passwords, ignore them:
            UnrecoverableEntryException wrongPasswordException = null;
            for (Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements(); ) {
                String alias = aliases.nextElement();
                KeyStore.PrivateKeyEntry privateKeyEntry = null;
                try {
                    privateKeyEntry = getPrivateKeyEntry(keyStore, alias, password);
                } catch (UnrecoverableEntryException wrongPassword) {
                    wrongPasswordException = wrongPassword;
                }
                if (null != privateKeyEntry) {
                    keys.add(privateKeyEntry);
                }
            }
            ImmutableSet<KeyStore.PrivateKeyEntry> entries = keys.build();
            if (entries.isEmpty() && null != wrongPasswordException) {
                // If there is only keys with different passwords, the given password is probably incorrect:
                throw wrongPasswordException;
            }
            return entries;
        } catch (SamlEngineConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new SamlEngineConfigurationException("203016", "Invalid keystore", e);
            // samlengine.invalid.keystore.code=203016
        }
    }

    public static KeyStore.PrivateKeyEntry getPrivateKeyEntry(KeyStore keyStore, String alias, char[] password)
            throws SamlEngineConfigurationException, UnrecoverableEntryException {
        try {
            if (keyStore.isKeyEntry(alias)) {
                return decryptPrivateKey(keyStore, alias, password);
            }
        } catch (UnrecoverableEntryException wrongPassword) {
            // The password does not match this alias
            throw wrongPassword;
        } catch (Exception e) {
            throw new SamlEngineConfigurationException("203016", "Invalid keystore", e);
            // samlengine.invalid.keystore.code=203016
        }
        return null;
    }

    private static KeyStore.PrivateKeyEntry decryptPrivateKey(KeyStore keyStore, String alias, char[] password)
            throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, DestroyFailedException {
        KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(password);
        try {
            KeyStore.Entry entry = keyStore.getEntry(alias, passwordProtection);
            // the entry can also be a symmetric key (without a certificate)
            if (entry instanceof KeyStore.PrivateKeyEntry) {
                return (KeyStore.PrivateKeyEntry) entry;
            }
        } finally {
            passwordProtection.destroy();
        }
        return null;
    }

    /*
    public static KeyStore.PrivateKeyEntry getPrivateKeyEntry(KeyStore keyStore,
                                                              String serialNumber,
                                                              String issuer,
                                                              char[] password) throws SamlEngineConfigurationException {
        try {
            for (Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements(); ) {
                String alias = aliases.nextElement();
                if (keyStore.isKeyEntry(alias)) {
                    X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
                    if (null == certificate || !CertificateUtil.matchesCertificate(serialNumber, issuer, certificate)) {
                        continue;
                    }
                    KeyStore.PrivateKeyEntry entry = decryptPrivateKey(keyStore, alias, password);
                    if (null != entry) {
                        return entry;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            throw new SamlEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorCode(),
                    EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorMessage(), e);
        }
    } */

}
