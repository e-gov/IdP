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

import com.google.common.cache.CacheBuilder;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.metadata.*;
import eu.eidas.encryption.exception.MarshallException;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.signature.SignableXMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Cache class storing the values of remote parties metadata
 */
public class IdPMetadataCache implements IMetadataCachingService {

    private static final Logger LOG = LoggerFactory.getLogger(IdPMetadataCache.class);

    private static final String SIGNATURE_HOLDER_ID_PREFIX="signatureholder";

    private ConcurrentMap<String, SerializedEntityDescriptor> map = null;

    // todo: correct values for these parameters?
    protected Map<String, SerializedEntityDescriptor> getMap() {
        if (map == null) {
            map = CacheBuilder.newBuilder()
                    .expireAfterAccess(86400L, TimeUnit.SECONDS)
                    .maximumSize(10000L).<String, SerializedEntityDescriptor>build().asMap();
        }
        return map;
    }

    private class SerializedEntityDescriptor {
        /**
         * the entitydescriptor serialized as xml
         */
        private String serializedEntityDescriptor;

        /**
         * the type/origin (either statically loaded or retrieved from the network)
         */
        private EntityDescriptorType type;

        public SerializedEntityDescriptor(String descriptor, EntityDescriptorType type) {
            setSerializedEntityDescriptor(descriptor);
            setType(type);
        }

        public String getSerializedEntityDescriptor() {
            return serializedEntityDescriptor;
        }

        public void setSerializedEntityDescriptor(String serializedEntityDescriptor) {
            this.serializedEntityDescriptor = serializedEntityDescriptor;
        }

        public EntityDescriptorType getType() {
            return type;
        }

        public void setType(EntityDescriptorType type) {
            this.type = type;
        }
    }


    @Override
    public final EntityDescriptor getDescriptor(String url) throws EIDASMetadataProviderException {
        if(getMap()!=null){
            SerializedEntityDescriptor content=getMap().get(url);
            if(content!=null && !content.getSerializedEntityDescriptor().isEmpty()) {
                try {
                    return deserializeEntityDescriptor(content.getSerializedEntityDescriptor());
                } catch (UnmarshallException e) {
                    LOG.error("Unable to deserialize metadata entity descriptor from cache for "+url);
                    LOG.error(e.getStackTrace().toString());
                    throw new EIDASMetadataProviderException(e.getMessage());
                }
            }
        }
        return null;
    }

    @Override
    public final void putDescriptor(String url, EntityDescriptor ed, EntityDescriptorType type) {
        if(getMap()!=null){
            if(ed==null){
                getMap().remove(url);
            }else {
                String content = serializeEntityDescriptor(ed);
                if (content != null && !content.isEmpty()) {
                    getMap().put(url, new SerializedEntityDescriptor(content, type));
                }
            }
        }
    }
    @Override
    public final EntityDescriptorType getDescriptorType(String url) {
        if (getMap() != null) {
            SerializedEntityDescriptor content = getMap().get(url);
            if (content != null) {
                return content.getType();
            }
        }
        return null;
    }

    private String serializeEntityDescriptor(XMLObject ed){
        try {
            return EidasStringUtil.toString(OpenSamlHelper.marshall(ed));
        } catch (MarshallException e) {
            throw new IllegalStateException(e);
        }
    }

    private EntityDescriptor deserializeEntityDescriptor(String content) throws UnmarshallException {
        EntityDescriptorContainer container = MetadataUtil.deserializeEntityDescriptor(content);
        return container.getEntityDescriptors().isEmpty()?null:container.getEntityDescriptors().get(0);
    }

    @Override
    public void putDescriptorSignatureHolder(String url, SignableXMLObject container){
        getMap().put(SIGNATURE_HOLDER_ID_PREFIX+url, new SerializedEntityDescriptor(serializeEntityDescriptor(container), EntityDescriptorType.NONE));
    }

    @Override
    public void putDescriptorSignatureHolder(String url, EntityDescriptorContainer container){
        if(container.getSerializedEntitesDescriptor()!=null){
            getMap().put(SIGNATURE_HOLDER_ID_PREFIX+url, new SerializedEntityDescriptor(EidasStringUtil.toString(container.getSerializedEntitesDescriptor()), EntityDescriptorType.SERIALIZED_SIGNATURE_HOLDER));
        }else{
            putDescriptorSignatureHolder(url, container.getEntitiesDescriptor());
        }
    }

}
