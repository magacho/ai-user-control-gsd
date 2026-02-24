package com.bemobi.aiusercontrol.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.DirectoryScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
@ConditionalOnProperty(prefix = "app.google-workspace", name = "enabled", havingValue = "true")
public class GoogleWorkspaceConfig {

    private static final Logger log = LoggerFactory.getLogger(GoogleWorkspaceConfig.class);

    private final AppProperties appProperties;

    public GoogleWorkspaceConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void validateConfiguration() {
        AppProperties.GoogleWorkspace gws = appProperties.getGoogleWorkspace();

        if (gws.getServiceAccountKeyPath() == null || gws.getServiceAccountKeyPath().isBlank()) {
            throw new IllegalStateException(
                    "Google Workspace integration is enabled but 'app.google-workspace.service-account-key-path' is not configured. " +
                    "Set the GWS_SERVICE_ACCOUNT_KEY_PATH environment variable.");
        }

        if (gws.getDelegatedAdminEmail() == null || gws.getDelegatedAdminEmail().isBlank()) {
            throw new IllegalStateException(
                    "Google Workspace integration is enabled but 'app.google-workspace.delegated-admin-email' is not configured. " +
                    "Set the GWS_DELEGATED_ADMIN_EMAIL environment variable.");
        }

        Path keyFilePath = Path.of(gws.getServiceAccountKeyPath());
        if (!Files.exists(keyFilePath)) {
            throw new IllegalStateException(
                    "Google Workspace service account key file not found at: " + gws.getServiceAccountKeyPath() + ". " +
                    "Ensure the GWS_SERVICE_ACCOUNT_KEY_PATH points to a valid JSON key file.");
        }

        log.info("Google Workspace integration configured successfully (domain: {}, admin: {})",
                gws.getDomain(), gws.getDelegatedAdminEmail());
    }

    @Bean
    public Directory googleDirectoryService() throws IOException, GeneralSecurityException {
        AppProperties.GoogleWorkspace gws = appProperties.getGoogleWorkspace();

        GoogleCredentials credentials = ServiceAccountCredentials
                .fromStream(new FileInputStream(gws.getServiceAccountKeyPath()))
                .createScoped(Collections.singletonList(DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY))
                .createDelegated(gws.getDelegatedAdminEmail());

        return new Directory.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("AI User Control")
                .build();
    }
}
