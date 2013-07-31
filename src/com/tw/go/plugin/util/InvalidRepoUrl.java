package com.tw.go.plugin.util;

import com.thoughtworks.go.plugin.api.validation.Errors;
import com.thoughtworks.go.plugin.api.validation.ValidationError;

public class InvalidRepoUrl extends RepoUrl {

    public static final String MESSAGE = "Invalid/Unsupported Repository url";

    public InvalidRepoUrl(String url, String usernameValue, String passwordValue) {
        super("InvalidRepoUrl");
    }

    @Override
    public void validate(Errors errors) {
        doBasicValidations(errors);
        if(!errors.hasErrors())
        errors.addError(new ValidationError(REPO_URL, MESSAGE));
    }

    @Override
    public void checkConnection() {
        throw new RuntimeException(MESSAGE);
    }
}
