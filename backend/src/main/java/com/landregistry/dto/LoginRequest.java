package com.landregistry.dto;

public class LoginRequest {
    private String login;
    private String password;

    public LoginRequest() {}

    public LoginRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public static class Builder {
        private String login;
        private String password;

        public Builder login(String login) { this.login = login; return this; }
        public Builder password(String password) { this.password = password; return this; }

        public LoginRequest build() {
            return new LoginRequest(login, password);
        }
    }
}
