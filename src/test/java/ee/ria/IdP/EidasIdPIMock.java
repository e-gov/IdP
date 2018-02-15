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

package ee.ria.IdP;

import ee.ria.IdP.eidas.EidasIdPI;
import ee.ria.IdP.exceptions.InvalidAuthRequest;
import ee.ria.IdP.model.EENaturalPerson;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

public class EidasIdPIMock implements EidasIdPI {
    @Override
    public IAuthenticationRequest parseRequest(String samlRequest) {
        EidasAuthenticationRequest.Builder builder = EidasAuthenticationRequest.builder();
        builder.originCountryCode("ET");
        builder.assertionConsumerServiceURL( "https://someurl.com/");
        builder.binding( "POST");
        builder.citizenCountryCode( "ET");
        builder.destination( "mocked" );
        builder.id( "mockid");
        builder.issuer( "mockissuer");
        builder.levelOfAssurance( "http://eidas.europa.eu/LoA/high");
        builder.nameIdFormat(null);
        builder.providerName("mockprovider");
        builder.requestedAttributes(null); //attributeMapBuilder.build());

        builder.levelOfAssuranceComparison(LevelOfAssuranceComparison.MINIMUM.stringValue());
        builder.spType(null);

        EidasAuthenticationRequest request;
        request = builder.build();
        return request;
    }

        @Override
    public String buildAuthenticationResponse(IAuthenticationRequest authRequest, EENaturalPerson naturalPerson) {
        return "mock_response";
    }

    @Override
    public String buildErrorResponse(IAuthenticationRequest authRequest) {
        return "mock_error_response";
    }
}
