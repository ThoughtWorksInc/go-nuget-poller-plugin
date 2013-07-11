package com.tw.go.plugin.nuget;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.internal.matchers.StringContains.containsString;

public class NuGetCmdParamsTest {
    @Test
    public void shouldReturnUrlWithEscapedPassword() throws Exception {
        String repoid = "repoid";
        String repourl = "http://repohost:1111/some/path#fragment?q=foo";
        String spec = "pkg-spec";
        String username = "username";
        String password = "!4321abcd";
        NuGetCmdParams params = new NuGetCmdParams(repoid, new RepoUrl(repourl, username, password), spec);

        assertThat(params.getRepoFromId(), is("repoid,http://username:%214321abcd@repohost:1111/some/path#fragment?q=foo"));
    }

    @Test
    public void shouldThrowExceptionIfRepoUrlIsInvalid() throws Exception {
        try {
            new NuGetCmdParams("repoid", new RepoUrl("://some/path", "username", "!4321abcd"), "pkg-spec").getRepoFromId();
            fail("should throw exception");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("java.net.MalformedURLException"));
        }
    }

    @Test
    public void shouldReturnUrlAsIsIfNoCredentialsProvided() throws Exception {
        String repoid = "repoid";
        String repourl = "http://repohost:1111/some/path#fragment?q=foo";
        String spec = "pkg-spec";
        NuGetCmdParams params = new NuGetCmdParams(repoid, new RepoUrl(repourl, null, null), spec);
        assertThat(params.getRepoFromId(), is("repoid,http://repohost:1111/some/path#fragment?q=foo"));
    }

    @Test
    public void shouldThrowExceptionIfUrlDoesNotContainTwoForwardSlash() {
        NuGetCmdParams params = new NuGetCmdParams("id", new RepoUrl("file:/path", "user", "pwd"), "spec");
        try {
            params.getRepoFromId();
            fail("expected invalid uri exception");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Invalid uri format file:/path"));
        }
    }

}
