package com.exemple.integration.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import com.exemple.authorization.core.client.AuthorizationClientBuilder;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;

@Component
@DependsOn("initCassandra")
public class InitData {

    public static final String APP_HEADER = "app";

    public static final String BACK_APP = "back";

    public static final String TEST_APP = "test";

    public static final String ADMIN_APP = "admin";

    public static final String VERSION_HEADER = "version";

    public static final String VERSION_V1 = "v1";

    public static final String VERSION_V0 = "v0";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SchemaResource schemaResource;

    @Autowired
    private ApplicationDetailService applicationDetailService;

    @Autowired
    private AuthorizationClientBuilder authorizationClientBuilder;

    @PostConstruct
    public void initSchema() throws IOException {

        // APP

        Map<String, Object> detail = new HashMap<>();
        detail.put("keyspace", "test_service");
        detail.put("authorization_keyspace", "test_authorization");
        detail.put("company", "test_company");
        detail.put("clientIds", Sets.newHashSet("test_service", "test_service_user"));
        detail.put("authorization_clientIds", Sets.newHashSet("test_service", "test_service_user"));

        Set<String> accountFilter = new HashSet<>();
        accountFilter.add("lastname");
        accountFilter.add("firstname");
        accountFilter.add("email");
        accountFilter.add("optin_mobile");
        accountFilter.add("civility");
        accountFilter.add("mobile");
        accountFilter.add("creation_date");
        accountFilter.add("update_date");
        accountFilter.add("birthday");
        accountFilter.add("addresses[*[city,street]]");
        accountFilter.add("cgus[code,version]");

        Set<String> accountField = new HashSet<>();
        accountField.add("lastname");
        accountField.add("firstname");
        accountField.add("email");
        accountField.add("optin_mobile");
        accountField.add("civility");
        accountField.add("mobile");
        accountField.add("creation_date");
        accountField.add("update_date");
        accountField.add("birthday");
        accountField.add("addresses[*[city,street]]");
        accountField.add("cgus[code,version]");

        ResourceExecutionContext.get().setKeyspace("test_service");

        SchemaEntity accountSchema = new SchemaEntity();
        accountSchema.setApplication(TEST_APP);
        accountSchema.setVersion(VERSION_V1);
        accountSchema.setResource("account");
        accountSchema.setProfile("user");
        accountSchema.setContent(MAPPER.readTree(IOUtils.toByteArray(new ClassPathResource("account.json").getInputStream())));
        accountSchema.setFilters(accountFilter);
        accountSchema.setFields(accountField);

        schemaResource.save(accountSchema);

        SchemaEntity accountV0Schema = new SchemaEntity();
        accountV0Schema.setApplication(TEST_APP);
        accountV0Schema.setVersion(VERSION_V0);
        accountV0Schema.setResource("account");
        accountV0Schema.setProfile("user");
        accountV0Schema.setContent(MAPPER.readTree(IOUtils.toByteArray(new ClassPathResource("account.v0.json").getInputStream())));
        accountV0Schema.setFilters(accountFilter);

        schemaResource.save(accountV0Schema);

        Set<String> loginFilter = new HashSet<>();
        loginFilter.add("id");
        loginFilter.add("disabled");
        loginFilter.add("username");

        Set<String> loginField = new HashSet<>();
        loginField.add("id");
        loginField.add("disabled");
        loginField.add("username");
        loginField.add("password");

        ObjectNode patchId = MAPPER.createObjectNode();
        patchId.put("op", "add");
        patchId.put("path", "/properties/id/readOnly");
        patchId.put("value", true);

        ObjectNode patchUsername = MAPPER.createObjectNode();
        patchUsername.put("op", "add");
        patchUsername.put("path", "/properties/username/readOnly");
        patchUsername.put("value", true);

        Set<JsonNode> loginPatchs = new HashSet<>();
        loginPatchs.add(patchId);
        loginPatchs.add(patchUsername);

        SchemaEntity loginSchema = new SchemaEntity();
        loginSchema.setApplication(TEST_APP);
        loginSchema.setVersion(VERSION_V1);
        loginSchema.setResource("login");
        loginSchema.setProfile("user");
        loginSchema.setContent(MAPPER.readTree(IOUtils.toByteArray(new ClassPathResource("login.json").getInputStream())));
        loginSchema.setFilters(loginFilter);
        loginSchema.setFields(loginField);
        loginSchema.setPatchs(loginPatchs);

        schemaResource.save(loginSchema);

        Set<String> loginIdFilter = new HashSet<>();
        loginIdFilter.add("id");
        loginIdFilter.add("disabled");
        loginIdFilter.add("username");

        Set<String> loginIdField = new HashSet<>();
        loginIdField.add("id");
        loginIdField.add("disabled");
        loginIdField.add("username");
        loginIdField.add("password");

        SchemaEntity loginIdSchema = new SchemaEntity();
        loginIdSchema.setApplication(TEST_APP);
        loginIdSchema.setVersion(VERSION_V1);
        loginIdSchema.setResource("login_id");
        loginIdSchema.setProfile("user");
        loginIdSchema.setContent(MAPPER.readTree(IOUtils.toByteArray(new ClassPathResource("login_id.json").getInputStream())));
        loginIdSchema.setFilters(loginIdFilter);
        loginIdSchema.setFields(loginIdField);

        schemaResource.save(loginIdSchema);

        Set<String> subscriptionFilter = new HashSet<>();
        subscriptionFilter.add("subscription_date");

        SchemaEntity subscriptionSchema = new SchemaEntity();
        subscriptionSchema.setApplication(TEST_APP);
        subscriptionSchema.setVersion(VERSION_V1);
        subscriptionSchema.setResource("subscription");
        subscriptionSchema.setProfile("user");
        subscriptionSchema.setContent(MAPPER.readTree(IOUtils.toByteArray(new ClassPathResource("subscription.json").getInputStream())));
        subscriptionSchema.setFilters(subscriptionFilter);

        schemaResource.save(subscriptionSchema);

        applicationDetailService.put(TEST_APP, MAPPER.convertValue(detail, JsonNode.class));

        // STOCK

        Map<String, Object> backDetail = new HashMap<>();
        backDetail.put("keyspace", "test_service");
        backDetail.put("company", "test_company");
        backDetail.put("clientIds", Sets.newHashSet("test_back", "test_back_user"));

        applicationDetailService.put(BACK_APP, MAPPER.convertValue(backDetail, JsonNode.class));

        // ADMIN

        loginSchema = new SchemaEntity();
        loginSchema.setApplication(ADMIN_APP);
        loginSchema.setVersion(VERSION_V1);
        loginSchema.setResource("login");
        loginSchema.setProfile("user");
        loginSchema.setContent(MAPPER.readTree(IOUtils.toByteArray(new ClassPathResource("login.json").getInputStream())));
        loginSchema.setFilters(loginFilter);
        loginSchema.setPatchs(loginPatchs);

        schemaResource.save(loginSchema);

        Map<String, Object> adminDetail = new HashMap<>();
        adminDetail.put("keyspace", "test_service");
        adminDetail.put("authorization_keyspace", "test_authorization");
        adminDetail.put("company", "test_company");
        adminDetail.put("clientIds", Sets.newHashSet("test_admin"));
        adminDetail.put("authorization_clientIds", Sets.newHashSet("test_admin"));

        applicationDetailService.put(ADMIN_APP, MAPPER.convertValue(adminDetail, JsonNode.class));

    }

    @PostConstruct
    public void initAuthorization() throws Exception {

        String password = "{bcrypt}" + BCrypt.hashpw("secret", BCrypt.gensalt());

        authorizationClientBuilder

                .withClient("test_service").secret(password).authorizedGrantTypes("client_credentials").redirectUris("xxx")
                .scopes("account:create", "login:head", "login:create", "subscription:update", "subscription:read")
                .autoApprove("account:create", "login:create", "subscription:update", "subscription:read").authorities("ROLE_APP")
                .additionalInformation("keyspace=test_authorization")

                .and()

                .withClient("test_service_user").secret(password).authorizedGrantTypes("password", "authorization_code", "refresh_token")
                .redirectUris("xxx").scopes("account:read", "account:update", "login:update", "login:delete", "login:read", "login:head")
                .autoApprove("account:read", "account:update", "login:update", "login:delete", "login:read").authorities("ROLE_APP")
                .additionalInformation("keyspace=test_authorization")

                .and()

                .withClient("test_back").secret(password).authorizedGrantTypes("client_credentials").scopes("stock").autoApprove("stock")
                .authorities("ROLE_BACK")

                .and()

                .withClient("test_back_user").secret(password).authorizedGrantTypes("password").scopes("stock:read", "stock:update")
                .autoApprove("stock:read", "stock:update").authorities("ROLE_BACK")

                .and()

                .withClient("resource").secret(password).authorizedGrantTypes("client_credentials").authorities("ROLE_TRUSTED_CLIENT")

                .and()

                .withClient("test_admin").secret(password).authorizedGrantTypes("client_credentials").scopes("xxx").autoApprove("xxx")
                .authorities("ROLE_TRUSTED_CLIENT")

                .and().build();
    }

}
