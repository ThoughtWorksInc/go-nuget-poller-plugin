package com.tw.go.plugin.nuget.apimpl;

import com.thoughtworks.go.plugin.api.config.Property;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialProperty;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.tw.go.plugin.nuget.Feed;
import com.tw.go.plugin.nuget.NuGetParams;
import com.tw.go.plugin.nuget.config.NuGetPackageConfig;
import com.tw.go.plugin.util.HttpRepoURL;
import com.tw.go.plugin.util.RepoUrl;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.Date;

import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.*;

public class NuGetPollerTest {
    @Test
    public void PollerShouldExcuteCorrectCmd() {
        NuGetPoller poller = new NuGetPoller();
        NuGetPoller spy = spy(poller);
        RepositoryConfiguration repoCfgs = mock(RepositoryConfiguration.class);
        PackageConfiguration pkgCfgs = mock(PackageConfiguration.class);
        String repoUrlStr = "http://google.com";//something valid to satisfy connection check
        when(repoCfgs.get(RepoUrl.REPO_URL)).thenReturn(new PackageMaterialProperty(RepoUrl.REPO_URL, repoUrlStr));
        String user = "user";
        when(repoCfgs.get(RepoUrl.USERNAME)).thenReturn(new PackageMaterialProperty(RepoUrl.USERNAME, user));
        String password = "passwrod";
        when(repoCfgs.get(RepoUrl.PASSWORD)).thenReturn(new PackageMaterialProperty(RepoUrl.PASSWORD, password));
        String packageId = "7-Zip";
        Property property = new PackageMaterialProperty(NuGetPackageConfig.PACKAGE_ID, packageId);
        when(pkgCfgs.get(NuGetPackageConfig.PACKAGE_ID)).thenReturn(property);
        PackageRevision dummyResult = new PackageRevision("1.0", new Date(), "user");
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

    @Test
    public void shouldReturnNullPackageRevisionWhenNoNewPackagesAreAvailable() throws Exception {
        PackageRevision previouslyKnownRevision = new PackageRevision("1.2", new Date(), "user");
        HttpRepoURL repoUrl = new HttpRepoURL("http://localhost", "user", "password");

        NuGetPoller poller = Mockito.spy(new NuGetPoller());
        Feed feed = mock(Feed.class);
        Document document = mock(Document.class);
        NodeList nodeList = mock(NodeList.class);

        doReturn(feed).when(poller).getFeed(anyString());
        when(feed.download()).thenReturn(document);
        when(document.getElementsByTagName("entry")).thenReturn(nodeList);
        when(nodeList.getLength()).thenReturn(0);

        PackageRevision packageRevision = poller.poll(new NuGetParams(repoUrl, "packageId", "1.0", "2.0", previouslyKnownRevision, false));
        Assert.assertThat(packageRevision, nullValue());
    }
}
