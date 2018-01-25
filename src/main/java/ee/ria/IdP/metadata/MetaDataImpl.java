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

import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.EidasMetadata;
import eu.eidas.auth.engine.metadata.MetadataConfigParams;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.opensaml.common.xml.SAMLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Id provider metadata assembler
 */
@Component
public class MetaDataImpl implements MetaDataI {
    private static final Logger LOG = LoggerFactory.getLogger(MetaDataImpl.class);

    private ProtocolEngineI engine;
    private MetaDataConfigurationI configuration;

    public MetaDataImpl(ProtocolEngineI engine, MetaDataConfigurationI configuration){
        this.engine = engine;
        this.configuration = configuration;
    }

    @Override
    public String generateMetadata() throws EIDASSAMLEngineException {
        EidasMetadata.Generator generator = EidasMetadata.generator();
        MetadataConfigParams.Builder mcp = MetadataConfigParams.builder();
        mcp.idpEngine(engine);
        mcp.entityID(configuration.getMetaDataUrl());
        mcp.addProtocolBindingLocation(SAMLConstants.SAML2_POST_BINDING_URI, configuration.getPostBindingUrl());
        mcp.technicalContact(configuration.getTechnicalContact());
        mcp.supportContact(configuration.getSupportContact());
        mcp.organization(configuration.getOrganization());
        mcp.signingMethods(configuration.getSigningMethods());
        mcp.digestMethods(configuration.getDigestMethods());
        mcp.encryptionAlgorithms(configuration.getEncryptionAlgorithms());
        generator.configParams(mcp.build());
        EidasMetadata eidasMetadata = generator.build();
        return eidasMetadata.getMetadata();
    }
}
