package com.tw.go.plugin.util;

import com.thoughtworks.go.plugin.api.validation.Errors;
import com.thoughtworks.go.plugin.api.validation.ValidationError;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class FileRepoUrl extends RepoUrl {
    public FileRepoUrl(String url, String invalidUser, String invalidPassword) {
        super(url, invalidUser, invalidPassword);
    }

    public FileRepoUrl(String url) {
        super(url);
    }

    @Override
    public void validate(Errors errors) {
        try {
            doBasicValidations(errors);
            URL validatedUrl = new URL(this.url);
            if (StringUtil.isNotBlank(validatedUrl.getUserInfo())) {
                errors.addError(new ValidationError(REPO_URL, "User info invalid for file URL"));
            }
        } catch (MalformedURLException e) {
            errors.addError(new ValidationError(REPO_URL, "Invalid URL : " + url));
        }
    }

    public void checkConnection() {
        if (credentialsDetected()) {
            throw new RuntimeException("File protocol does not support username and/or password.");
        }
        try {
            URL url = new URL(this.url);
            if (!new File(url.getPath()).exists()) {
                throw new RuntimeException("Invalid file path.");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
