package com.tw.go.plugin.nuget.apimpl;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfigurations;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.nuget.NuGetParams;
import com.tw.go.plugin.nuget.config.NuGetPackageConfig;
import com.tw.go.plugin.nuget.config.NuGetRepoConfig;
import com.tw.go.plugin.nuget.config.RepoUrl;
import org.junit.Test;

import java.util.Date;

import static org.mockito.Mockito.*;

public class NuGetPollerTest {
    @Test
    public void PollerShouldExcuteCorrectCmd(){
        NuGetPoller poller = new NuGetPoller();
        NuGetPoller spy = spy(poller);
        PackageConfigurations repoCfgs = mock(PackageConfigurations.class);
        PackageConfigurations pkgCfgs = mock(PackageConfigurations.class);
        String repoUrlStr = "http://google.com";//something valid to satisfy connection check
        when(repoCfgs.get(NuGetRepoConfig.REPO_URL)).thenReturn(new PackageConfiguration(NuGetRepoConfig.REPO_URL, repoUrlStr));
        String user = "user";
        when(repoCfgs.get(NuGetRepoConfig.USERNAME)).thenReturn(new PackageConfiguration(NuGetRepoConfig.USERNAME, user));
        String password = "passwrod";
        when(repoCfgs.get(NuGetRepoConfig.PASSWORD)).thenReturn(new PackageConfiguration(NuGetRepoConfig.PASSWORD, password));
        String packageId = "7-Zip";
        PackageConfiguration packageConfiguration = new PackageConfiguration(NuGetPackageConfig.PACKAGE_ID, packageId);
        when(pkgCfgs.get(NuGetPackageConfig.PACKAGE_ID)).thenReturn(packageConfiguration);
        PackageRevision dummyResult = new PackageRevision("1.0", new Date(),"user");
        RepoUrl repoUrl = RepoUrl.create(repoUrlStr, user, password);
        NuGetParams params = new NuGetParams(repoUrl, packageId, null, null, null, true);
        doReturn(dummyResult).when(spy).poll(params);
        //actual test
        spy.getLatestRevision(pkgCfgs, repoCfgs);
        verify(spy).poll(params);
    }

}
