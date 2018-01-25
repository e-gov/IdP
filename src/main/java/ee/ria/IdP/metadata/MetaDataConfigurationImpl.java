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

package ee.ria.IdP.metadata;

import eu.eidas.auth.engine.metadata.ContactData;
import eu.eidas.auth.engine.metadata.OrganizationData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Class holding metadata configuration for id provider.
 * Values are initialized via context configuration parameters (see IdP.xml)
 */
@Component
public class MetaDataConfigurationImpl implements MetaDataConfigurationI {
    private static final String SIGNATURE_ALGORITHM_WHITELIST=
        "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256;http://www.w3.org/2001/04/xmldsig-more#rsa-sha384;"+
        "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512;http://www.w3.org/2001/04/xmldsig-more#rsa-ripemd160;"+
        "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256;http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384;"+
        "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512;http://www.w3.org/2007/05/xmldsig-more#sha256-rsa-MGF1";
    private static final String ENCRYPTION_ALGORITHM_WHITELIST=
        "http://www.w3.org/2009/xmlenc11#aes128-gcm;http://www.w3.org/2009/xmlenc11#aes256-gcm;"+
        "http://www.w3.org/2009/xmlenc11#aes192-gcm";


    @Value("${BaseUrl}")
    private String baseUrl;

    @Value("${TechnicalContactGivenName}")
    private String technicalContactGivenName;
    @Value("${TechnicalContactSurname}")
    private String technicalContactSurname;
    @Value("${TechnicalContactPhone}")
    private String technicalContactPhone;
    @Value("${TechnicalContactEmail}")
    private String technicalContactEmail;
    @Value("${TechnicalContactCompanyName}")
    private String technicalContactCompanyName;

    @Value("${SupportContactGivenName}")
    private String supportContactGivenName;
    @Value("${SupportContactSurname}")
    private String supportContactSurname;
    @Value("${SupportContactPhone}")
    private String supportContactPhone;
    @Value("${SupportContactEmail}")
    private String supportContactEmail;
    @Value("${SupportContactCompanyName}")
    private String supportContactCompanyName;

    @Value("${OrganizationName}")
    private String organizationName;
    @Value("${OrganizationDisplayName}")
    private String organizationDisplayName;
    @Value("${OrganizationUrl}")
    private String organizationUrl;

    @Override
    public String getLocalCountry() {
        return "ET";
    }

    @Override
    public String getMetaDataUrl() {
        return baseUrl + "/metadata";
    }

    @Override
    public String getPostBindingUrl() {
        return baseUrl + "/auth";
    }

    @Override
    public ContactData getTechnicalContact() {
        return ContactData.builder()
                .givenName(technicalContactGivenName)
                .surName(technicalContactSurname)
                .phone(technicalContactPhone)
                .email(technicalContactEmail)
                .company(technicalContactCompanyName)
                .build();
    }

    @Override
    public ContactData getSupportContact() {
        return ContactData.builder()
                .givenName(supportContactGivenName)
                .surName(supportContactSurname)
                .phone(supportContactPhone)
                .email(supportContactEmail)
                .company(supportContactCompanyName)
                .build();        
    }

    @Override
    public OrganizationData getOrganization() {
        return OrganizationData.builder()
                .name(organizationName)
                .displayName(organizationDisplayName)
                .url(organizationUrl)
                .build();
    }

    @Override
    public String getSigningMethods() {
        return SIGNATURE_ALGORITHM_WHITELIST;
    }

    @Override
    public String getDigestMethods() {
        return SIGNATURE_ALGORITHM_WHITELIST;
    }

    @Override
    public String getEncryptionAlgorithms() {
        return ENCRYPTION_ALGORITHM_WHITELIST;
    }
}
