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

    @Override
    public String toString() {
        return "AppProperties{" +
                "adminEmails=" + adminEmails +
                ", googleWorkspace=" + googleWorkspace +
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
}
