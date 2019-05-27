package ee.ria.IdP.util;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class EstonianIdCodeUtilTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void getEstonianIdCodeWithSupportedEstonianIdentityCodeFormats() {
        final String[] identityCodePrefixes = {"", "PNOEE-"};
        final String identityCode = "69612310256";

        for (String prefix : identityCodePrefixes) {
            String prefixedIdentityCode = EstonianIdCodeUtil.getEstonianIdCode(prefix + identityCode);
            Assert.assertEquals(identityCode, prefixedIdentityCode);
        }
    }

    @Test
    public void getEstonianIdCodeWithInvalidEstonianIdentityCodeFormat() {
        final String prefixedIdentityCode = "PREFIX69612310256";

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid Estonian identity code");

        EstonianIdCodeUtil.getEstonianIdCode(prefixedIdentityCode);
    }

}
