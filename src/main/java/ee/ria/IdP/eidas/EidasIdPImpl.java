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

import ee.ria.IdP.exceptions.InvalidAuthRequest;
import ee.ria.IdP.metadata.MetaDataConfigurationI;
import ee.ria.IdP.model.EENaturalPerson;
import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASSubStatusCode;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.attribute.*;
import eu.eidas.auth.commons.attribute.impl.DateTimeAttributeValue;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.eidas.spec.NaturalPersonSpec;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.bouncycastle.util.encoders.Base64;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.print.DocFlavor;
import java.util.ArrayList;

/**
 * Eidas protocol responder for id provider
 */
@Service
public class EidasIdPImpl implements EidasIdPI {
    private static final Logger LOG = LoggerFactory.getLogger(EidasIdPImpl.class);

    private static final String OUR_EIDAS_LOA = "http://eidas.europa.eu/LoA/high";

    private static AttributeRegistry EE_ATTRIBUTES;

    private ProtocolEngineI protocolEngine;
    private MetaDataConfigurationI metaDataConfiguration;
    private MetadataFetcherI metadataFetcherI;

    public EidasIdPImpl(ProtocolEngineI protocolEngine, MetadataFetcherI metadataFetcherI,
                        MetaDataConfigurationI metaDataConfiguration) {
        this.protocolEngine = protocolEngine;
        this.metadataFetcherI = metadataFetcherI;
        this.metaDataConfiguration = metaDataConfiguration;

        EE_ATTRIBUTES = AttributeRegistries.of( new AttributeDefinition[]{
                NaturalPersonSpec.Definitions.PERSON_IDENTIFIER,
                NaturalPersonSpec.Definitions.CURRENT_FAMILY_NAME,
                NaturalPersonSpec.Definitions.CURRENT_GIVEN_NAME,
                NaturalPersonSpec.Definitions.DATE_OF_BIRTH});

    }

    /**
     * Parses SAML request received from client browser
     *
     * @param samlRequest
     * @return
     * @throws InvalidAuthRequest
     */
    public IAuthenticationRequest parseRequest(String samlRequest) throws InvalidAuthRequest {
        if(samlRequest==null || "".equals(samlRequest)) {
            LOG.error("SamlRequest missing");
            throw new InvalidAuthRequest("no.samlrequest");
        }

        IAuthenticationRequest authRequest;
        try {
            authRequest = protocolEngine.unmarshallRequestAndValidate(Base64.decode(samlRequest),
                    metaDataConfiguration.getLocalCountry());
        }
        catch(EIDASSAMLEngineException e) {
            LOG.error("Error decoding SamlRequest", e);
            throw new InvalidAuthRequest("invalid.samlrequest");
        }

        LOG.info("SAML request ID: " + authRequest.getId());
        if (LOG.isDebugEnabled())
            LOG.debug(authRequest.toString());

        checkRequestAttributes(authRequest);

        authRequest = addRequestCallback(authRequest);

        return authRequest;
    }

    private IAuthenticationRequest addRequestCallback(IAuthenticationRequest authRequest) throws InvalidAuthRequest {

        if (authRequest.getAssertionConsumerServiceURL() != null)
            return authRequest;

        EidasAuthenticationRequest.Builder builder =
                    EidasAuthenticationRequest.builder((IEidasAuthenticationRequest) authRequest);
        String callback = null;
        try {
            callback = MetadataUtil.getAssertionConsumerUrlFromMetadata(metadataFetcherI,
                (MetadataSignerI) protocolEngine.getSigner(), authRequest);
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error fetching metadata", e);
            throw new InvalidAuthRequest("no.callback");
        }

        if(callback==null) {
            LOG.error("Request nor metadata did not contain callback url");
            throw new InvalidAuthRequest("no.callback");
        }
        builder.assertionConsumerServiceURL(callback);
        IAuthenticationRequest newAuthnRequest = builder.build();
        return newAuthnRequest;
    }

    // check that we support all requested mandatory attributes
    private void checkRequestAttributes(IAuthenticationRequest authRequest) throws InvalidAuthRequest {
        for(AttributeDefinition<?> attributeDefinition : authRequest.getRequestedAttributes().getDefinitions()) {
            if(attributeDefinition.isRequired()) {
                if(!EE_ATTRIBUTES.contains(attributeDefinition)) {
                    LOG.error("Requested mandatory attribute {} is not supported", attributeDefinition.getFriendlyName());
                    throw new InvalidAuthRequest("Requested mandatory attribute " + attributeDefinition.getFriendlyName() +
                            " is not supported.");
                }
            }
        }
    }

    private AuthenticationResponse.Builder createResponseBuilder(IAuthenticationRequest authRequest) {
        AuthenticationResponse.Builder authResponse = new AuthenticationResponse.Builder();

        authResponse.inResponseTo(authRequest.getId());
        authResponse.issuer(metaDataConfiguration.getMetaDataUrl());
        authResponse.id(SAMLEngineUtils.generateNCName());
        authResponse.levelOfAssurance(OUR_EIDAS_LOA);

        return authResponse;
    }

    private void addStringAttributeValue(ImmutableAttributeMap.Builder builder, AttributeDefinition<String> attr,
                                         String value) {
        ArrayList<StringAttributeValue> valueList = new ArrayList<>(2);
        // non transliterated version is "alternate"
        if (AttributeValueTransliterator.needsTransliteration(value)) {
            String trValue = AttributeValueTransliterator.transliterate(value);
            valueList.add(new StringAttributeValue(trValue));
        }
        valueList.add(new StringAttributeValue(value)); // stringattributevalue detects internally if this is alternate or not
        builder.put(attr, valueList);
    }

    private void addDateAttributeValue(ImmutableAttributeMap.Builder builder, AttributeDefinition attr,
                                       DateTime value) {
        builder.put(attr, new DateTimeAttributeValue(value));
    }

    /**
     * Builds successful authentication response
     * @param authRequest original authentication request
     * @param naturalPerson authentication person data
     * @return Base64 encoded response message
     */
    public String buildAuthenticationResponse(IAuthenticationRequest authRequest,
                                                 EENaturalPerson naturalPerson) {
        AuthenticationResponse.Builder authResponse = createResponseBuilder(authRequest);
        authResponse.statusCode(EIDASStatusCode.SUCCESS_URI.toString());

        ImmutableAttributeMap.Builder mapBuilder = ImmutableAttributeMap.builder();

        // for now lets add all attributes we have
        addStringAttributeValue(mapBuilder, NaturalPersonSpec.Definitions.PERSON_IDENTIFIER,
                naturalPerson.getIdCode());
        addStringAttributeValue(mapBuilder, NaturalPersonSpec.Definitions.CURRENT_GIVEN_NAME,
                naturalPerson.getFirstName());
        addStringAttributeValue(mapBuilder, NaturalPersonSpec.Definitions.CURRENT_FAMILY_NAME,
                naturalPerson.getFamilyName());
        addDateAttributeValue(mapBuilder, NaturalPersonSpec.Definitions.DATE_OF_BIRTH,
                naturalPerson.getBirthDate());

        ImmutableAttributeMap attributes = mapBuilder.build();
        authResponse.attributes(attributes);

        AuthenticationResponse authenticationResponse = authResponse.build();

        LOG.info("SAML response ID: " + authenticationResponse.getId());
        if (LOG.isDebugEnabled())
            LOG.debug(authenticationResponse.toString());

        try {
            IResponseMessage responseMessage = protocolEngine.generateResponseMessage(authRequest, authenticationResponse,
                    protocolEngine.getSigner().isResponseSignAssertions(), "127.0.0.1");
            return new String(Base64.encode(responseMessage.getMessageBytes()));
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Could not build SAML authentication response message", e);
            throw new RuntimeException("saml.error");
        }
    }

    /**
     * Builds authentication response in case of error
     * @param authRequest orginal authentication request
     * @return Base64 encoded error response
     */
    public String buildErrorResponse(IAuthenticationRequest authRequest) {
        AuthenticationResponse.Builder responseBuilder = createResponseBuilder(authRequest);

        responseBuilder.statusCode(EIDASStatusCode.RESPONDER_URI.toString());
        responseBuilder.subStatusCode(EIDASSubStatusCode.AUTHN_FAILED_URI.toString());
        responseBuilder.statusMessage(EidasErrorKey.AUTHENTICATION_FAILED_ERROR.toString());

        AuthenticationResponse authenticationResponse = responseBuilder.build();

        try {
            IResponseMessage responseMessage = protocolEngine.generateResponseErrorMessage(
                    authRequest, authenticationResponse, "127.0.0.1");
            return new String(Base64.encode(responseMessage.getMessageBytes()));
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Could not build SAML error response message", e);
            throw new RuntimeException("saml.error");
        }
    }
}
