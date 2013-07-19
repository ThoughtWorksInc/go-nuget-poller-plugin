package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.nuget.config.RepoUrl;

import static com.tw.go.plugin.nuget.NuGetPackage.PACKAGE_VERSIONONLY;

public class NuGetCmdParams {
    private final String packageSpec;
    private final RepoUrl repoUrl;
    private PackageRevision lastKnownVersion = null;

    public NuGetCmdParams(RepoUrl repoUrl, String packageSpec) {
        this.packageSpec = packageSpec;
        this.repoUrl = repoUrl;
    }

    public NuGetCmdParams(RepoUrl repoUrl, String packageSpecValue, PackageRevision lastKnownVersion) {
        this(repoUrl, packageSpecValue);
        this.lastKnownVersion = lastKnownVersion;
    }

    public String getRepoId() {
        return repoUrl.getRepoId();
    }

    public String getPackageSpec() {
        return packageSpec;
    }

    public String getApplicablePackageSpec() {
        if (!repoUrl.isHttp()) return packageSpec;
        return "Id:" + packageSpec;
    }

    public String getRepoUrlStr() {
        return repoUrl.forDisplay();
    }
    public RepoUrl getRepoUrl() {
        return repoUrl;
    }

    public boolean isHttp() {
        return repoUrl.isHttp();
    }

    public String getRepoUrlStrWithTrailingSlash() {
        if(repoUrl.forDisplay().endsWith("/")) return repoUrl.forDisplay();
        return repoUrl.forDisplay()+"/";
    }

    public boolean isLastVersionKnown() {
        return lastKnownVersion != null;
    }

    public String getLastKnownVersion() {
        return lastKnownVersion.getDataFor(PACKAGE_VERSIONONLY);
    }
}
