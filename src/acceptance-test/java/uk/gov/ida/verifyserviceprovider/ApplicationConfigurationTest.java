package uk.gov.ida.verifyserviceprovider;

import io.dropwizard.testing.ConfigOverride;
import org.junit.After;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.exceptions.EntityIdConfigurationException;
import uk.gov.ida.verifyserviceprovider.rules.VerifyServiceProviderAppRule;

public class ApplicationConfigurationTest {

    private VerifyServiceProviderAppRule appRule;

    @After
    public void tearDown() {
        appRule.getTestSupport().after();
    }

    @Test
    public void shouldStartWithEntityIds() {
        appRule = new VerifyServiceProviderAppRule(
            ConfigOverride.config("serviceEntityIds", "http://verify-service-provider")
        );

        appRule.getTestSupport().before();
    }

    @Test
    public void shouldStartWithEntityId() {
        appRule = new VerifyServiceProviderAppRule(
            ConfigOverride.config("serviceEntityId", "http://verify-service-provider")
        );

        appRule.getTestSupport().before();
    }

    @Test
    public void shouldAllowBothEntityIdAndEntityIdsIfValuesAreMissing() {
        appRule = new VerifyServiceProviderAppRule(
            ConfigOverride.config("serviceEntityIds", "http://verify-service-provider"),
            ConfigOverride.config("serviceEntityId", "")
        );

        appRule.getTestSupport().before();
    }

    @Test(expected = EntityIdConfigurationException.class)
    public void shouldNotStartWithBothEntityIdAndEntityIds() {
        appRule = new VerifyServiceProviderAppRule(
            ConfigOverride.config("serviceEntityIds", "http://verify-service-provider"),
            ConfigOverride.config("serviceEntityId", "http://verify-service-provider")
        );

        appRule.getTestSupport().before();
    }
}
