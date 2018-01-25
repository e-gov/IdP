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

import eu.eidas.auth.engine.metadata.impl.CachingMetadataFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class fetching the metadata from remote parties.
 */
public class IdPMetadataFetcher extends CachingMetadataFetcher {
        private static final Logger LOG = LoggerFactory.getLogger(IdPMetadataFetcher.class);

        public IdPMetadataFetcher() {
            super();
            setCache(new IdPMetadataCache());
            /*if (StringUtils.isNotEmpty(IDPUtil.getMetadataRepositoryPath())) {
                FileMetadataLoader fp = new FileMetadataLoader();
                fp.setRepositoryPath(IDPUtil.getMetadataRepositoryPath());
                setMetadataLoaderPlugin(fp);
            }*/
            initProcessor();
        }

        @Override
        public boolean isHttpRetrievalEnabled() {
            return true;
        }

        @Override
        protected boolean mustUseHttps() {
            return false;
        }

        @Override
        protected boolean mustValidateSignature(String url) {
            //setTrustedEntityDescriptors(IDPUtil.getTrustedEntityDescriptors());
            return super.mustValidateSignature(url);
        }
}
