package ee.ria.IdP.xroad;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@Getter
public class Company {
    private final String companyRegistryCode;
    private final String companyName;
}
