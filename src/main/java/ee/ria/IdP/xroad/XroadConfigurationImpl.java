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

package ee.ria.IdP.xroad;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class XroadConfigurationImpl {

    @Value("${XroadServerUrl}")
    private String xroadServerUrl;
    @Value("${XroadServerConnectTimeoutInMilliseconds:3000}")
    private int xroadServerConnectTimeoutInMilliseconds;
    @Value("${XroadServerReadTimeoutInMilliseconds:3000}")
    private int xroadServerReadTimeoutInMilliseconds;

    @Value("${XRoadServiceRoadInstance}")
    private String xRoadServiceRoadInstance;
    @Value("${XRoadServiceMemberClass}")
    private String xRoadServiceMemberClass;
    @Value("${XRoadServiceMemberCode}")
    private String xRoadServiceMemberCode;
    @Value("${XRoadServiceSubsystemCode}")
    private String xRoadServiceSubsystemCode;

    @Value("${XRoadClientSubSystemRoadInstance}")
    private String xRoadClientSubSystemRoadInstance;
    @Value("${XRoadClientSubSystemMemberClass}")
    private String xRoadClientSubSystemMemberClass;
    @Value("${XRoadClientSubSystemMemberCode}")
    private String xRoadClientSubSystemMemberCode;
    @Value("${XRoadClientSubSystemSubsystemCode}")
    private String xRoadClientSubSystemSubsystemCode;

    @Value("${XRoadEsindusv2AllowedTypes: TÜ, UÜ, OÜ, AS, TÜH, SA, MTÜ}")
    private String[] xRoadEsindusv2AllowedTypes;
}
