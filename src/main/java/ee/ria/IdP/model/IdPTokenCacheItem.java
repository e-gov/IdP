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

package ee.ria.IdP.model;

import com.codeborne.security.mobileid.MobileIDSession;
import ee.ria.IdP.exceptions.MobileIdError;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import org.joda.time.DateTime;

/**
 * Model object holding data about ongoing mobile id sessions
 */
public class IdPTokenCacheItem {
    private String originalRequest;
    private IAuthenticationRequest samlRequest;
    private MobileIDSession mobileIDSession;
    private boolean isLegalPersonRequest = false;

    private boolean completed = false;
    private MobileIdError error = null;

    public IdPTokenCacheItem(String originalRequest, IAuthenticationRequest samlRequest, MobileIDSession mobileIDSession) {
        this(originalRequest, samlRequest, mobileIDSession, false);
    }

    public IdPTokenCacheItem(String originalRequest, IAuthenticationRequest samlRequest, MobileIDSession mobileIDSession, boolean isLegalPersonRequest) {
        this.originalRequest = originalRequest;
        this.samlRequest = samlRequest;
        this.mobileIDSession = mobileIDSession;
        this.isLegalPersonRequest = isLegalPersonRequest;
    }

    public String getOriginalRequest() {
        return originalRequest;
    }

    public IAuthenticationRequest getSamlRequest() {
        return samlRequest;
    }

    public MobileIDSession getMobileIDSession() {
        return mobileIDSession;
    }

    public synchronized void setCompleted(MobileIdError e) {
        completed = true;
        error = e;
    }

    public synchronized boolean getCompleted() {
        return completed;
    }

    public synchronized MobileIdError getError() {
        return error;
    }

    public boolean isLegalPersonRequest() {
        return isLegalPersonRequest;
    }
}
