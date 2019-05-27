package ee.ria.IdP.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public final class EstonianIdCodeUtil {

    public static final String ESTONIAN_ID_CODE_REGEX = "[1-6][0-9]{2}((0[1-9])|(1[0-2]))((0[1-9])|([1-2][0-9])|(3[0-1]))[0-9]{4}";
    public static final String GENERIC_ESTONIAN_ID_CODE_REGEX = "(|PNOEE-)(" + ESTONIAN_ID_CODE_REGEX + ")";
    public static final Pattern ID_CODE_PATTERN = Pattern.compile(GENERIC_ESTONIAN_ID_CODE_REGEX);

    public static String getEstonianIdCode(String idCode) {
        final Matcher matcher = ID_CODE_PATTERN.matcher(idCode);

        if (matcher.matches()) {
            return matcher.group(2);
        } else {
            throw new IllegalArgumentException("Invalid Estonian identity code");
        }
    }
}
