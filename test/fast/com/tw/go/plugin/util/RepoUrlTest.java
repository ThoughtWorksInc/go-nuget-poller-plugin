package com.tw.go.plugin.util;

import com.thoughtworks.go.plugin.api.validation.Errors;
import com.thoughtworks.go.plugin.api.validation.ValidationError;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.tw.go.plugin.util.RepoUrl.REPO_URL;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class RepoUrlTest {
    @Test
    public void shouldCorrectlyCheckIfRepositoryConfigurationValid() {
        assertRepositoryUrlValidation("", asList(new ValidationError(REPO_URL, InvalidRepoUrl.MESSAGE)), true);
        assertRepositoryUrlValidation(null, asList(new ValidationError(REPO_URL, InvalidRepoUrl.MESSAGE)), true);
        assertRepositoryUrlValidation("  ", asList(new ValidationError(REPO_URL, InvalidRepoUrl.MESSAGE)), true);
        assertRepositoryUrlValidation("h://localhost", asList(new ValidationError(REPO_URL, InvalidRepoUrl.MESSAGE)), true);
        assertRepositoryUrlValidation("ftp:///foo.bar", asList(new ValidationError(REPO_URL, InvalidRepoUrl.MESSAGE)), true);
        assertRepositoryUrlValidation("incorrectUrl", asList(new ValidationError(REPO_URL, InvalidRepoUrl.MESSAGE)), true);
        assertRepositoryUrlValidation("http://user:password@localhost", asList(new ValidationError(REPO_URL, "User info should not be provided as part of the URL. Please provide credentials using USERNAME and PASSWORD configuration keys.")), true);
        assertRepositoryUrlValidation("http://correct.com/url", new ArrayList<ValidationError>(), false);
        assertRepositoryUrlValidation("file:///foo.bar", new ArrayList<ValidationError>(), false);
    }

    @Test
    public void shouldReturnURLWithBasicAuth() {
        HttpRepoURL repoUrl = (HttpRepoURL) RepoUrl.create("http://localhost", "user", "password");
        assertThat(repoUrl.getUrlWithBasicAuth(), is("http://user:password@localhost"));
    }

    @Test
    public void shouldReturnTheRightConnectionCheckerBasedOnUrlScheme() {
        RepoUrl checker = RepoUrl.create("http://foobar.com", null, null);
        assertThat(checker instanceof HttpRepoURL, is(true));
    }

    @Test
    public void shouldThrowExceptionIfURIIsInvalid_checkConnection() {
        try {
            RepoUrl.create("://foobar.com", null, null).checkConnection();
            fail("should have failed");
        } catch (Exception e) {
            assertThat(e.getMessage(), is(InvalidRepoUrl.MESSAGE));
        }
    }

    @Test
    public void shouldThrowExceptionIfSchemeIsInvalid_checkConnection() {
        try {
            RepoUrl.create("httph://foobar.com", null, null).checkConnection();
            fail("should have failed");
        } catch (Exception e) {
            assertThat(e.getMessage(), is(InvalidRepoUrl.MESSAGE));
        }
    }

    @Test
    public void shouldGetUrlForDisplay() throws Exception {
        assertThat(RepoUrl.create("file:///foo/bar", null, null).forDisplay(), is("file:///foo/bar"));
    }

    private void assertRepositoryUrlValidation(String url, List<ValidationError> expectedErrors, boolean hasErrors) {
        Errors errors = new Errors();
        RepoUrl.create(url, null, null).validate(errors);
        assertThat(errors.hasErrors(), is(hasErrors));
        assertThat(errors.getErrors().size(), is(expectedErrors.size()));
        assertThat(errors.getErrors().containsAll(expectedErrors), is(true));
    }
    @Test
    public void shouldReturnUrlWithEscapedPassword() throws Exception {
        String repourl = "http://repohost:1111/some/path#fragment?q=foo";
        String username = "username";
        String password = "!4321abcd";
        HttpRepoURL repoUrl = (HttpRepoURL) RepoUrl.create(repourl, username, password);

        assertThat(repoUrl.getUrlWithBasicAuth(), is("http://username:%214321abcd@repohost:1111/some/path#fragment?q=foo"));
    }

    @Test
    public void shouldReturnUrlAsIsIfNoCredentialsProvided() throws Exception {
        String url = "http://repohost:1111/some/path#fragment?q=foo";
        HttpRepoURL repoUrl = (HttpRepoURL) RepoUrl.create(url, null, null);
        assertThat(repoUrl.getUrlWithBasicAuth(), is(url));
    }

    @Test
    public void shouldAcceptWindowsUNCurls(){
        Errors errors = new Errors();
        RepoUrl.create("\\\\insrinaray\\nuget-local-repo", null, null).validate(errors);
        assertFalse(errors.hasErrors());
    }
}
