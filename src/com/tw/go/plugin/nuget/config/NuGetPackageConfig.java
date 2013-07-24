package com.tw.go.plugin.nuget.config;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfigurations;

public class NuGetPackageConfig {
    public static final String PACKAGE_ID = "PACKAGE_ID";
    public static final String POLL_VERSION_FROM = "POLL_VERSION_FROM";
    public static final String POLL_VERSION_TO = "POLL_VERSION_TO";
    public static final String INCLUDE_PRE_RELEASE = "INCLUDE_PRE_RELEASE";
    private final PackageConfigurations packageConfigs;
    private final PackageConfiguration packageIdConfig;

    public NuGetPackageConfig(PackageConfigurations packageConfigs) {
        this.packageConfigs = packageConfigs;
        this.packageIdConfig = packageConfigs.get(PACKAGE_ID);
    }

    public boolean isPackageIdMissing() {
        return packageIdConfig == null;
    }

    public String getPackageId() {
        return packageIdConfig.getValue();
    }

    public String[] getValidKeys() {
        return new String[]{PACKAGE_ID, POLL_VERSION_FROM, POLL_VERSION_TO, INCLUDE_PRE_RELEASE};
    }

    public String getPollVersionFrom() {
        PackageConfiguration from = packageConfigs.get(POLL_VERSION_FROM);
        return (from == null) ? null : from.getValue();
    }

    public String getPollVersionTo() {
        PackageConfiguration to = packageConfigs.get(POLL_VERSION_TO);
        return (to == null) ? null : to.getValue();
    }

    public boolean isIncludePreRelease() {
        PackageConfiguration config = packageConfigs.get(INCLUDE_PRE_RELEASE);
        if(config == null) return true;
        if(config.getValue() == null) return true;
        return !config.getValue().equalsIgnoreCase("no");
    }
}
