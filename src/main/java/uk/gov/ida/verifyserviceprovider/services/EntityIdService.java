package uk.gov.ida.verifyserviceprovider.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslateSamlResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.InvalidEntityIdException;

import java.util.List;

public class EntityIdService {
    private final List<String> configuredEntityIds;
    private static final Logger LOG = LoggerFactory.getLogger(EntityIdService.class);

    public EntityIdService(List<String> configuredEntityIds) {
        this.configuredEntityIds = configuredEntityIds;
    }

    public String getEntityId(RequestGenerationBody requestGenerationBody) {
        String entityId = requestGenerationBody.getEntityId();
        LOG.info(String.format("Received request to generate authn request with entityId %s", entityId != null ? entityId : "from config"));
        return getEntityId(entityId);
    }

    public String getEntityId(TranslateSamlResponseBody translateSamlResponseBody) {
        String entityId = translateSamlResponseBody.getEntityId();
        LOG.info(String.format("Received request to translate a saml response with entityId %s", entityId != null ? entityId : "from config"));
        return getEntityId(entityId);
    }

    private String getEntityId(String providedEntityId) {
        if (configuredEntityIds.contains(providedEntityId)) {
            return providedEntityId;
        } else {
            throw new InvalidEntityIdException(String.format("Provided entityId: %s is not listed in config", providedEntityId));
        }
    }
}
