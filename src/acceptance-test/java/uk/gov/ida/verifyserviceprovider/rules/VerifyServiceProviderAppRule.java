package uk.gov.ida.verifyserviceprovider.rules;

import com.fasterxml.jackson.core.JsonProcessingException;
import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Rule;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;

public class VerifyServiceProviderAppRule extends DropwizardAppRule<VerifyServiceProviderConfiguration> {

    @Rule
    private static MockMsaServer msaServer = new MockMsaServer();

    public VerifyServiceProviderAppRule(String secondaryEncryptionKey, ConfigOverride... additionalConfigOverrides) {
        super(
            VerifyServiceProviderApplication.class,
            "verify-service-provider.yml",
            getConfigOverrides(secondaryEncryptionKey, additionalConfigOverrides)
        );
    }

    @Override
    protected void before() {
        IdaSamlBootstrap.bootstrap();
        try {
            msaServer.serveDefaultMetadata();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        super.before();
    }

    private static ConfigOverride[] getConfigOverrides(String secondaryEncryptionKey, ConfigOverride[] additionalConfigOverrides) {
        List<ConfigOverride> configOverrides = Stream.of(
            ConfigOverride.config("server.connector.port", String.valueOf(0)),
            ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG"),
            ConfigOverride.config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
            ConfigOverride.config("verifyHubConfiguration.environment", "COMPLIANCE_TOOL"),
            ConfigOverride.config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY),
            ConfigOverride.config("samlSecondaryEncryptionKey", secondaryEncryptionKey),
            ConfigOverride.config("msaMetadata.uri", msaServer.getMsaMetdataUri()),
            ConfigOverride.config("msaMetadata.expectedEntityId", MockMsaServer.MSA_ENTITY_ID)
        ).collect(Collectors.toList());

        configOverrides.addAll(asList(additionalConfigOverrides));
        return configOverrides.toArray(new ConfigOverride[configOverrides.size()]);
    }

    public VerifyServiceProviderAppRule() {
        this(TEST_RP_PRIVATE_ENCRYPTION_KEY, ConfigOverride.config("serviceEntityIds", "http://verify-service-provider"));
    }

    public VerifyServiceProviderAppRule(String serviceEntityIdOverride) {
        this(TEST_RP_PRIVATE_ENCRYPTION_KEY, ConfigOverride.config("serviceEntityIds", serviceEntityIdOverride));
    }

    public VerifyServiceProviderAppRule(ConfigOverride... configOverrides) {
        this(TEST_RP_PRIVATE_ENCRYPTION_KEY, configOverrides);
    }
}
