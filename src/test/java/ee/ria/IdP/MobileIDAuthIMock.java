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

import com.codeborne.security.mobileid.MobileIDSession;
import ee.ria.IdP.exceptions.MobileIdError;
import ee.ria.IdP.mobileid.MobileIDAuthI;
import ee.ria.IdP.model.IdPTokenCacheItem;

public class MobileIDAuthIMock implements MobileIDAuthI {
    @Override
    public MobileIDSession startMobileIdAuth(String personalCode, String phoneNumber) throws MobileIdError {
        if("throw".equals(personalCode))
            throw new MobileIdError("throwed for personal code as requested", null);
        if("throw".equals(phoneNumber))
            throw new MobileIdError("throwed for phone number as requested", null);
        MobileIDSession mockSession = new MobileIDSession(1, "mock_challenge",
                "mock_firstname", "mock_lastname", "36002121234");
        return mockSession;
    }

    @Override
    public boolean checkMobileIdAuth(IdPTokenCacheItem cacheItem) throws MobileIdError {
        return "finished".equals(cacheItem.getOriginalRequest());
    }
}
