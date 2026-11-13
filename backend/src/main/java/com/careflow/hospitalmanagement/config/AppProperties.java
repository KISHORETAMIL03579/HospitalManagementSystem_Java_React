package com.careflow.hospitalmanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Smtp smtp = new Smtp();

    public Jwt getJwt() {
        return jwt;
    }

    public Smtp getSmtp() {
        return smtp;
    }

    public static class Jwt {
        private String secret = "CareFlow_Super_Secret_JWT_Signing_Key_2026_Minimum_32_Bytes!";
        private int expirationMinutes = 120;
        private String issuer = "CareFlowHMS";
        private String audience = "CareFlowHMSClient";

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public int getExpirationMinutes() {
            return expirationMinutes;
        }

        public void setExpirationMinutes(int expirationMinutes) {
            this.expirationMinutes = expirationMinutes;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getAudience() {
            return audience;
        }

        public void setAudience(String audience) {
            this.audience = audience;
        }
    }

    public static class Smtp {
        private String frontendBaseUrl = "http://localhost:5173";
        private String supportUrl = "http://localhost:5173/contact";
        private String senderEmail = "noreply@careflow.com";
        private String senderName = "CareFlow HMS Security";
        private boolean enableRealDelivery = false;

        public String getFrontendBaseUrl() {
            return frontendBaseUrl;
        }

        public void setFrontendBaseUrl(String frontendBaseUrl) {
            this.frontendBaseUrl = frontendBaseUrl;
        }

        public String getSupportUrl() {
            return supportUrl;
        }

        public void setSupportUrl(String supportUrl) {
            this.supportUrl = supportUrl;
        }

        public String getSenderEmail() {
            return senderEmail;
        }

        public void setSenderEmail(String senderEmail) {
            this.senderEmail = senderEmail;
        }

        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }

        public boolean isEnableRealDelivery() {
            return enableRealDelivery;
        }

        public void setEnableRealDelivery(boolean enableRealDelivery) {
            this.enableRealDelivery = enableRealDelivery;
        }
    }
}
