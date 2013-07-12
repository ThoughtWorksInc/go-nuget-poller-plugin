package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.validation.Errors;
import com.thoughtworks.go.plugin.api.validation.ValidationError;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.tw.go.plugin.nuget.RepoUrl.REPO_URL;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.internal.matchers.StringContains.containsString;

public class RepoUrlTest {
    @Test
    public void shouldCorrectlyCheckIfRepositoryConfigurationValid() {
        assertRepositoryUrlValidation("", asList(new ValidationError(REPO_URL, "Repository url is empty")), true);
        assertRepositoryUrlValidation(null, asList(new ValidationError(REPO_URL, "Repository url is empty")), true);
        assertRepositoryUrlValidation("  ", asList(new ValidationError(REPO_URL, "Repository url is empty")), true);
        assertRepositoryUrlValidation("h://localhost", asList(new ValidationError(REPO_URL, "Invalid URL : h://localhost")), true);
        assertRepositoryUrlValidation("ftp:///foo.bar", asList(new ValidationError(REPO_URL, "Invalid URL: Only http is supported.")), true);
        assertRepositoryUrlValidation("incorrectUrl", asList(new ValidationError(REPO_URL, "Invalid URL : incorrectUrl")), true);
        assertRepositoryUrlValidation("http://user:password@localhost", asList(new ValidationError(REPO_URL, "User info should not be provided as part of the URL. Please provide credentials using USERNAME and PASSWORD configuration keys.")), true);
        assertRepositoryUrlValidation("http://correct.com/url", new ArrayList<ValidationError>(), false);
        assertRepositoryUrlValidation("file:///foo.bar", asList(new ValidationError(REPO_URL, "Invalid URL: Only http is supported.")), true);
    }

    @Test
    public void shouldReturnURLWithBasicAuth() {
        RepoUrl repoUrl = new RepoUrl("http://localhost", "user", "password");
        assertThat(repoUrl.getUrlWithBasicAuth(), is("http://user:password@localhost"));
    }

    @Test
    public void shouldReturnTheRightConnectionCheckerBasedOnUrlScheme() {
        ConnectionChecker checker = new RepoUrl("http://foobar.com", null, null).getChecker();
        assertThat(checker instanceof HttpConnectionChecker, is(true));
    }

    @Test
    public void shouldThrowExceptionIfURIIsInvalid_checkConnection() {
        try {
            new RepoUrl("://foobar.com", null, null).checkConnection();
            fail("should have failed");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("Invalid URL: java.net.MalformedURLException: no protocol: ://foobar.com"));
        }
    }

    @Test
    public void shouldThrowExceptionIfSchemeIsInvalid_checkConnection() {
        try {
            new RepoUrl("httph://foobar.com", null, null).checkConnection();
            fail("should have failed");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Invalid URL: java.net.MalformedURLException: unknown protocol: httph"));
        }
    }

    @Test
    public void shouldFailCheckConnectionToTheRepoWhenHttpUrlIsNotReachable() {
        try {
            new RepoUrl("http://sifystdgobgr101.thoughtworks.com:8080/tfs/", null, null).checkConnection();
            fail("should fail");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("HTTP/1.1 401 Unauthorized"));
        }
    }

    @Test
    public void shouldGetUrlForDisplay() throws Exception {
        assertThat(new RepoUrl("file:///foo/bar", null, null).forDisplay(), is("file:///foo/bar"));
    }

    private void assertRepositoryUrlValidation(String url, List<ValidationError> expectedErrors, boolean hasErrors) {
        Errors errors = new Errors();
        new RepoUrl(url, null, null).validate(errors);
        assertThat(errors.hasErrors(), is(hasErrors));
        assertThat(errors.getErrors().size(), is(expectedErrors.size()));
        assertThat(errors.getErrors().containsAll(expectedErrors), is(true));
    }
    @Test
    public void shouldReturnUrlWithEscapedPassword() throws Exception {
        String repourl = "http://repohost:1111/some/path#fragment?q=foo";
        String username = "username";
        String password = "!4321abcd";
        RepoUrl repoUrl = new RepoUrl(repourl, username, password);

        assertThat(repoUrl.getUrlWithBasicAuth(), is("http://username:%214321abcd@repohost:1111/some/path#fragment?q=foo"));
    }

    @Test
    public void shouldThrowExceptionIfRepoUrlIsInvalid() throws Exception {
        try {
            RepoUrl repoUrl = new RepoUrl("://some/path", "username", "!4321abcd");
            repoUrl.getUrlWithBasicAuth();
            fail("should throw exception");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("java.net.MalformedURLException"));
        }
    }

    @Test
    public void shouldReturnUrlAsIsIfNoCredentialsProvided() throws Exception {
        String url = "http://repohost:1111/some/path#fragment?q=foo";
        RepoUrl repoUrl = new RepoUrl(url, null, null);
        assertThat(repoUrl.getUrlWithBasicAuth(), is(url));
    }
}
