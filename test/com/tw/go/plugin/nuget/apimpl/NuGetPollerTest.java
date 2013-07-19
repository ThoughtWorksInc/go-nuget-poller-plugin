package com.tw.go.plugin.nuget.apimpl;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfigurations;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.nuget.config.RepoUrl;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

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
        String packageId = "7-Zip";
        PackageConfiguration packageConfiguration = new PackageConfiguration(NuGetConfig.PACKAGE_ID, packageId);
        when(pkgCfgs.get(NuGetConfig.PACKAGE_ID)).thenReturn(packageConfiguration);
        PackageRevision dummyResult = new PackageRevision("1.0", new Date(),"user");
        RepoUrl repoUrl = RepoUrl.create(repoUrlStr, user, password);
        doReturn(dummyResult).when(spy).executeNuGetCmd(eq(repoUrl), argThat(new PackageIdMatcher(packageId)));
        //actual test
        spy.getLatestRevision(pkgCfgs, repoCfgs);
        verify(spy).executeNuGetCmd(eq(repoUrl), argThat(new PackageIdMatcher(packageId)));
    }

    class PackageIdMatcher extends ArgumentMatcher <PackageConfiguration>{
        private String expectedPackageId;

        PackageIdMatcher(String expectedPackageId) {
            this.expectedPackageId = expectedPackageId;
        }

        @Override
        public boolean matches(Object o) {
            return ((PackageConfiguration)o).getValue().equals(expectedPackageId);
        }
    }

}
