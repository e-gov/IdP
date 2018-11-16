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

package ee.ria.IdP.mobileid;

import com.codeborne.security.AuthenticationException;
import com.codeborne.security.mobileid.MobileIDSession;
import ee.ria.IdP.exceptions.MobileIdError;
import ee.ria.IdP.model.IdPTokenCacheItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Mobile Id authentication service
 * uses codeborne mobile id library
 */
@Service
public class MobileIDAuth implements MobileIDAuthI {
    private static final Logger LOG = LoggerFactory.getLogger(MobileIDAuth.class);

    private MobileIDAuthenticatorWrapper authenticator;

    /**
     *
     * @param serviceUrl mobile id service url, default is https://tsp.demo.sk.ee (testing value)
     * @param serviceName mobile id service name, default is "Testimine" (used for testing
     */
    @Autowired
    public MobileIDAuth(@Value("${DigiDocServiceUrl: https://tsp.demo.sk.ee}") String serviceUrl,
                        @Value("${DigiDocServiceName: Testimine}") String serviceName) {
        // make sure that keystores are set correctly at tomcat level!

        authenticator = new MobileIDAuthenticatorWrapper(serviceUrl);
        authenticator.setServiceName(serviceName);
    }

    /**
     * Starts a mobile id authentication session
     *
     * @param phoneNumber phone number of the user
     * @return MobileIdSession that will be used later to check status of authentication
     * @throws MobileIdError
     */
    @Override
    public MobileIDSession startMobileIdAuth(String personalCode, String phoneNumber) throws MobileIdError {
        try {
            MobileIDSession mobileIDSession = authenticator.startLogin(personalCode, phoneNumber);
            return mobileIDSession;
        }
        catch (AuthenticationException e) {
            throw new MobileIdError("mobileid.fail", e);
        }
    }

    /**
     * Checks the status of mobile id authentication session
     * @param cacheItem cached session item saved from startMobileIdAuth
     * @return true if authentication has succeeded, false if authentication is still in progress
     * @throws MobileIdError when some error happened during authentication or user cancelled
     */
    @Override
    public boolean checkMobileIdAuth(IdPTokenCacheItem cacheItem) throws MobileIdError {
        // these values are just placeholders for mobileid library
        MobileIDSession placeholder = new MobileIDSession(cacheItem.getMobileIDSession().sessCode,
                "", "X", "X", "33333");
        try {
            if(authenticator.isLoginComplete(placeholder)) {
                cacheItem.setCompleted(null);
                return true;
            }
            return false;
        }
        catch (AuthenticationException e) {
            MobileIdError error = new MobileIdError("mobileid.fail", e);
            cacheItem.setCompleted(error);
            throw error;
        }
    }
}
