package com.bemobi.aiusercontrol.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private List<String> adminEmails = new ArrayList<>();

    public List<String> getAdminEmails() {
        return adminEmails;
    }

    public void setAdminEmails(List<String> adminEmails) {
        this.adminEmails = adminEmails;
    }

    @Override
    public String toString() {
        return "AppProperties{" +
                "adminEmails=" + adminEmails +
                '}';
    }
}
