package com.tw.go.plugin.nuget;

import com.tw.go.plugin.nuget.config.RepoUrl;

public class NuGetCmdParams {
    private final String repoId;
    private final String packageSpec;
    private final RepoUrl repoUrl;

    public NuGetCmdParams(String repoId, RepoUrl repoUrl, String packageSpec) {
        this.repoId = repoId;
        this.packageSpec = packageSpec;
        this.repoUrl = repoUrl;
    }

    public String getRepoId() {
        return repoId;
    }

    public String getPackageSpec() {
        return packageSpec;
    }

    public String getApplicablePackageSpec() {
        if (repoUrl.isWindowsShare()) return packageSpec;
        return "Id:" + packageSpec;
    }

    public String getRepoUrl() {
        return repoUrl.forDisplay();
    }
}
