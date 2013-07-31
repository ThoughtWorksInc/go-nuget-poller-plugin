package com.tw.go.plugin.util;

import com.thoughtworks.go.plugin.api.validation.Errors;

public class WindowsUNCUrl extends RepoUrl {
    public WindowsUNCUrl(String url, String invalidUser, String invalidPassword) {
        super(url, invalidUser, invalidPassword);
    }

    @Override
    public void validate(Errors errors) {
    }

    @Override
    public void checkConnection() {
    }

    @Override
    public String getSeparator() {
        return "\\";
    }
}
