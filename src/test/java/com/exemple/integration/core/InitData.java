package com.exemple.integration.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import com.exemple.authorization.core.client.AuthorizationClient;
import com.exemple.authorization.core.client.resource.AuthorizationClientResource;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;

import jakarta.annotation.PostConstruct;

@Configuration
@DependsOn("initCassandra")
public class InitData {

    public static final String APP_HEADER = "app";

    public static final String BACK_APP = "back";

    public static final String TEST_APP = "test";

    public static final String VERSION_HEADER = "version";

    public static final String VERSION_V1 = "v1";

    public static final String VERSION_V0 = "v0";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SchemaResource schemaResource;

    @Autowired
    private ApplicationDetailService applicationDetailService;

    @Autowired
    private AuthorizationClientResource authorizationClientResource;

    @PostConstruct
    public void initSchema() throws IOException {

        // APP

        Map<String, Object> detail = new HashMap<>();
        detail.put("keyspace", "test_service");
        detail.put("authorization_keyspace", "test_authorization");
        detail.put("company", "test_company");
        detail.put("clientIds", Sets.newHashSet("test_service", "test_service_user"));
        detail.put("authorization_clientIds", Sets.newHashSet("test_service", "test_service_user"));
        detail.put("account", Map.of("uniqueProperties", Sets.newHashSet("email")));

        ResourceExecutionContext.get().setKeyspace("test_service");

        SchemaEntity accountSchema = new SchemaEntity();
        accountSchema.setApplication(TEST_APP);
        accountSchema.setVersion(VERSION_V1);
        accountSchema.setResource("account");
        accountSchema.setProfile("user");
        accountSchema.setContent(MAPPER.readTree(new ClassPathResource("account.json").getContentAsByteArray()));

        ObjectNode patchUpdateDate = MAPPER.createObjectNode();
        patchUpdateDate.put("op", "add");
        patchUpdateDate.put("path", "/properties/update_date");
        patchUpdateDate.set("value", MAPPER.readTree(
                """
                {"type": "string","format": "date-time","readOnly": true}"
                """));

        ObjectNode patchCreationDate = MAPPER.createObjectNode();
        patchCreationDate.put("op", "add");
        patchCreationDate.put("path", "/required/0");
        patchCreationDate.put("value", "creation_date");

        Set<JsonNode> accountPatchs = new HashSet<>();
        accountPatchs.add(patchUpdateDate);
        accountPatchs.add(patchCreationDate);
        accountSchema.setPatchs(accountPatchs);

        schemaResource.save(accountSchema);

        SchemaEntity accountV0Schema = new SchemaEntity();
        accountV0Schema.setApplication(TEST_APP);
        accountV0Schema.setVersion(VERSION_V0);
        accountV0Schema.setResource("account");
        accountV0Schema.setProfile("user");
        accountV0Schema.setContent(MAPPER.readTree(new ClassPathResource("account.v0.json").getContentAsByteArray()));

        schemaResource.save(accountV0Schema);

        SchemaEntity loginSchema = new SchemaEntity();
        loginSchema.setApplication(TEST_APP);
        loginSchema.setVersion(VERSION_V1);
        loginSchema.setResource("login");
        loginSchema.setProfile("user");
        loginSchema.setContent(MAPPER.readTree(new ClassPathResource("login.json").getContentAsByteArray()));

        schemaResource.save(loginSchema);

        SchemaEntity loginIdSchema = new SchemaEntity();
        loginIdSchema.setApplication(TEST_APP);
        loginIdSchema.setVersion(VERSION_V1);
        loginIdSchema.setResource("login_id");
        loginIdSchema.setProfile("user");
        loginIdSchema.setContent(MAPPER.readTree(new ClassPathResource("login_id.json").getContentAsByteArray()));

        schemaResource.save(loginIdSchema);

        SchemaEntity subscriptionSchema = new SchemaEntity();
        subscriptionSchema.setApplication(TEST_APP);
        subscriptionSchema.setVersion(VERSION_V1);
        subscriptionSchema.setResource("subscription");
        subscriptionSchema.setProfile("user");
        subscriptionSchema.setContent(MAPPER.readTree(new ClassPathResource("subscription.json").getContentAsByteArray()));

        schemaResource.save(subscriptionSchema);

        applicationDetailService.put(TEST_APP, MAPPER.convertValue(detail, JsonNode.class));

        // STOCK

        Map<String, Object> backDetail = new HashMap<>();
        backDetail.put("keyspace", "test_service");
        backDetail.put("company", "test_company");
        backDetail.put("clientIds", Sets.newHashSet("test_back", "test_back_user"));

        applicationDetailService.put(BACK_APP, MAPPER.convertValue(backDetail, JsonNode.class));

    }

    @PostConstruct
    public void initAuthorization() throws Exception {

        var secret = "{bcrypt}" + BCrypt.hashpw("secret", BCrypt.gensalt());

        var testService = AuthorizationClient.builder()
                .id(UUID.randomUUID().toString())
                .clientId("test_service")
                .clientSecret(secret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue())
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue())
                .redirectUri("http://xxx")
                .scope("account:create")
                .scope("login:head")
                .scope("login:create")
                .scope("subscription:update")
                .scope("subscription:read")
                .scope("ROLE_APP")
                .requireAuthorizationConsent(false)
                .build();

        authorizationClientResource.save(testService);

        var testServiceUser = AuthorizationClient.builder()
                .id(UUID.randomUUID().toString())
                .clientId("test_service_user")
                .clientSecret(secret)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE.getValue())
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN.getValue())
                .redirectUri("http://xxx")
                .scope("account:read")
                .scope("account:update")
                .scope("login:head")
                .scope("login:create")
                .scope("login:update")
                .scope("login:read")
                .requireAuthorizationConsent(false)
                .build();

        authorizationClientResource.save(testServiceUser);

        var testBack = AuthorizationClient.builder()
                .id(UUID.randomUUID().toString())
                .clientId("test_back")
                .clientSecret(secret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue())
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue())
                .redirectUri("http://xxx")
                .scope("ROLE_BACK")
                .requireAuthorizationConsent(false)
                .build();

        authorizationClientResource.save(testBack);

        var testBackUser = AuthorizationClient.builder()
                .id(UUID.randomUUID().toString())
                .clientId("test_back_user")
                .clientSecret(secret)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE.getValue())
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN.getValue())
                .redirectUri("http://xxx")
                .scope("stock:read")
                .scope("stock:update")
                .requireAuthorizationConsent(false)
                .build();

        authorizationClientResource.save(testBackUser);

        var resourceClient = AuthorizationClient.builder()
                .id(UUID.randomUUID().toString())
                .clientId("resource")
                .clientSecret(secret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue())
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue())
                .authorizationGrantType(AuthorizationGrantType.JWT_BEARER.getValue())
                .requireAuthorizationConsent(false)
                .build();

        authorizationClientResource.save(resourceClient);

    }

}
