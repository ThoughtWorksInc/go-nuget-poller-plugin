package com.tw.go.plugin.nuget.config;

import com.thoughtworks.go.plugin.api.material.packagerepository.Property;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.tw.go.plugin.util.RepoUrl;

public class NuGetRepoConfig {
    private final RepositoryConfiguration repoConfigs;
    private final Property repoUrlConfig;

    public NuGetRepoConfig(RepositoryConfiguration repoConfigs) {
        this.repoConfigs = repoConfigs;
        repoUrlConfig = repoConfigs.get(RepoUrl.REPO_URL);
    }

    public String stringValueOf(Property property) {
        if (property == null) return null;
        return property.getValue();
    }

    public RepoUrl getRepoUrl() {
        return RepoUrl.create(
                repoUrlConfig.getValue(),
                stringValueOf(repoConfigs.get(RepoUrl.USERNAME)),
                stringValueOf(repoConfigs.get(RepoUrl.PASSWORD)));
    }
    public boolean isHttp() {
        return RepoUrl.create(
                repoUrlConfig.getValue(),
                stringValueOf(repoConfigs.get(RepoUrl.USERNAME)),
                stringValueOf(repoConfigs.get(RepoUrl.PASSWORD))).isHttp();
    }

    public boolean isRepoUrlMissing() {
        return repoUrlConfig == null;
    }

    public static String[] getValidKeys() {
        return new String[]{RepoUrl.REPO_URL, RepoUrl.USERNAME, RepoUrl.PASSWORD};
    }
}
