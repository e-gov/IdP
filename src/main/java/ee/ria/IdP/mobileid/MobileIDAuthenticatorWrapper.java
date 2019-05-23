package ee.ria.IdP.mobileid;

import com.codeborne.security.mobileid.MobileIDAuthenticator;
import com.codeborne.security.mobileid.MobileIDSession;

public class MobileIDAuthenticatorWrapper extends MobileIDAuthenticator {

    public MobileIDAuthenticatorWrapper() {
        super();
    }

    public MobileIDAuthenticatorWrapper(String digidocServiceURL) {
        super(digidocServiceURL);
    }

    public MobileIDAuthenticatorWrapper(String digidocServiceURL, String serviceName) {
        super(digidocServiceURL, serviceName);
    }

    public MobileIDSession startLogin(String personalCode, String phone) {
        return super.startLogin(personalCode, null, "+372" + phone);
    }

}
