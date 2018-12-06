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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import ee.ria.IdP.eidas.EidasIdPI;
import ee.ria.IdP.exceptions.IdCardNotFound;
import ee.ria.IdP.exceptions.InvalidAuthData;
import ee.ria.IdP.exceptions.InvalidAuthRequest;
import ee.ria.IdP.exceptions.MobileIdError;
import ee.ria.IdP.metadata.MetaDataI;
import ee.ria.IdP.mobileid.MobileIDAuthI;
import ee.ria.IdP.model.EENaturalPerson;
import ee.ria.IdP.model.IdPTokenCacheItem;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.bouncycastle.util.encoders.Base64;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sun.security.provider.X509Factory;
import sun.security.x509.X500Name;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

@Controller
public class IdPMainController {
    private static final Logger LOG = LoggerFactory.getLogger(IdPMainController.class);

    private MetaDataI metaDataI;
    private EidasIdPI eidasIdPI;
    private MobileIDAuthI mobileIDAuth;
    private Cache<String, IdPTokenCacheItem> tokenCache;

    private String baseUrl;

    private DateTimeFormatter dateFormatter;

    public IdPMainController(MetaDataI metaDataI, EidasIdPI eidasIdPI,
                             MobileIDAuthI mobileIDAuthI,
                             @Value("${TokenExpiration: 200}") long tokenExpiration) {
        this.metaDataI = metaDataI;
        this.eidasIdPI = eidasIdPI;
        this.mobileIDAuth = mobileIDAuthI;

        tokenCache = CacheBuilder.newBuilder()
                .expireAfterWrite( tokenExpiration, TimeUnit.SECONDS)
                .build();

        dateFormatter = DateTimeFormat.forPattern("dd.MM.yyyy");
    }

    @GetMapping(value="/metadata", produces = "application/xml; charset=utf-8")
    public @ResponseBody String getMetaData() throws EIDASSAMLEngineException {
        LOG.debug("/metadata request started");
        String metaData = metaDataI.generateMetadata();
        LOG.debug("/metadata request finished");
        return metaData;
    }

    @PostMapping(value="/auth")
    public String showWelcome(@RequestParam(required = false) String SAMLRequest,
                              @RequestParam(required = false) String lang, Model model) throws InvalidAuthRequest {
        LOG.debug("/auth request started");

        // this also verifies request, without request, can not return
        eidasIdPI.parseRequest(SAMLRequest);

        model.addAttribute("lang", lang);
        model.addAttribute("SAMLRequest", SAMLRequest);
        LOG.debug("/auth request finished");
        return "welcome";
    }

    @PostMapping(value="/idauth")
    public String authenticate(HttpServletRequest request,
                               @RequestParam(required = false) String SAMLRequest,
                               @RequestParam(required = false) String lang,
                               Model model) throws InvalidAuthRequest {
        IAuthenticationRequest authenticationRequest = eidasIdPI.parseRequest(SAMLRequest);

        EENaturalPerson naturalPerson;
        try {
            X509Certificate clientCert = readClientCertificate(request);
            naturalPerson = parseClientCertificate(clientCert);
        } catch (IdCardNotFound idCardNotFound) {
            return fillErrorInfo(model, SAMLRequest, authenticationRequest,idCardNotFound,"error.idcard.notfound", lang);
        } catch (InvalidAuthData invalidAuthData) {
            return fillErrorInfo(model, SAMLRequest, authenticationRequest, invalidAuthData, "error.general", lang);
        }

        //EENaturalPerson naturalPerson = new EENaturalPerson("Kiilaspea", "Mati",
        //        "34010111234");

        String response = eidasIdPI.buildAuthenticationResponse(authenticationRequest,naturalPerson);

        model.addAttribute("lang", lang);
        model.addAttribute("SAMLResponse", response);
        model.addAttribute( "responseCallback", authenticationRequest.getAssertionConsumerServiceURL());

        addPersonAttributes(model, naturalPerson);
        return "authorize";
    }

    private String fillErrorInfo(Model model, String originalRequest, IAuthenticationRequest authenticationRequest, Exception exception,
                                 String errorMessageCode, String lang) {
        if(authenticationRequest != null) {
            model.addAttribute( "SAMLRequest", originalRequest);
            model.addAttribute("SAMLResponse", eidasIdPI.buildErrorResponse(authenticationRequest));
            model.addAttribute( "responseCallback", authenticationRequest.getAssertionConsumerServiceURL());
        }
        else {
            model.addAttribute( "SAMLRequest", "");
            model.addAttribute("SAMLResponse", "");
            model.addAttribute( "responseCallback", "");
        }
        model.addAttribute("exception", exception);
        model.addAttribute("lang", lang);
        model.addAttribute( "errorMessageCode", errorMessageCode);

        return "error";
    }

    private void addPersonAttributes(Model model, EENaturalPerson naturalPerson) {
        model.addAttribute("personalCode", naturalPerson.getIdCode());
        model.addAttribute( "surName", naturalPerson.getFamilyName());
        model.addAttribute( "name", naturalPerson.getFirstName());
        model.addAttribute( "birthDate", dateFormatter.print(naturalPerson.getBirthDate()));
    }

    private X509Certificate readClientCertificate(HttpServletRequest request) throws IdCardNotFound, InvalidAuthData {
        String verifyStatus = request.getHeader("SSL_CLIENT_VERIFY");

        if(!"SUCCESS".equals(verifyStatus)) {
            if (verifyStatus == null || "".equals(verifyStatus)) {
                LOG.error("SSL_CLIENT_VERIFY header not found");
                throw new IllegalStateException("SSL_CLIENT_VERIFY header not found");
            }

            // NONE, SUCCESS, GENEROUS or FAILED:reason
            if ("NONE".equals(verifyStatus) || "GENEROUS".equals(verifyStatus)) {// no certificate or not the one requested
                LOG.info("IdCard verification failed, status {}", verifyStatus);
                throw new IdCardNotFound("idcard.notfound");
            }

            if (verifyStatus.startsWith("FAILED:")) {
                LOG.info("IdCard cert did not verify: {}", verifyStatus);
                throw new InvalidAuthData("idcard.verify.failed");
            }

            LOG.error("Unexpected ssl verify status: {}", verifyStatus);
            throw new IllegalStateException("Unexpected ssl verify status: " + verifyStatus);
        }

        String certHeader = request.getHeader("SSL_CLIENT_CERT");
        if(certHeader == null || "".equals(certHeader)) {
            LOG.error("SSL_CLIENT_CERT header not found");
            throw new IllegalStateException("SSL_CLIENT_CERT header not found");
        }

            // remove PEM header and footer
        certHeader = certHeader.substring(X509Factory.BEGIN_CERT.length(),
                certHeader.length() - X509Factory.END_CERT.length() - 1); // funny things with whitespace

        byte[] decoded = Base64.decode(certHeader);
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(new ByteArrayInputStream(decoded));
            return (X509Certificate) cert;
        } catch (CertificateException e) {
            LOG.error("Unexpected error parsing certificate", e);
            // should not happen as certificate was already verified!
            throw new RuntimeException(e);
        }
    }

    private EENaturalPerson parseClientCertificate(X509Certificate clientCert) throws InvalidAuthData {
        String clientName = clientCert.getSubjectX500Principal().getName();
        String cn = null;
        try {
            X500Name x500Name = new X500Name(clientName);
            cn = x500Name.getCommonName();
        } catch (IOException e) {
            LOG.error("Could not parse certificate", e);
            throw new InvalidAuthData("invalid.cert");
        }
        String[] cnfields = cn.split(",");
        if(cnfields.length != 3) {
            // should not happen as cert was verified
            LOG.error("Invalid certificate common name found: {}", cn);
            throw new InvalidAuthData("invalid.cert");
        }
        return new EENaturalPerson(cnfields[0],cnfields[1],cnfields[2]);
    }

    @PostMapping(value="/midwelcome")
    public String showMobileIdStart(@RequestParam(required = false) String SAMLRequest,
                                    @RequestParam(required = false) String lang,
                                    Model model) throws InvalidAuthRequest {
        eidasIdPI.parseRequest(SAMLRequest);

        model.addAttribute("lang", lang);
        model.addAttribute("SAMLRequest", SAMLRequest);
        return "midstart";
    }

    @PostMapping(value="/midauth")
    public String startMobileIdAuth(@RequestParam(required = false) String SAMLRequest,
                                    @RequestParam(required = false) String lang,
                                    @RequestParam(required = false) String personalCode,
                                    @RequestParam(required = false) String phoneNumber,
                                    Model model) throws InvalidAuthRequest {
        IAuthenticationRequest authenticationRequest = eidasIdPI.parseRequest(SAMLRequest);

        if (personalCode == null) {
            return fillErrorInfo(model, SAMLRequest, authenticationRequest, null, "error.personal.code", lang);
        }
        if (phoneNumber == null) {
            return fillErrorInfo(model, SAMLRequest, authenticationRequest, null, "error.phone.number", lang);
        }

        MobileIDSession mobileIDSession;
        try {
            mobileIDSession = mobileIDAuth.startMobileIdAuth(personalCode, phoneNumber);
        } catch (MobileIdError mobileIdError) {
            return fillErrorInfo(model, SAMLRequest, authenticationRequest,mobileIdError,"error.mobileid", lang);
        }

        IdPTokenCacheItem cacheItem = new IdPTokenCacheItem(SAMLRequest, authenticationRequest, mobileIDSession);
        String sessionToken = String.valueOf(cacheItem.getMobileIDSession().sessCode);
        putCacheItem(cacheItem,sessionToken);

        model.addAttribute("lang", lang);
        model.addAttribute("SAMLRequest", SAMLRequest);
        model.addAttribute( "sessionToken", sessionToken);
        model.addAttribute( "challenge", mobileIDSession.challenge);
        model.addAttribute( "checkUrl", "midstatus?sessionToken=" + cacheItem.getMobileIDSession().sessCode);
        return "midwait";
    }

    // needed for test so not private
    void putCacheItem(IdPTokenCacheItem cacheItem, String sessionToken) {
        tokenCache.put(sessionToken,cacheItem);
    }

    @PostMapping(value="/midcheck")
    public String showMobileIdCheck(@RequestParam(required = false) String sessionToken,
                                    @RequestParam(required = false) String lang,
                                    Model model) {
        IdPTokenCacheItem cacheItem = tokenCache.getIfPresent(sessionToken);
        if(cacheItem==null) {
            return "fatal_error";
        }

        if(!cacheItem.getCompleted()) {
            try {
                if (!mobileIDAuth.checkMobileIdAuth(cacheItem)) {
                    model.addAttribute("sessionToken", sessionToken);
                    model.addAttribute("challenge", cacheItem.getMobileIDSession().challenge);
                    model.addAttribute("checkUrl", "midstatus?sessionToken=" + sessionToken);
                    model.addAttribute("SAMLRequest", cacheItem.getOriginalRequest());
                    model.addAttribute("lang", lang);
                    return "midwait";
                }
            } catch (MobileIdError mobileIdError) {
                return fillErrorInfo(model, cacheItem.getOriginalRequest(), cacheItem.getSamlRequest(),mobileIdError,"error.mobileid", lang);
            }
        } else if(cacheItem.getError()!=null) {
            return fillErrorInfo(model, cacheItem.getOriginalRequest(), cacheItem.getSamlRequest(),cacheItem.getError(),"error.mobileid", lang);
        }

        EENaturalPerson naturalPerson;
        try {
            naturalPerson = new EENaturalPerson(cacheItem.getMobileIDSession().lastName,
                    cacheItem.getMobileIDSession().firstName, cacheItem.getMobileIDSession().personalCode);

        } catch (InvalidAuthData invalidAuthData) {
            return fillErrorInfo(model, cacheItem.getOriginalRequest(), cacheItem.getSamlRequest(), invalidAuthData, "error.general", lang);
        }

        String response = eidasIdPI.buildAuthenticationResponse(cacheItem.getSamlRequest(),naturalPerson);

        model.addAttribute("SAMLResponse", response);
        model.addAttribute( "responseCallback", cacheItem.getSamlRequest().getAssertionConsumerServiceURL());
        model.addAttribute("lang", lang);

        addPersonAttributes(model, naturalPerson);

        return "authorize";
    }

    @GetMapping("/midstatus")
    @ResponseBody
    public String getMobileIdStatus(@RequestParam(required = false) String sessionToken) {
        IdPTokenCacheItem cacheItem = tokenCache.getIfPresent(sessionToken);
        if(cacheItem==null)
            return "ERROR";

        if(cacheItem.getCompleted())
            return "OK";

        try {
            if(!mobileIDAuth.checkMobileIdAuth(cacheItem))
                return "WAIT";
        } catch (MobileIdError mobileIdError) {
            return "ERROR";
        }

        return "OK";
    }
}
