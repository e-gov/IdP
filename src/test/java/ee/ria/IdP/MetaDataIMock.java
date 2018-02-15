package ee.ria.IdP;

import ee.ria.IdP.metadata.MetaDataI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.opensaml.saml2.metadata.EntityDescriptor;

import javax.annotation.Nonnull;

public class MetaDataIMock implements MetaDataI {
    @Override
    public String generateMetadata() throws EIDASSAMLEngineException {
        return "mocked_metadata";
    }
}
