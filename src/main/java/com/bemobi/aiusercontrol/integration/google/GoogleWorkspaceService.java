package com.bemobi.aiusercontrol.integration.google;

import com.bemobi.aiusercontrol.config.AppProperties;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.User;
import com.google.api.services.directory.model.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@ConditionalOnProperty(prefix = "app.google-workspace", name = "enabled", havingValue = "true")
public class GoogleWorkspaceService {

    private static final Logger log = LoggerFactory.getLogger(GoogleWorkspaceService.class);
    private static final int MAX_RESULTS = 500;

    private final Directory directory;
    private final AppProperties appProperties;

    public GoogleWorkspaceService(Directory directory, AppProperties appProperties) {
        this.directory = directory;
        this.appProperties = appProperties;
    }

    public List<GwsUser> fetchAllUsers() {
        String domain = appProperties.getGoogleWorkspace().getDomain();
        String customSchemaName = appProperties.getGoogleWorkspace().getCustomSchemaName();

        log.info("Fetching all users from Google Workspace domain: {}", domain);

        List<GwsUser> allUsers = new ArrayList<>();
        String pageToken = null;

        try {
            do {
                Directory.Users.List request = directory.users().list()
                        .setDomain(domain)
                        .setMaxResults(MAX_RESULTS)
                        .setProjection("full")
                        .setOrderBy("email");

                if (pageToken != null) {
                    request.setPageToken(pageToken);
                }

                Users response = request.execute();
                List<User> users = response.getUsers();

                if (users != null) {
                    for (User user : users) {
                        GwsUser gwsUser = mapToGwsUser(user, customSchemaName);
                        allUsers.add(gwsUser);
                    }
                }

                pageToken = response.getNextPageToken();
                log.debug("Fetched {} users so far, nextPageToken: {}", allUsers.size(),
                        pageToken != null ? "present" : "none");
            } while (pageToken != null);

            log.info("Successfully fetched {} users from Google Workspace", allUsers.size());
            return allUsers;

        } catch (IOException e) {
            String message = "Failed to fetch users from Google Workspace: " + e.getMessage();
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    public Optional<GwsUser> lookupUserByEmail(String email) {
        String customSchemaName = appProperties.getGoogleWorkspace().getCustomSchemaName();
        log.debug("Looking up user in GWS: {}", email);
        try {
            User user = directory.users().get(email)
                    .setProjection("full")
                    .execute();
            return Optional.of(mapToGwsUser(user, customSchemaName));
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) {
                log.debug("User not found in GWS: {}", email);
                return Optional.empty();
            }
            log.error("GWS lookup failed for {}: {}", email, e.getMessage());
            throw new RuntimeException("GWS lookup failed for " + email, e);
        } catch (IOException e) {
            log.error("GWS lookup IO error for {}: {}", email, e.getMessage());
            throw new RuntimeException("GWS lookup failed for " + email, e);
        }
    }

    @SuppressWarnings("unchecked")
    private GwsUser mapToGwsUser(User user, String customSchemaName) {
        String email = user.getPrimaryEmail();
        String name = user.getName() != null ? user.getName().getFullName() : null;
        String avatarUrl = user.getThumbnailPhotoUrl();
        String githubUsername = null;

        Map<String, Map<String, Object>> customSchemas = user.getCustomSchemas();
        if (customSchemas != null) {
            Map<String, Object> schema = customSchemas.get(customSchemaName);
            if (schema != null) {
                Object githubValue = schema.get("git_name");
                if (githubValue != null) {
                    githubUsername = githubValue.toString();
                }
            }
        }

        String department = null;
        List<Map<String, Object>> organizations = (List<Map<String, Object>>) user.get("organizations");
        if (organizations != null) {
            for (Map<String, Object> org : organizations) {
                Boolean primary = (Boolean) org.get("primary");
                if (Boolean.TRUE.equals(primary)) {
                    Object dept = org.get("department");
                    if (dept != null) {
                        department = dept.toString();
                    }
                    break;
                }
            }
        }

        return new GwsUser(email, name, avatarUrl, githubUsername, department);
    }

    public static class GwsUser {

        private final String email;
        private final String name;
        private final String avatarUrl;
        private final String githubUsername;
        private final String department;

        public GwsUser(String email, String name, String avatarUrl, String githubUsername, String department) {
            this.email = email;
            this.name = name;
            this.avatarUrl = avatarUrl;
            this.githubUsername = githubUsername;
            this.department = department;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public String getGithubUsername() {
            return githubUsername;
        }

        public String getDepartment() {
            return department;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GwsUser gwsUser = (GwsUser) o;
            return Objects.equals(email, gwsUser.email);
        }

        @Override
        public int hashCode() {
            return Objects.hash(email);
        }

        @Override
        public String toString() {
            return "GwsUser{" +
                    "email='" + email + '\'' +
                    ", name='" + name + '\'' +
                    ", avatarUrl='" + avatarUrl + '\'' +
                    ", githubUsername='" + githubUsername + '\'' +
                    ", department='" + department + '\'' +
                    '}';
        }
    }
}
