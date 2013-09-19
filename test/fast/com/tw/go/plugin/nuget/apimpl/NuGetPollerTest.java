package com.tw.go.plugin.nuget.apimpl;

import com.thoughtworks.go.plugin.api.material.packagerepository.Property;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.tw.go.plugin.nuget.NuGetParams;
import com.tw.go.plugin.nuget.config.NuGetPackageConfig;
import com.tw.go.plugin.util.RepoUrl;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Date;

import static org.mockito.Mockito.*;

public class NuGetPollerTest {
    @Test
    public void PollerShouldExcuteCorrectCmd(){
        NuGetPoller poller = new NuGetPoller();
        NuGetPoller spy = spy(poller);
        RepositoryConfiguration repoCfgs = mock(RepositoryConfiguration.class);
        PackageConfiguration pkgCfgs = mock(PackageConfiguration.class);
        String repoUrlStr = "http://google.com";//something valid to satisfy connection check
        when(repoCfgs.get(RepoUrl.REPO_URL)).thenReturn(new Property(RepoUrl.REPO_URL, repoUrlStr));
        String user = "user";
        when(repoCfgs.get(RepoUrl.USERNAME)).thenReturn(new Property(RepoUrl.USERNAME, user));
        String password = "passwrod";
        when(repoCfgs.get(RepoUrl.PASSWORD)).thenReturn(new Property(RepoUrl.PASSWORD, password));
        String packageId = "7-Zip";
        Property property = new Property(NuGetPackageConfig.PACKAGE_ID, packageId);
        when(pkgCfgs.get(NuGetPackageConfig.PACKAGE_ID)).thenReturn(property);
        PackageRevision dummyResult = new PackageRevision("1.0", new Date(),"user");
        RepoUrl repoUrl = RepoUrl.create(repoUrlStr, user, password);
        final NuGetParams params = new NuGetParams(repoUrl, packageId, null, null, null, true);
        Matcher<NuGetParams> nuGetParamsMatcher = new BaseMatcher<NuGetParams>() {
            NuGetParams expected = params;
            @Override
            public boolean matches(Object item) {
                NuGetParams nuGetParams = (NuGetParams) item;
                return expected.getPackageId().equals(nuGetParams.getPackageId()) &&
                        expected.getRepoUrl().equals(nuGetParams.getRepoUrl());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(expected.getPackageId());
            }
        };
        doReturn(dummyResult).when(spy).poll(argThat(nuGetParamsMatcher));
        //actual test
        spy.getLatestRevision(pkgCfgs, repoCfgs);
        verify(spy).poll(argThat(nuGetParamsMatcher));
    }

}
