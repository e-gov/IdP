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
import com.google.common.base.Splitter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import ee.ria.IdP.eidas.EidasIdPI;
import ee.ria.IdP.eidas.EidasIdPImpl;
import ee.ria.IdP.exceptions.*;
import ee.ria.IdP.metadata.MetaDataI;
import ee.ria.IdP.mobileid.MobileIDAuthI;
import ee.ria.IdP.model.EELegalPerson;
import ee.ria.IdP.model.EENaturalPerson;
import ee.ria.IdP.model.IdPTokenCacheItem;
import ee.ria.IdP.xroad.EBusinessRegistryService;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.bouncycastle.util.encoders.Base64;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.*;

@Controller
public class IdPMainController {

    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";
    private static final Logger LOG = LoggerFactory.getLogger(IdPMainController.class);

    private MetaDataI metaDataI;
    private EidasIdPI eidasIdPI;
    private MobileIDAuthI mobileIDAuth;
    private EBusinessRegistryService eBusinessRegistryService;
    private Cache<String, IdPTokenCacheItem> tokenCache;

    private String baseUrl;

    private DateTimeFormatter dateFormatter;

    public IdPMainController(MetaDataI metaDataI, EidasIdPI eidasIdPI,
                             MobileIDAuthI mobileIDAuthI, EBusinessRegistryService eBusinessRegistryService,
                             @Value("${TokenExpiration: 200}") long tokenExpiration) {
        this.metaDataI = metaDataI;
        this.eidasIdPI = eidasIdPI;
        this.mobileIDAuth = mobileIDAuthI;
        this.eBusinessRegistryService = eBusinessRegistryService;

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

        addPersonAttributes(model, naturalPerson);


        boolean isLegalPersonRequest = isLegalPersonRequest(authenticationRequest);
        if (isLegalPersonRequest) {
            HttpSession session = request.getSession();
            session.setAttribute("naturalPerson", naturalPerson);
            session.setAttribute("samlRequest", authenticationRequest);

            model.addAttribute( "responseCallback", authenticationRequest.getAssertionConsumerServiceURL());
            model.addAttribute( "SAMLRequest", SAMLRequest);
            model.addAttribute("SAMLResponse", eidasIdPI.buildErrorResponse(authenticationRequest));
            model.addAttribute("lang", lang);
            addPersonAttributes(model, naturalPerson);
            return "legal-person-select";
        } else {
            String response = eidasIdPI.buildAuthenticationResponse(authenticationRequest,naturalPerson);
            model.addAttribute("lang", lang);
            model.addAttribute("SAMLResponse", response);
            model.addAttribute( "responseCallback", authenticationRequest.getAssertionConsumerServiceURL());
            return "authorize";
        }
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

    private void addLegalPersonAttributes(Model model, EELegalPerson legalPerson) {
        model.addAttribute("legalPersonName", legalPerson.getLegalName());
        model.addAttribute("legalPersonIdentifier", legalPerson.getLegalPersonIdentifier());
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
        certHeader = certHeader.substring(BEGIN_CERT.length(),
                certHeader.length() - END_CERT.length() - 1); // funny things with whitespace

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
        Map<String, String> params = Splitter.on(", ").withKeyValueSeparator("=").split(
                clientCert.getSubjectDN().getName()
        );
        String surname = params.get("SURNAME");
        String givenname = params.get("GIVENNAME");
        String serialnumber = params.get("SERIALNUMBER");
        return new EENaturalPerson(surname,givenname,serialnumber);
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
            LOG.error("DigidocService returned an error: " + mobileIdError.getMessage(), mobileIdError);
            return fillErrorInfo(model, SAMLRequest, authenticationRequest,mobileIdError,"error.mobileid", lang);
        }


        boolean isLegalPersonRequest = isLegalPersonRequest(authenticationRequest);
        IdPTokenCacheItem cacheItem = new IdPTokenCacheItem(SAMLRequest, authenticationRequest, mobileIDSession, isLegalPersonRequest);
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
    public String showMobileIdCheck(HttpServletRequest request, @RequestParam(required = false) String sessionToken,
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
                LOG.error("DigidocService returned an error: " + mobileIdError.getMessage(), mobileIdError);
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

        if (cacheItem.isLegalPersonRequest()) {
            HttpSession session = request.getSession();
            session.setAttribute("naturalPerson", naturalPerson);
            session.setAttribute("samlRequest", cacheItem.getSamlRequest());

            model.addAttribute( "responseCallback", cacheItem.getSamlRequest().getAssertionConsumerServiceURL());
            model.addAttribute( "SAMLRequest", cacheItem.getOriginalRequest());
            model.addAttribute("SAMLResponse", eidasIdPI.buildErrorResponse(cacheItem.getSamlRequest()));
            model.addAttribute("lang", lang);
            addPersonAttributes(model, naturalPerson);
            return "legal-person-select";
        } else {
            String response = eidasIdPI.buildAuthenticationResponse(cacheItem.getSamlRequest(),naturalPerson);

            model.addAttribute("SAMLResponse", response);
            model.addAttribute( "responseCallback", cacheItem.getSamlRequest().getAssertionConsumerServiceURL());
            model.addAttribute("lang", lang);
            addPersonAttributes(model, naturalPerson);
            return "authorize";
        }
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

    @GetMapping(value="/legal_person")
    public ModelAndView fetchLegalPersonsList(HttpServletRequest request) {
        try {
            EENaturalPerson naturalPerson = (EENaturalPerson) request.getSession().getAttribute("naturalPerson");
            if (naturalPerson == null) {
                return getJsonErrorView(BAD_REQUEST, "Authenticated representative of the legal person was not found in session");
            }

            List<EELegalPerson> legalPersons = eBusinessRegistryService.executeEsindusV2Service(naturalPerson.getIdCode());
            if (CollectionUtils.isEmpty(legalPersons)) {
                return getJsonErrorView(FORBIDDEN, "No related legal persons found for current user");
            } else {
                request.getSession().setAttribute("legalPersons", legalPersons);
                ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView(), Collections.singletonMap("legalPersons", legalPersons));
                modelAndView.setStatus(OK);
                return modelAndView;
            }

        } catch (XroadServiceNotAvailable e) {
            LOG.error("X-road service not available. Error: {}", e.getMessage(), e);
            return getJsonErrorView(BAD_GATEWAY, "Could not connect to business registry");
        } catch (Exception e) {
            LOG.error("Failed to get a list  of legal persons. Error: {}", e.getMessage(), e);
            return getJsonErrorView(INTERNAL_SERVER_ERROR, "Unexpected technical exception encountered.");
        }
    }

    @PostMapping(value="/confirm_legal_person")
    public String confirmSelectedLegalperson(HttpServletRequest request, @RequestParam(required = true) String legalPersonId,
                                             Model model)  {
        LOG.debug("/confirm_legal_person request started");

        try {
            IAuthenticationRequest authenticationRequest = (IAuthenticationRequest)request.getSession().getAttribute("samlRequest");
            EENaturalPerson naturalPerson = (EENaturalPerson)request.getSession().getAttribute("naturalPerson");
            List<EELegalPerson> legalPersons = (List)request.getSession().getAttribute("legalPersons");
            Assert.notNull(legalPersons,"Cannot select a legal person. No legalPersons list found in session");
            Optional<EELegalPerson> selectedLegalPerson = legalPersons.stream().filter(e -> e.getLegalPersonIdentifier().equals(legalPersonId)).findFirst();

            if (selectedLegalPerson.isPresent()) {
                String samlResponse = eidasIdPI.buildAuthenticationResponse(authenticationRequest, naturalPerson, selectedLegalPerson.get());
                model.addAttribute("SAMLResponse", samlResponse);
                model.addAttribute("responseCallback", authenticationRequest.getAssertionConsumerServiceURL());
                addPersonAttributes(model, naturalPerson);
                addLegalPersonAttributes(model, selectedLegalPerson.get());
                LOG.debug("/confirm_legal_person request complete");
                return "authorize";
            } else {
                throw new IllegalStateException("No legal person found with this id '" + legalPersonId + "'");
            }

        } finally {
            request.getSession().invalidate();
        }
    }

    private ModelAndView getJsonErrorView(HttpStatus statusCode, String errorMessage) {
        Map<String, String> map = new HashMap<>();
        map.put("error", errorMessage);
        ModelAndView mv = new ModelAndView(new MappingJackson2JsonView(), map);
        mv.setStatus(statusCode);
        return mv;
    }

    private boolean isLegalPersonRequest(IAuthenticationRequest authenticationRequest) {
        return authenticationRequest.getRequestedAttributes().getAttributeMap().keySet().containsAll(EidasIdPImpl.EE_LEGAL_PERSON_ATTRIBUTES.getAttributes());
    }
}
