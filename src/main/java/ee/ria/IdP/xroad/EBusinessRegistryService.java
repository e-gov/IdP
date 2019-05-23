package ee.ria.IdP.xroad;


import ee.ria.IdP.exceptions.XroadServiceNotAvailable;
import ee.ria.IdP.model.EELegalPerson;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.net.ssl.SSLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang.StringEscapeUtils.escapeXml;

@Slf4j
public class EBusinessRegistryService {

    private static final String SOAP_REQUEST_TEMPLATE = "xtee-arireg.esindus_v2.v1.ftl";

    @NonNull
    private final Configuration templateConfiguration;
    @NonNull
    private final XroadConfigurationImpl xroadConfiguration;

    private final String xpathFilterForEttevotjad;

    public EBusinessRegistryService(@NonNull Configuration templateConfiguration, @NonNull XroadConfigurationImpl xroadConfiguration) {
        this.templateConfiguration = templateConfiguration;
        this.xroadConfiguration = xroadConfiguration;
        this.xpathFilterForEttevotjad = "//ettevotjad/item[" +
                "staatus = 'R' " +
                "and (" + getConditionList(xroadConfiguration.getXRoadEsindusv2AllowedTypes(), "oiguslik_vorm = '%s'") + ") " +
                "and isikud/item[" +
                    "isikukood_riik = 'EST' " +
                    "and fyysilise_isiku_kood = '%s' " +
                    "and ainuesindusoigus_olemas = 'JAH'" +
                "]" +
            "]";
    }

    public List<EELegalPerson> executeEsindusV2Service(String idCode) {
        Assert.notNull(idCode, "idCode is required!");
        Assert.isTrue(idCode.matches("^[0-9]{11,11}$"), "idCode has invalid format! Must contain only numbers");

        try {
            String request = getEsindusV2Request(idCode, UUID.randomUUID().toString());
            NodeList response = send(request, String.format(xpathFilterForEttevotjad, idCode));
            if (response != null) {
                return extractResults(response);
            } else {
                return emptyList();
            }
        } catch (XroadServiceNotAvailable e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected error occurred: " +e.getMessage(), e);
        }
    }

    private List<EELegalPerson> extractResults(NodeList response) throws XPathExpressionException {
        List<EELegalPerson> legalPersons = new ArrayList<>();
        for (int i = 0; i < response.getLength(); i++) {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String idCode = (String)xPath.compile("ariregistri_kood/text()").evaluate(response.item(i), XPathConstants.STRING);
            String name = (String)xPath.compile("arinimi/text()").evaluate(response.item(i), XPathConstants.STRING);
            legalPersons.add(new EELegalPerson(name, idCode));
        }
        return legalPersons;
    }

    protected String getEsindusV2Request(String idCode, String nonce) {
        try {
            Template template = templateConfiguration.getTemplate(SOAP_REQUEST_TEMPLATE);
            try (Writer writer = new StringWriter()) {

                Map<String, String> params = new HashMap();
                params.put("nonce", escapeXml(nonce));
                params.put("serviceRoadInstance", escapeXml(xroadConfiguration.getXRoadServiceRoadInstance()));
                params.put("serviceMemberClass", escapeXml(xroadConfiguration.getXRoadServiceMemberClass()));
                params.put("serviceMemberCode", escapeXml(xroadConfiguration.getXRoadServiceMemberCode()));
                params.put("serviceSubsystemCode", escapeXml(xroadConfiguration.getXRoadServiceSubsystemCode()));

                params.put("subsystemRoadInstance", escapeXml(xroadConfiguration.getXRoadClientSubSystemRoadInstance()));
                params.put("subsystemMemberClass", escapeXml(xroadConfiguration.getXRoadClientSubSystemMemberClass()));
                params.put("subsystemMemberCode", escapeXml(xroadConfiguration.getXRoadClientSubSystemMemberCode()));
                params.put("subsystemSubsystemCode", escapeXml(xroadConfiguration.getXRoadClientSubSystemSubsystemCode()));

                params.put("personIdCode", escapeXml(idCode));
                template.process(params, writer);
                return writer.toString();
            }
        } catch (IOException | TemplateException e) {
            throw new IllegalStateException("Could not create SOAP request from template: " + e.getMessage(), e);
        }
    }

    protected NodeList send(String request, String filterExpression) {
        try {
            log.info("Sending 'POST' request to URL: {}, body: {}  ", xroadConfiguration.getXroadServerUrl(), request);
            URL obj = new URL(xroadConfiguration.getXroadServerUrl());
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setReadTimeout(xroadConfiguration.getXroadServerReadTimeoutInMilliseconds());
            con.setConnectTimeout(xroadConfiguration.getXroadServerConnectTimeoutInMilliseconds());
            con.setRequestMethod("POST");
            con.setRequestProperty("SOAPAction", "");
            con.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
            con.setDoOutput(true);

            try( DataOutputStream wr = new DataOutputStream( con.getOutputStream())) {
                wr.write( request.getBytes(StandardCharsets.UTF_8) );
            }

            try (InputStream in = (InputStream) con.getContent()) {
                int responseCode = con.getResponseCode();
                String response = IOUtils.toString(in, StandardCharsets.UTF_8);
                log.info("Response received. Code: {}, Response body: {}", responseCode, response);

                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                Document xmlDocument = builder.parse(IOUtils.toInputStream(response, StandardCharsets.UTF_8));
                XPath xPath = XPathFactory.newInstance().newXPath();
                String faultCode = (String)xPath.compile("/Envelope/Body/Fault/faultcode/text()")
                        .evaluate(xmlDocument, XPathConstants.STRING);
                if (StringUtils.isEmpty(faultCode)) {
                    return (NodeList) xPath.compile(filterExpression).evaluate(xmlDocument, XPathConstants.NODESET);
                } else {
                    String faultstring = (String)xPath.compile("/Envelope/Body/Fault/faultstring/text()")
                            .evaluate(xmlDocument, XPathConstants.STRING);
                    throw new IllegalStateException("XRoad service returned a soap fault: faultcode = '" + faultCode
                            + "', faultstring = '" + faultstring + "'");
                }
            }

        } catch (SocketTimeoutException | ConnectException | UnknownHostException | SSLException e) {
            throw new XroadServiceNotAvailable("Connection failed: " + e.getMessage(), e);
        } catch (XPathExpressionException | IOException | SAXException | ParserConfigurationException e) {
            throw new IllegalStateException("Failed to extract data from response: " + e.getMessage(), e);
        }
    }

    private String getConditionList(String[] type, String parmeter) {
        return StringUtils.join(Arrays.stream(type).map(str -> String.format(parmeter, str)).collect(Collectors.toList()), " or ");
    }
}

