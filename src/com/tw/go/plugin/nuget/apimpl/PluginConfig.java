package com.tw.go.plugin.nuget.apimpl;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfigurations;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRepositoryConfiguration;
import com.thoughtworks.go.plugin.api.validation.Errors;
import com.thoughtworks.go.plugin.api.validation.ValidationError;
import com.tw.go.plugin.nuget.config.NuGetPackageConfig;
import com.tw.go.plugin.nuget.config.NuGetRepoConfig;

import static com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration.*;
import static com.tw.go.plugin.nuget.config.NuGetRepoConfig.*;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class PluginConfig implements PackageRepositoryConfiguration {

    private static Logger LOGGER = Logger.getLoggerFor(PluginConfig.class);
    public static final PackageConfiguration REPO_CONFIG_REPO_URL =
            new PackageConfiguration(REPO_URL).with(DISPLAY_NAME, "Package Source").with(DISPLAY_ORDER, 0);
    public static final PackageConfiguration REPO_CONFIG_USERNAME =
            new PackageConfiguration(USERNAME).with(REQUIRED, false).with(DISPLAY_NAME, "UserName").with(DISPLAY_ORDER, 1);
    public static final PackageConfiguration REPO_CONFIG_PASSWORD =
            new PackageConfiguration(PASSWORD).with(REQUIRED, false).with(SECURE, true).with(DISPLAY_NAME, "Password").with(DISPLAY_ORDER, 2);
    public static final PackageConfiguration PKG_CONFIG_PACKAGE_ID =
            new PackageConfiguration(NuGetPackageConfig.PACKAGE_ID).with(DISPLAY_NAME, "Package Id").with(DISPLAY_ORDER, 0);
    public static final PackageConfiguration PKG_CONFIG_POLL_VERSION_FROM =
            new PackageConfiguration(NuGetPackageConfig.POLL_VERSION_FROM).with(REQUIRED, false).with(DISPLAY_NAME, "Version to poll >=").with(DISPLAY_ORDER, 1);
    public static final PackageConfiguration PKG_CONFIG_POLL_VERSION_TO =
            new PackageConfiguration(NuGetPackageConfig.POLL_VERSION_TO).with(REQUIRED, false).with(DISPLAY_NAME, "Version to poll <").with(DISPLAY_ORDER, 2);

    public PackageConfigurations getRepositoryConfiguration() {
        PackageConfigurations configurations = new PackageConfigurations();
        configurations.addConfiguration(REPO_CONFIG_REPO_URL);
        configurations.addConfiguration(REPO_CONFIG_USERNAME);
        configurations.addConfiguration(REPO_CONFIG_PASSWORD);
        return configurations;
    }

    public PackageConfigurations getPackageConfiguration() {
        PackageConfigurations configurations = new PackageConfigurations();
        configurations.addConfiguration(PKG_CONFIG_PACKAGE_ID);
        configurations.addConfiguration(PKG_CONFIG_POLL_VERSION_FROM);
        configurations.addConfiguration(PKG_CONFIG_POLL_VERSION_TO);
        return configurations;
    }

    public boolean isRepositoryConfigurationValid(PackageConfigurations repoConfigs, Errors errors) {
        NuGetRepoConfig nuGetRepoConfig = new NuGetRepoConfig(repoConfigs);
        if (nuGetRepoConfig.isRepoUrlMissing()) {
            String message = "Repository url not specified";
            LOGGER.error(message);
            errors.addError(new ValidationError(REPO_URL, message));
            return false;
        }
        nuGetRepoConfig.getRepoUrl().validate(errors);
        detectInvalidKeys(repoConfigs, errors, nuGetRepoConfig.getValidKeys());
        return !errors.hasErrors();
    }

    private void detectInvalidKeys(PackageConfigurations configs, Errors errors, String[] validKeys){
        for(PackageConfiguration config : configs.list()){
            boolean valid = false;
            for(String validKey : validKeys){
                if(validKey.equals(config.getKey())) {
                    valid = true; break;
                }
            }
            if(!valid) errors.addError(new ValidationError("Unrecognized key: " + config.getKey()));
        }
    }

    public boolean isPackageConfigurationValid(PackageConfigurations packageConfig, PackageConfigurations repoConfig, Errors errors) {
        NuGetPackageConfig nuGetPackageConfig = new NuGetPackageConfig(packageConfig);
        if (nuGetPackageConfig.isPackageIdMissing()) {
            String message = "Package id not specified";
            LOGGER.info(message);
            errors.addError(new ValidationError(NuGetPackageConfig.PACKAGE_ID, message));
            return false;
        }
        String packageId = nuGetPackageConfig.getPackageId();
        if (packageId == null) {
            String message = "Package id is null";
            LOGGER.info(message);
            errors.addError(new ValidationError(NuGetPackageConfig.PACKAGE_ID, message));
        }
        if (packageId != null && isBlank(packageId.trim())) {
            String message = "Package id is empty";
            LOGGER.info(message);
            errors.addError(new ValidationError(NuGetPackageConfig.PACKAGE_ID, message));
        }
        if (packageId != null && (packageId.contains("*") || packageId.contains("?"))) {
            String message = String.format("Package id [%s] is invalid", packageId);
            LOGGER.info(message);
            errors.addError(new ValidationError(NuGetPackageConfig.PACKAGE_ID, message));
        }
        detectInvalidKeys(packageConfig, errors, nuGetPackageConfig.getValidKeys());
        return !errors.hasErrors();
    }

    public void testConnection(PackageConfigurations packageConfigurations, PackageConfigurations repositoryConfigurations) {
        try {
            new NuGetPoller().getLatestRevision(packageConfigurations, repositoryConfigurations);
        } catch (Exception e) {
            String message = "Test Connection failed: " + e.getMessage();
            LOGGER.warn(message);
            throw new RuntimeException(message, e);
        }
    }
}
