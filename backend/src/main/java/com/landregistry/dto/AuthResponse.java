package com.landregistry.dto;

public class AuthResponse {
    private String token;
    private String userId;
    private String login;
    private String displayName;
    private String role;
    private String walletAddress;

    public AuthResponse() {}

    public AuthResponse(String token, String userId, String login, String displayName, String role, String walletAddress) {
        this.token = token;
        this.userId = userId;
        this.login = login;
        this.displayName = displayName;
        this.role = role;
        this.walletAddress = walletAddress;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getWalletAddress() { return walletAddress; }
    public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }

    public static class Builder {
        private String token;
        private String userId;
        private String login;
        private String displayName;
        private String role;
        private String walletAddress;

        public Builder token(String token) { this.token = token; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder login(String login) { this.login = login; return this; }
        public Builder displayName(String displayName) { this.displayName = displayName; return this; }
        public Builder role(String role) { this.role = role; return this; }
        public Builder walletAddress(String walletAddress) { this.walletAddress = walletAddress; return this; }

        public AuthResponse build() {
            return new AuthResponse(token, userId, login, displayName, role, walletAddress);
        }
    }
}
