package com.dbee.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String frontendOrigin,
        String jwtSecret,
        String jwtCookieName,
        long jwtExpirationMs,
        boolean cookieSecure,
        String dialogflowProjectId,
        String dialogflowLanguageCode
) {
}
