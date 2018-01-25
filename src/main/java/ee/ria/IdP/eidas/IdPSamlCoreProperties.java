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

package ee.ria.IdP.eidas;

import eu.eidas.auth.engine.core.SamlEngineCoreProperties;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.RequestAbstractType;

import java.util.Set;

/* values from SamlEngine_IdP.xml */
public class IdPSamlCoreProperties implements SamlEngineCoreProperties {
    @Override
    public String getConsentAuthnRequest() {
        return RequestAbstractType.UNSPECIFIED_CONSENT;
    }

    @Override
    public String getConsentAuthnResp() {
        return getConsentAuthnResponse();
    }

    @Override
    public String getConsentAuthnResponse() {
        return RequestAbstractType.OBTAINED_CONSENT;
    }

    // what is this and how is this used?
    @Override
    public String getFormatEntity() {
        return NameIDType.ENTITY;
    }

    @Override
    public String getProperty(String s) {
        // wtf method and interface is this?
        // will return null as idp does not use this method
        return null;
    }

    @Override
    public String getProtocolBinding() {
        return "HTTP-POST";
    }

    // how are these urls used and where to get values? maybe just return null?
    @Override
    public String getRequester() {
        return "http://S-PEPS.gov.xx";
    }

    @Override
    public String getResponder() {
        return "http://C-PEPS.gov.xx";
    }

    @Override
    public Set<String> getSupportedMessageFormatNames() {
        // what to return here?
        return null;
    }

    @Override
    public Integer getTimeNotOnOrAfter() {
        return 300;
    }

    @Override
    public String isEidCrossBordShare() {
        return null;
    }

    @Override
    public String isEidCrossBorderShare() {
        return "true";
    }

    @Override
    public String isEidCrossSectShare() {
        return "true";
    }

    @Override
    public String isEidCrossSectorShare() {
        return "true";
    }

    @Override
    public String isEidSectorShare() {
        return "true";
    }

    @Override
    public boolean isIpValidation() {
        return false;
    }

    @Override
    public boolean isOneTimeUse() {
        return false;
    }

    @Override
    public boolean isValidateSignature() {
        return true;
    }
}
