package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.validation.Errors;
import com.thoughtworks.go.plugin.api.validation.ValidationError;
import com.tw.go.plugin.util.StringUtil;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class RepoUrl {
    public static final String REPO_URL = "REPO_URL";
    public static final String USERNAME = "USERNAME";
    public static final String PASSWORD = "PASSWORD";
    private final String url;
    private Credentials credentials;
    private static HashMap<String, ConnectionChecker> map = new HashMap<String, ConnectionChecker>();

    static {
        map.put("http", new HttpConnectionChecker());
    }

    public RepoUrl(String url, String user, String password) {
        this.url = url;
        this.credentials = new Credentials(user, password);
    }

    public void validate(Errors errors) {
        try {
            if (StringUtil.isBlank(url)) {
                errors.addError(new ValidationError(REPO_URL, "Repository url is empty"));
                return;
            }
            URL validatedUrl = new URL(this.url);
            if (!(validatedUrl.getProtocol().startsWith("http"))) {
                errors.addError(new ValidationError(REPO_URL, "Invalid URL: Only http is supported."));
            }

            if (StringUtil.isNotBlank(validatedUrl.getUserInfo())) {
                errors.addError(new ValidationError(REPO_URL, "User info should not be provided as part of the URL. Please provide credentials using USERNAME and PASSWORD configuration keys."));
            }
            credentials.validate(errors);
        } catch (MalformedURLException e) {
            errors.addError(new ValidationError(REPO_URL, "Invalid URL : " + url));
        }
    }

    ConnectionChecker getChecker() {
        try {
            return map.get(new URL(url).getProtocol());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid URL: " + e);
        }
    }


    public String getUrlWithBasicAuth() {
        String localUrl = this.url;
        try {
            new URL(localUrl);
            if (credentials.provided()) {
                String[] parts = localUrl.split("//");
                if (parts.length != 2) throw new RuntimeException(String.format("Invalid uri format %s", this.url));
                localUrl = parts[0] + "//" + credentials.getUserInfo() + "@" + parts[1];
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return localUrl;
    }

    public void checkConnection() {
        getChecker().checkConnection(url, credentials);
    }

    public String forDisplay() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RepoUrl repoUrl = (RepoUrl) o;

        if (!url.equals(repoUrl.url)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    public String getRepoId() {
        return DigestUtils.md5Hex(url);

    }
}
