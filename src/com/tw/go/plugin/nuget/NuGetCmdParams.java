package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.nuget.config.RepoUrl;

import static com.tw.go.plugin.nuget.NuGetPackage.PACKAGE_VERSIONONLY;

public class NuGetCmdParams {
    private final String packageId;
    private final RepoUrl repoUrl;
    private PackageRevision lastKnownVersion = null;

    public NuGetCmdParams(RepoUrl repoUrl, String packageId) {
        this.packageId = packageId;
        this.repoUrl = repoUrl;
    }

    public NuGetCmdParams(RepoUrl repoUrl, String packageId, PackageRevision lastKnownVersion) {
        this(repoUrl, packageId);
        this.lastKnownVersion = lastKnownVersion;
    }

    public String getRepoId() {
        return repoUrl.getRepoId();
    }

    public String getPackageId() {
        return packageId;
    }

    public String getPrefixedPackageId() {
        if (!repoUrl.isHttp()) return packageId;
        return "Id:" + packageId;
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
