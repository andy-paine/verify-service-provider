package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.dropwizard.Configuration;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Duration;
import uk.gov.ida.verifyserviceprovider.exceptions.EntityIdConfigurationException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class VerifyServiceProviderConfiguration extends Configuration {

    @JsonProperty
    @Valid
    private List<String> serviceEntityIds = new ArrayList<>();

    @JsonProperty
    @Valid
    private String serviceEntityId;

    @JsonProperty
    @NotNull
    @Valid
    private VerifyHubConfiguration verifyHubConfiguration;

    @JsonProperty
    @NotNull
    @Valid
    @JsonDeserialize(using = PrivateKeyDeserializer.class)
    private PrivateKey samlSigningKey;

    @JsonProperty
    @NotNull
    @Valid
    @JsonDeserialize(using = PrivateKeyDeserializer.class)
    private PrivateKey samlPrimaryEncryptionKey;

    @JsonProperty
    @Valid
    @JsonDeserialize(using = PrivateKeyDeserializer.class)
    private PrivateKey samlSecondaryEncryptionKey;

    @JsonProperty
    @NotNull
    @Valid
    private MsaMetadataConfiguration msaMetadata;

    @JsonProperty
    @NotNull
    @Valid
    private Duration clockSkew;

    public List<String> getServiceEntityIds() {
        if (StringUtils.isBlank(serviceEntityId) && serviceEntityIds.isEmpty()) {
            throw new EntityIdConfigurationException("Either serviceEntityId or serviceEntityIds must be defined");
        }
        if (!StringUtils.isBlank(serviceEntityId) && !serviceEntityIds.isEmpty()) {
            throw new EntityIdConfigurationException("Both serviceEntityId and serviceEntityIds defined");
        }
        return StringUtils.isBlank(serviceEntityId) ? serviceEntityIds : Collections.singletonList(serviceEntityId);
    }

    public URI getHubSsoLocation() {
        return verifyHubConfiguration.getHubSsoLocation();
    }

    public PrivateKey getSamlSigningKey() {
        return samlSigningKey;
    }

    public PrivateKey getSamlPrimaryEncryptionKey() {
        return samlPrimaryEncryptionKey;
    }

    public PrivateKey getSamlSecondaryEncryptionKey() {
        return samlSecondaryEncryptionKey;
    }

    public MsaMetadataConfiguration getMsaMetadata() {
        return msaMetadata;
    }

    public HubMetadataConfiguration getVerifyHubMetadata() {
        return verifyHubConfiguration.getHubMetadataConfiguration();
    }

    public Duration getClockSkew() {
        return clockSkew;
    }
}
