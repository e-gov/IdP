package ee.ria.IdP.logging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.Assert;

@Slf4j
public class StatisticsLogger {

    private static final Logger STATISTICS_LOG = LoggerFactory.getLogger("IdpStatistics");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final String MDC_PARAM_COUNTRY_CODE = "countryCode";
    public static final String MDC_PARAM_AUTH_TYPE = "authType";
    public static final String MDC_PARAM_PERSON_TYPE = "personType";
    public static final String MDC_PARAM_EVENT_TYPE = "eventType";

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class LogEvent {
        private String personType;
        private String eventType;
        private String authType;
        private String country;
        private String error;
    }

    public static void logEvent(PersonType personType, EventType eventType, AuthType authType, String originCountryCode) {
        try {
            Assert.isTrue(originCountryCode.matches("^[A-Z]{2,2}$"), "Country code has invalid format!");
            MDC.put(MDC_PARAM_COUNTRY_CODE, originCountryCode);
            MDC.put(MDC_PARAM_AUTH_TYPE, authType.name());
            MDC.put(MDC_PARAM_PERSON_TYPE, personType.name());
            MDC.put(MDC_PARAM_EVENT_TYPE, eventType.name());

            STATISTICS_LOG.info(objectMapper.writeValueAsString(
                    LogEvent.builder()
                            .personType(MDC.get(MDC_PARAM_PERSON_TYPE))
                            .eventType(MDC.get(MDC_PARAM_EVENT_TYPE))
                            .authType(MDC.get(MDC_PARAM_AUTH_TYPE))
                            .country(MDC.get(MDC_PARAM_COUNTRY_CODE))
                            .build()));
        } catch (JsonProcessingException e) {
            log.error("Failed to log: " + e.getMessage(), e);
        }
    }

    public static void logErrorEvent(String errorMessageCode) {
        try {
            STATISTICS_LOG.info(objectMapper.writeValueAsString(
                    LogEvent.builder()
                            .personType(MDC.get(MDC_PARAM_PERSON_TYPE))
                            .eventType(EventType.AUTHENTICATION_FAILED.name())
                            .error(errorMessageCode)
                            .authType(MDC.get(MDC_PARAM_AUTH_TYPE))
                            .country(MDC.get(MDC_PARAM_COUNTRY_CODE))
                            .build()));
        } catch (JsonProcessingException e) {
            log.error("Failed to log: " + e.getMessage(), e);
        }
    }

    public enum PersonType {
        NATURAL_PERSON, LEGAL_PERSON_REPRESENTATIVE
    }

    public enum EventType {
        AUTHENTICATION_STARTED, AUTHENTICATION_SUCCESSFUL, LEGAL_PERSON_SELECTION_SUCCESSFUL, AUTHENTICATION_FAILED
    }

    public enum AuthType {
        MID,
        ID_CARD
    }

}