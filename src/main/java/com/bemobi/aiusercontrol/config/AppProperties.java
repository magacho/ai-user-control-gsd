package com.bemobi.aiusercontrol.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private List<String> adminEmails = new ArrayList<>();
    private GoogleWorkspace googleWorkspace = new GoogleWorkspace();
    private Integrations integrations = new Integrations();

    public List<String> getAdminEmails() {
        return adminEmails;
    }

    public void setAdminEmails(List<String> adminEmails) {
        this.adminEmails = adminEmails.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public GoogleWorkspace getGoogleWorkspace() {
        return googleWorkspace;
    }

    public void setGoogleWorkspace(GoogleWorkspace googleWorkspace) {
        this.googleWorkspace = googleWorkspace;
    }

    public Integrations getIntegrations() {
        return integrations;
    }

    public void setIntegrations(Integrations integrations) {
        this.integrations = integrations;
    }

    @Override
    public String toString() {
        return "AppProperties{" +
                "adminEmails=" + adminEmails +
                ", googleWorkspace=" + googleWorkspace +
                ", integrations=" + integrations +
                '}';
    }

    public static class GoogleWorkspace {

        private boolean enabled;
        private String serviceAccountKeyPath;
        private String delegatedAdminEmail;
        private String domain;
        private String customSchemaName = "GitHub";

        public GoogleWorkspace() {
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getServiceAccountKeyPath() {
            return serviceAccountKeyPath;
        }

        public void setServiceAccountKeyPath(String serviceAccountKeyPath) {
            this.serviceAccountKeyPath = serviceAccountKeyPath;
        }

        public String getDelegatedAdminEmail() {
            return delegatedAdminEmail;
        }

        public void setDelegatedAdminEmail(String delegatedAdminEmail) {
            this.delegatedAdminEmail = delegatedAdminEmail;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getCustomSchemaName() {
            return customSchemaName;
        }

        public void setCustomSchemaName(String customSchemaName) {
            this.customSchemaName = customSchemaName;
        }

        @Override
        public String toString() {
            return "GoogleWorkspace{" +
                    "enabled=" + enabled +
                    ", serviceAccountKeyPath='" + serviceAccountKeyPath + '\'' +
                    ", delegatedAdminEmail='" + delegatedAdminEmail + '\'' +
                    ", domain='" + domain + '\'' +
                    ", customSchemaName='" + customSchemaName + '\'' +
                    '}';
        }
    }

    public static class Integrations {

        private Claude claude = new Claude();
        private GitHubCopilot githubCopilot = new GitHubCopilot();
        private Cursor cursor = new Cursor();

        public Integrations() {
        }

        public Claude getClaude() {
            return claude;
        }

        public void setClaude(Claude claude) {
            this.claude = claude;
        }

        public GitHubCopilot getGithubCopilot() {
            return githubCopilot;
        }

        public void setGithubCopilot(GitHubCopilot githubCopilot) {
            this.githubCopilot = githubCopilot;
        }

        public Cursor getCursor() {
            return cursor;
        }

        public void setCursor(Cursor cursor) {
            this.cursor = cursor;
        }

        @Override
        public String toString() {
            return "Integrations{" +
                    "claude=" + claude +
                    ", githubCopilot=" + githubCopilot +
                    ", cursor=" + cursor +
                    '}';
        }
    }

    public static class Claude {

        private String apiUrl;
        private String apiKey;

        public Claude() {
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        @Override
        public String toString() {
            return "Claude{" +
                    "apiUrl='" + apiUrl + '\'' +
                    '}';
        }
    }

    public static class GitHubCopilot {

        private String apiUrl;
        private String apiToken;
        private String organization;

        public GitHubCopilot() {
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        public String getApiToken() {
            return apiToken;
        }

        public void setApiToken(String apiToken) {
            this.apiToken = apiToken;
        }

        public String getOrganization() {
            return organization;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
        }

        @Override
        public String toString() {
            return "GitHubCopilot{" +
                    "apiUrl='" + apiUrl + '\'' +
                    ", organization='" + organization + '\'' +
                    '}';
        }
    }

    public static class Cursor {

        private String apiUrl;
        private String apiKey;

        public Cursor() {
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        @Override
        public String toString() {
            return "Cursor{" +
                    "apiUrl='" + apiUrl + '\'' +
                    '}';
        }
    }
}
