package com.example.wardrobe.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Storage storage = new Storage();
    private Cors cors = new Cors();
    private Auth auth = new Auth();
    private Seed seed = new Seed();

    public Storage getStorage() { return storage; }
    public void setStorage(Storage storage) { this.storage = storage; }

    public Cors getCors() { return cors; }
    public void setCors(Cors cors) { this.cors = cors; }

    public Auth getAuth() { return auth; }
    public void setAuth(Auth auth) { this.auth = auth; }

    public Seed getSeed() { return seed; }
    public void setSeed(Seed seed) { this.seed = seed; }

    public static class Storage {
        private String imagesPath;
        public String getImagesPath() { return imagesPath; }
        public void setImagesPath(String imagesPath) { this.imagesPath = imagesPath; }
    }

    public static class Cors {
        private String allowedOrigins;
        public String getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    }

    public static class Auth {
        private boolean allowUserCreation;
        public boolean isAllowUserCreation() { return allowUserCreation; }
        public void setAllowUserCreation(boolean allowUserCreation) { this.allowUserCreation = allowUserCreation; }
    }

    public static class Seed {
        private boolean enabled;
        private String username;
        private String password;
        private String displayName;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
    }
}
