package com.tw.go.plugin.util;

import com.thoughtworks.go.plugin.api.validation.Errors;
import com.thoughtworks.go.plugin.api.validation.ValidationError;
import org.apache.commons.codec.digest.DigestUtils;

public abstract class RepoUrl {
    public static final String REPO_URL = "REPO_URL";
    public static final String USERNAME = "USERNAME";
    public static final String PASSWORD = "PASSWORD";
    protected final String url;
    protected Credentials credentials;

    public RepoUrl(String url, String user, String password) {
        this(url);
        this.credentials = new Credentials(user, password);
    }

    public RepoUrl(String url) {
        this.url = url;
    }

    public abstract void validate(Errors errors);

    public void doBasicValidations(Errors errors) {
        if (StringUtil.isBlank(url)) errors.addError(new ValidationError(REPO_URL, "Repository url is empty"));
    }

    public abstract void checkConnection();

    public String forDisplay() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RepoUrl repoUrl = (RepoUrl) o;

        return url.equals(repoUrl.url);

    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    public String getRepoId() {
        return DigestUtils.md5Hex(url);

    }

    public static RepoUrl create(String url, String usernameValue, String passwordValue) {
        if (url == null) return new InvalidRepoUrl(url, usernameValue, passwordValue);
        if (url.startsWith("http://")) return new HttpRepoURL(url, usernameValue, passwordValue);
        if (url.startsWith("https://")) return new HttpsRepoURL(url, usernameValue, passwordValue);
        if (url.startsWith("file://")) return new FileRepoUrl(url, usernameValue, passwordValue);
        if (url.startsWith("\\\\")) return new WindowsUNCUrl(url, usernameValue, passwordValue);
        return new InvalidRepoUrl(url, usernameValue, passwordValue);
    }

    protected boolean credentialsDetected() {
        return credentials != null && credentials.detected();
    }

    public boolean isHttp() {
        return this instanceof HttpRepoURL || this instanceof HttpsRepoURL ;
    }

    public String getSeparator() {
        return "/";
    }
}
