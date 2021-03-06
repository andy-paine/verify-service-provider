package uk.gov.ida.verifyserviceprovider.factories;

import io.dropwizard.setup.Environment;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.security.crypto.KeySupport;
import uk.gov.ida.saml.security.PublicKeyFactory;
import uk.gov.ida.shared.utils.manifest.ManifestReader;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.factories.saml.AuthnRequestFactory;
import uk.gov.ida.verifyserviceprovider.factories.saml.ResponseFactory;
import uk.gov.ida.verifyserviceprovider.healthcheck.MetadataHealthCheck;
import uk.gov.ida.verifyserviceprovider.metadata.MetadataPublicKeyExtractor;
import uk.gov.ida.verifyserviceprovider.resources.GenerateAuthnRequestResource;
import uk.gov.ida.verifyserviceprovider.resources.TranslateSamlResponseResource;
import uk.gov.ida.verifyserviceprovider.resources.VersionNumberResource;
import uk.gov.ida.verifyserviceprovider.services.EntityIdService;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;

import java.security.KeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class VerifyServiceProviderFactory {

    private final DropwizardMetadataResolverFactory metadataResolverFactory = new DropwizardMetadataResolverFactory();
    private final Environment environment;
    private final VerifyServiceProviderConfiguration configuration;
    private final ResponseFactory responseFactory;

    private volatile MetadataResolver hubMetadataResolver;
    private volatile MetadataResolver msaMetadataResolver;
    private final DateTimeComparator dateTimeComparator;
    private final EntityIdService entityIdService;
    private final ManifestReader manifestReader;

    public VerifyServiceProviderFactory(
        VerifyServiceProviderConfiguration configuration,
        Environment environment
    ) throws KeyException {
        this.environment = environment;
        this.configuration = configuration;
        this.responseFactory = new ResponseFactory(getDecryptionKeyPairs(configuration.getSamlPrimaryEncryptionKey(), configuration.getSamlSecondaryEncryptionKey()));
        this.dateTimeComparator = new DateTimeComparator(configuration.getClockSkew());
        this.entityIdService = new EntityIdService(configuration.getServiceEntityIds());
        this.manifestReader = new ManifestReader();
    }

    private List<KeyPair> getDecryptionKeyPairs(PrivateKey primary, PrivateKey secondary) throws KeyException {
        if (secondary == null) {
            return singletonList(createKeyPair(primary));
        } else {
            return asList(createKeyPair(primary), createKeyPair(secondary));
        }
    }

    private KeyPair createKeyPair(PrivateKey key) throws KeyException {
        return new KeyPair(KeySupport.derivePublicKey(key), key);
    }

    public MetadataHealthCheck getHubMetadataHealthCheck() {
        return new MetadataHealthCheck(
            getHubMetadataResolver(),
            configuration.getVerifyHubMetadata().getExpectedEntityId()
        );
    }

    public MetadataHealthCheck getMsaMetadataHealthCheck() {
        return new MetadataHealthCheck(
            getMsaMetadataResolver(),
            configuration.getMsaMetadata().getExpectedEntityId()
        );
    }

    public GenerateAuthnRequestResource getGenerateAuthnRequestResource() throws Exception {
        MetadataPublicKeyExtractor metadataPublicKeyExtractor = new MetadataPublicKeyExtractor(
            configuration.getVerifyHubMetadata().getExpectedEntityId(),
            getHubMetadataResolver(),
            new PublicKeyFactory()
        );
        EncrypterFactory encrypterFactory = new EncrypterFactory(metadataPublicKeyExtractor);

        PrivateKey signingKey = configuration.getSamlSigningKey();

        AuthnRequestFactory authnRequestFactory = new AuthnRequestFactory(
                configuration.getHubSsoLocation(),
                createKeyPair(signingKey),
                manifestReader, encrypterFactory
        );

        return new GenerateAuthnRequestResource(
            authnRequestFactory,
            configuration.getHubSsoLocation(),
            entityIdService
        );
    }

    public TranslateSamlResponseResource getTranslateSamlResponseResource() throws ComponentInitializationException {
        return new TranslateSamlResponseResource(
            responseFactory.createResponseService(
                getHubMetadataResolver(),
                responseFactory.createAssertionTranslator(getMsaMetadataResolver(), dateTimeComparator),
                dateTimeComparator
            ),
            entityIdService
        );
    }

    public VersionNumberResource getVersionNumberResource() {
        return new VersionNumberResource(manifestReader);
    }

    private MetadataResolver getHubMetadataResolver() {
        MetadataResolver resolver = hubMetadataResolver;
        if (resolver == null) {
            synchronized (this) {
                resolver = hubMetadataResolver;
                if (resolver == null) {
                    hubMetadataResolver = resolver = metadataResolverFactory.createMetadataResolver(environment, configuration.getVerifyHubMetadata());
                }
            }
        }
        return resolver;
    }

    private MetadataResolver getMsaMetadataResolver() {
        MetadataResolver resolver = msaMetadataResolver;
        if (resolver == null) {
            synchronized (this) {
                resolver = msaMetadataResolver;
                if (resolver == null) {
                    msaMetadataResolver = resolver = metadataResolverFactory.createMetadataResolverWithoutSignatureValidation(environment, configuration.getMsaMetadata());
                }
            }
        }
        return resolver;
    }
}
