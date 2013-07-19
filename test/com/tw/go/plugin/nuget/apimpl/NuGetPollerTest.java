package com.tw.go.plugin.nuget.apimpl;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfigurations;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.nuget.config.RepoUrl;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class NuGetPollerTest {
    @Test
    public void PollerShouldExcuteCorrectCmd(){
        NuGetPoller poller = new NuGetPoller();
        NuGetPoller spy = spy(poller);
        PackageConfigurations repoCfgs = mock(PackageConfigurations.class);
        PackageConfigurations pkgCfgs = mock(PackageConfigurations.class);
        String repoUrlStr = "http://google.com";//something valid to satisfy connection check
        when(repoCfgs.get(RepoUrl.REPO_URL)).thenReturn(new PackageConfiguration(RepoUrl.REPO_URL, repoUrlStr));
        String user = "user";
        when(repoCfgs.get(RepoUrl.USERNAME)).thenReturn(new PackageConfiguration(RepoUrl.USERNAME, user));
        String password = "passwrod";
        when(repoCfgs.get(RepoUrl.PASSWORD)).thenReturn(new PackageConfiguration(RepoUrl.PASSWORD, password));
        String spec = "7-Zip";
        PackageConfiguration packageConfiguration = new PackageConfiguration(NuGetConfig.PACKAGE_SPEC, spec);
        when(pkgCfgs.get(NuGetConfig.PACKAGE_SPEC)).thenReturn(packageConfiguration);
        PackageRevision dummyResult = new PackageRevision("1.0", new Date(),"user");
        RepoUrl repoUrl = RepoUrl.create(repoUrlStr, user, password);
        doReturn(dummyResult).when(spy).executeNuGetCmd(anyString(),eq(repoUrl), argThat(new SpecMatcher(spec)));
        //actual test
        spy.getLatestRevision(pkgCfgs, repoCfgs);
        verify(spy).executeNuGetCmd(anyString(),eq(repoUrl), argThat(new SpecMatcher(spec)));
    }

    class SpecMatcher extends ArgumentMatcher <PackageConfiguration>{
        private String expectedSpec;

        SpecMatcher(String expectedSpec) {
            this.expectedSpec = expectedSpec;
        }

        @Override
        public boolean matches(Object o) {
            return ((PackageConfiguration)o).getValue().equals(expectedSpec);
        }
    }

    @Test
    public void shouldReturnNewerRevisionBasedonTimestamp() throws ParseException {
        NuGetPoller poller = new NuGetPoller();
        NuGetPoller spy = spy(poller);
        PackageRevision previouslyKnownRevision = new PackageRevision("abc-1.1.0", new SimpleDateFormat("yyyy-MM-dd").parse("2013-07-17"), "xyz");
        PackageRevision latestRevision = new PackageRevision("abc-1.1.1", new SimpleDateFormat("yyyy-MM-dd").parse("2013-07-18"), "pqr");
        PackageConfigurations repoCfg = mock(PackageConfigurations.class);
        PackageConfigurations pkgCfg = mock(PackageConfigurations.class);
        doReturn(latestRevision).when(spy).getLatestRevision(pkgCfg, repoCfg);
        assertThat(spy.latestModificationSince(pkgCfg, repoCfg, previouslyKnownRevision), is(latestRevision));
    }

    @Test
    public void shouldReturnNullIfNoNewerRevision() throws ParseException {
        NuGetPoller poller = new NuGetPoller();
        NuGetPoller spy = spy(poller);
        PackageRevision previouslyKnownRevision = new PackageRevision("abc-1.1.0", new SimpleDateFormat("yyyy-MM-dd").parse("2013-07-17"), "xyz");
        PackageRevision latestRevision = new PackageRevision("abc-1.1.1", new SimpleDateFormat("yyyy-MM-dd").parse("2013-07-17"), "pqr");
        PackageConfigurations repoCfg = mock(PackageConfigurations.class);
        PackageConfigurations pkgCfg = mock(PackageConfigurations.class);
        doReturn(latestRevision).when(spy).getLatestRevision(pkgCfg, repoCfg);
        assertNull(spy.latestModificationSince(pkgCfg, repoCfg, previouslyKnownRevision));
    }
}
