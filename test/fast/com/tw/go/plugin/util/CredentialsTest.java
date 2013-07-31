package com.tw.go.plugin.util;

import com.thoughtworks.go.plugin.api.validation.Errors;
import com.thoughtworks.go.plugin.api.validation.ValidationError;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class CredentialsTest {
    @Test
    public void shouldGetUserInfo() throws Exception {
        Credentials credentials = new Credentials("user", "password");
        assertThat(credentials.getUserInfo(), is("user:password"));
    }

    @Test
    public void shouldGetUserInfoWithEscapedPassword() throws Exception {
        Credentials credentials = new Credentials("user", "!password@:");
        assertThat(credentials.getUserInfo(), is("user:%21password%40%3A"));
    }

    @Test
    public void shouldFailValidationIfOnlyPasswordProvided() throws Exception {
        Errors errors = new Errors();
        new Credentials(null, "password").validate(errors);
        assertThat(errors.hasErrors(), is(true));
        assertThat(errors.getErrors(), hasItem(new ValidationError(RepoUrl.USERNAME, "Both Username and password are required.")));

        errors = new Errors();
        new Credentials("user", "").validate(errors);
        assertThat(errors.hasErrors(), is(true));
        assertThat(errors.getErrors(), hasItem(new ValidationError(RepoUrl.PASSWORD, "Both Username and password are required.")));
    }
}
