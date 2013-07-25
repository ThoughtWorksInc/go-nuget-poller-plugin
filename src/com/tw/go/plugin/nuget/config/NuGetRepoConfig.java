package com.tw.go.plugin.nuget.config;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfigurations;

public class NuGetRepoConfig {
    public static final String REPO_URL = "REPO_URL";
    public static final String USERNAME = "USERNAME";
    public static final String PASSWORD = "PASSWORD";
    private final PackageConfigurations repoConfigs;
    private final PackageConfiguration repoUrlConfig;

    public NuGetRepoConfig(PackageConfigurations repoConfigs) {
        this.repoConfigs = repoConfigs;
        repoUrlConfig = repoConfigs.get(REPO_URL);
    }

    public String stringValueOf(PackageConfiguration packageConfiguration) {
        if (packageConfiguration == null) return null;
        return packageConfiguration.getValue();
    }

    public RepoUrl getRepoUrl() {
        return RepoUrl.create(
                repoUrlConfig.getValue(),
                stringValueOf(repoConfigs.get(USERNAME)),
                stringValueOf(repoConfigs.get(PASSWORD)));
    }

    public boolean isRepoUrlMissing() {
        return repoUrlConfig == null;
    }

    public static String[] getValidKeys() {
        return new String[]{REPO_URL, USERNAME, PASSWORD};
    }
}
